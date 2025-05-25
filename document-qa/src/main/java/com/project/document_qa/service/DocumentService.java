package com.project.document_qa.service;

import com.project.document_qa.dto.DocumentUploadRequest;
import com.project.document_qa.enums.ApiResponseCode;
import com.project.document_qa.enums.DocumentStatus;
import com.project.document_qa.exception.CommonApiException;
import com.project.document_qa.exception.ResourceNotFoundException;
import com.project.document_qa.model.*;
import com.project.document_qa.repository.DocumentRepository;
import com.project.document_qa.repository.DocumentTypeRepository;
import com.project.document_qa.repository.KeywordRepository;
import com.project.document_qa.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final KeywordRepository keywordRepository;
    private final JobLauncher jobLauncher;
    private final Job documentProcessingJob;
    private final DocumentSearchService searchService;
    private final CacheService cacheService;
    private final UserRepository userRepository;
    private final BatchJobService batchJobService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2, maxDelay = 10000))
    public Document uploadDocument(MultipartFile file, DocumentUploadRequest metadata, UserDetails userDetails) throws IOException {
        if (userDetails == null) {
            throw new CommonApiException(ApiResponseCode.Login.UNAUTHENTICATED);
        }
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(ApiResponseCode.User.USER_NOT_FOUND));
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath);
        String typeName = metadata.getDocumentType() != null ? metadata.getDocumentType() : "UNKNOWN";
        DocumentType documentType = getDocumentType(typeName);

        if (documentType == null)
            throw new CommonApiException(ApiResponseCode.Document.DOCUMENT_NOT_FOUND);
        Set<Keyword> keywords = new HashSet<>();
        if (metadata.getKeywords() != null) {
            for (String keywordName : metadata.getKeywords()) {
                Keyword keyword = keywordRepository.findByName(keywordName)
                        .orElseGet(() -> keywordRepository.save(Keyword.builder()
                                .name(keywordName)
                                .build()));
                keywords.add(keyword);
            }
        }
        Document document = getDocument(file, metadata, originalFilename, filePath, user, documentType, keywords);
        document = documentRepository.save(document);
        cacheService.cacheDocumentSummary(document);
        searchService.indexDocument(document);
        batchJobService.launchDocumentProcessingJob(document.getId());
        return document;
    }

    private DocumentType getDocumentType(String typeName) {
        return documentTypeRepository.findByName(typeName)
                .orElseGet(() -> {
                    log.info("Creating new document type: {}", typeName);
                    return documentTypeRepository.save(DocumentType.builder()
                            .name(typeName)
                            .build());
                });
    }

    private static Document getDocument(MultipartFile file, DocumentUploadRequest metadata, String originalFilename, Path filePath, User user, DocumentType documentType, Set<Keyword> keywords) {
        return Document.builder()
                .fileName(originalFilename)
                .filePath(filePath.toString())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .status(DocumentStatus.PENDING)
                .uploadedAt(LocalDate.now())
                .user(user)
                .author(user.getUsername())
                .documentType(documentType)
                .title(metadata.getTitle())
                .description(metadata.getDescription())
                .language(metadata.getLanguage())
                .keywords(keywords)
                .contentType(file.getContentType())
                .content("")
                .build();
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void launchBatchJobAsync(Document document) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("documentId", document.getId().toString())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(documentProcessingJob, jobParameters);
            log.info("Batch job triggered for document: {}. Job execution id: {}",
                    document.getId(), jobExecution.getId());
        } catch (JobExecutionAlreadyRunningException | JobRestartException |
                 JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            log.error("Error triggering batch job for document: {}", document.getId(), e);
            document.setStatus(DocumentStatus.FAILED);
            document.setErrorMessage("Failed to start processing: " + e.getMessage());
            documentRepository.save(document);
        }
    }

    @Cacheable(value = "documentSummaries", key = "#id", unless = "#result == null")
    public Document getDocument(Long id, Long userId) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ApiResponseCode.Document.DOCUMENT_NOT_FOUND));

        if (!document.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied to document: " + id);
        }

        return document;
    }

    @CacheEvict(value = "documentSummaries", key = "#id")
    @Transactional
    public void deleteDocument(Long id, Long userId) {
        Document document = getDocument(id, userId);
        try {
            Files.deleteIfExists(Paths.get(document.getFilePath()));
        } catch (IOException e) {
            log.error("Error deleting file for document: {}", id, e);
        }
        searchService.deleteDocumentIndex(document.getId().toString());
        documentRepository.delete(document);
    }

    public Page<Document> getUserDocuments(Long userId, Pageable pageable) {
        Page<Document> documents = documentRepository.findByUser_Id(userId, pageable);
        documents.forEach(cacheService::cacheDocumentSummary);
        return documents;
    }

    public List<Document> getDocumentsByStatusAndUser(DocumentStatus status, Long userId) {
        List<Document> documents = documentRepository.findByUser_IdAndStatus(userId, status);
        documents.forEach(cacheService::cacheDocumentSummary);
        return documents;
    }

    public List<Document> getDocumentsByTypeAndUser(String type, Long userId) {
        List<Document> documents = searchService.findByDocumentType(type, userId);
        documents.forEach(cacheService::cacheDocumentSummary);
        return documents;
    }

    public Page<Document> searchDocumentsByKeyword(String keyword, Long userId, Pageable pageable) {
        Page<Document> documents = searchService.findByKeyword(keyword, userId, pageable);
        documents.forEach(cacheService::cacheDocumentSummary);
        return documents;
    }

} 