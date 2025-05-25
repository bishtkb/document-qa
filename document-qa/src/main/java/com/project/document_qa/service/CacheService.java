package com.project.document_qa.service;

import com.project.document_qa.dto.DocumentSummary;
import com.project.document_qa.model.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    @Cacheable(value = "documentSummaries", key = "#documentId")
    public DocumentSummary getDocumentSummary(Long documentId) {
        log.debug("Cache miss for document summary: {}", documentId);
        return null;
    }

    @CachePut(value = "documentSummaries", key = "#document.id")
    public DocumentSummary cacheDocumentSummary(Document document) {
        log.debug("Caching document summary: {}", document.getId());
        return DocumentSummary.builder()
                .id(document.getId())
                .title(document.getTitle())
                .description(document.getDescription())
                .fileName(document.getFileName())
                .documentType(document.getDocumentType() != null ? document.getDocumentType().getName() : null)
                .language(document.getLanguage())
                .keywords(document.getKeywords().stream()
                        .map(keyword -> keyword.getName())
                        .collect(Collectors.toSet()))
                .status(document.getStatus())
                .uploadedAt(document.getUploadedAt())
                .processedAt(document.getProcessedAt())
                .fileSize(document.getFileSize())
                .build();
    }

    @CacheEvict(value = "documentSummaries", key = "#documentId")
    public void evictDocumentSummary(Long documentId) {
        log.debug("Evicting document summary from cache: {}", documentId);
    }

    @Cacheable(value = "searchResults", key = "#query + ':' + #userId")
    public List<Long> getCachedSearchResults(String query, Long userId) {
        log.debug("Cache miss for search results: {}:{}", query, userId);
        return null;
    }

    @CachePut(value = "searchResults", key = "#query + ':' + #userId")
    public List<Long> cacheSearchResults(String query, Long userId, List<Long> documentIds) {
        log.debug("Caching search results: {}:{}", query, userId);
        return documentIds;
    }

    @Cacheable(value = "documentMetadata", key = "#documentId + ':metadata'")
    public String getCachedDocumentMetadata(Long documentId) {
        log.debug("Cache miss for document metadata: {}", documentId);
        return null;
    }

    @CachePut(value = "documentMetadata", key = "#documentId + ':metadata'")
    public String cacheDocumentMetadata(Long documentId, String metadata) {
        log.debug("Caching document metadata: {}", documentId);
        return metadata;
    }

    @CacheEvict(value = "documentMetadata", key = "#documentId + ':metadata'")
    public void evictDocumentMetadata(Long documentId) {
        log.debug("Evicting document metadata from cache: {}", documentId);
    }
} 