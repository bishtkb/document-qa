package com.project.document_qa.service;

import com.project.document_qa.enums.ApiResponseCode;
import com.project.document_qa.exception.CommonApiException;
import com.project.document_qa.model.Document;
import com.project.document_qa.enums.DocumentStatus;
import com.project.document_qa.model.kafka.DocumentMessage;
import com.project.document_qa.processor.DocumentProcessor;
import com.project.document_qa.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final DocumentRepository documentRepository;
    private final DocumentProcessor documentProcessor;
    private final KafkaProducerService producerService;

    @KafkaListener(topics = "document.ingestion", groupId = "document-ingestion-group")
    @Transactional
    public void consumeIngestionMessage(DocumentMessage message, Acknowledgment acknowledgment) {
        try {
            log.info("Received ingestion message: {}", message);
            
            Document document = documentRepository.findById(message.getDocumentId())
                    .orElseThrow(() -> new CommonApiException(ApiResponseCode.Document.DOCUMENT_NOT_FOUND));
            document.setStatus(DocumentStatus.PROCESSING);
            document.setProcessedAt(LocalDate.now());
            documentRepository.save(document);
            producerService.sendDocumentForProcessing(document);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing ingestion message: {}", e.getMessage());
            acknowledgment.nack(Duration.ofSeconds(1));
        }
    }

    @KafkaListener(topics = "document.processing", groupId = "document-processing-group")
    @Transactional
    public void consumeProcessingMessage(DocumentMessage message, Acknowledgment acknowledgment) {
        try {
            log.info("Received processing message: {}", message);
            
            Document document = documentRepository.findById(message.getDocumentId())
                    .orElseThrow(() -> new CommonApiException(ApiResponseCode.Document.DOCUMENT_NOT_FOUND));
            documentProcessor.processDocument(document);
            document.setStatus(DocumentStatus.COMPLETED);
            document.setProcessedAt(LocalDate.now());
            documentRepository.save(document);
            producerService.sendDocumentStatus(document, null);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing document: {}", e.getMessage());
            Document document = documentRepository.findById(message.getDocumentId())
                    .orElseThrow(() -> new RuntimeException("Document not found: " + message.getDocumentId()));
            document.setStatus(DocumentStatus.FAILED);
            document.setErrorMessage(e.getMessage());
            documentRepository.save(document);
            producerService.sendDocumentStatus(document, e.getMessage());
            acknowledgment.acknowledge();
        }
    }

    @KafkaListener(topics = "document.status", groupId = "document-status-group")
    public void consumeStatusMessage(DocumentMessage message, Acknowledgment acknowledgment) {
        try {
            log.info("Received status message: {}", message);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing status message: {}", e.getMessage());
            acknowledgment.nack(Duration.ofSeconds(1));
        }
    }
} 