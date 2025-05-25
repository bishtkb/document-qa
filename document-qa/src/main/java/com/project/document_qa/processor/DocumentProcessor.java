package com.project.document_qa.processor;

import com.project.document_qa.model.Document;
import com.project.document_qa.enums.DocumentStatus;
import com.project.document_qa.service.DocumentSearchService;
import com.project.document_qa.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentProcessor {

    private final DocumentSearchService searchService;
    private final KafkaProducerService producerService;

    @Transactional
    public void processDocument(Document document) throws IOException {
        try {
            log.info("Processing document: {}", document.getId());
            String content = extractTextContent(document);
            Map<String, String> metadata = extractMetadata(document);
            searchService.indexDocument(document);
            document.setStatus(DocumentStatus.COMPLETED);
            producerService.sendStatusUpdate(document);
            log.info("Successfully processed document: {}", document.getId());
        } catch (Exception e) {
            log.error("Error processing document {}: {}", document.getId(), e.getMessage());
            document.setStatus(DocumentStatus.FAILED);
            producerService.sendDocumentStatus(document, e.getMessage());
            throw e;
        }
    }

    private String extractTextContent(Document document) throws IOException {
        Path filePath = Paths.get(document.getFilePath());
        String contentType = document.getContentType().toLowerCase();

        if (contentType.contains("pdf")) {
            return extractPdfContent(filePath);
        } else if (contentType.contains("word") || contentType.contains("doc")) {
            return extractWordContent(filePath);
        } else if (contentType.contains("text") || contentType.contains("plain")) {
            return new String(Files.readAllBytes(filePath));
        } else {
            throw new UnsupportedOperationException("Unsupported file type: " + contentType);
        }
    }

    private String extractPdfContent(Path filePath) throws IOException {
        try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractWordContent(Path filePath) throws IOException {
        String fileName = filePath.getFileName().toString().toLowerCase();

        if (fileName.endsWith(".docx")) {
            return extractDocxContent(filePath);
        } else if (fileName.endsWith(".doc")) {
            return extractDocContent(filePath);
        } else {
            throw new UnsupportedOperationException("Unsupported Word format: " + fileName);
        }
    }

    private String extractDocxContent(Path filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractDocContent(Path filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             HWPFDocument document = new HWPFDocument(fis);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }

    private Map<String, String> extractMetadata(Document document) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("title", document.getTitle());
        metadata.put("description", document.getDescription());
        metadata.put("fileName", document.getFileName());
        metadata.put("contentType", document.getContentType());
        metadata.put("fileSize", String.valueOf(document.getFileSize()));
        metadata.put("language", document.getLanguage());
        metadata.put("documentType", document.getDocumentType() != null ? document.getDocumentType().getName() : "UNKNOWN");
        metadata.put("uploadedAt", document.getUploadedAt().toString());
        metadata.put("processedAt", document.getProcessedAt() != null ? document.getProcessedAt().toString() : "");
        metadata.put("status", document.getStatus().name());
        metadata.put("userId", String.valueOf(document.getUserId()));

        if (document.getKeywords() != null && !document.getKeywords().isEmpty()) {
            String keywords = document.getKeywords().stream()
                    .map(keyword -> keyword.getName())
                    .collect(java.util.stream.Collectors.joining(","));
            metadata.put("keywords", keywords);
        }

        return metadata;
    }
} 