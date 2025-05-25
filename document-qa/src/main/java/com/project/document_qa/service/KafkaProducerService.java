package com.project.document_qa.service;

import com.project.document_qa.model.Document;
import com.project.document_qa.enums.DocumentStatus;
import com.project.document_qa.model.kafka.DocumentMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendDocumentForIngestion(Document document) {
        DocumentMessage message = DocumentMessage.builder()
                .documentId(document.getId())
                .action("INGEST")
                .status(DocumentStatus.PENDING)
                .build();

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("document.ingestion", message);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent document {} for ingestion", document.getId());
            } else {
                log.error("Failed to send document {} for ingestion: {}", document.getId(), ex.getMessage());
            }
        });
    }

    public void sendDocumentForProcessing(Document document) {
        DocumentMessage message = DocumentMessage.builder()
                .documentId(document.getId())
                .action("PROCESS")
                .status(DocumentStatus.PROCESSING)
                .build();

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("document.processing", message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent document {} for processing", document.getId());
            } else {
                log.error("Failed to send document {} for processing: {}", document.getId(), ex.getMessage());
            }
        });
    }

    public void sendStatusUpdate(Document document) {
        DocumentMessage message = DocumentMessage.builder()
                .documentId(document.getId())
                .action("UPDATE_STATUS")
                .status(document.getStatus())
                .build();

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("document.status", message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent status update for document {}: {}", document.getId(), document.getStatus());
            } else {
                log.error("Failed to send status update for document {}: {}", document.getId(), ex.getMessage());
            }
        });
    }
    public void sendDocumentStatus(Document document, String errorMessage) {
        DocumentMessage message = DocumentMessage.builder()
                .documentId(document.getId())
                .action("UPDATE_STATUS")
                .status(document.getStatus())
                .errorMessage(errorMessage)
                .build();

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send("document.status", message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent document status for document {}: {}", document.getId(), document.getStatus());
            } else {
                log.error("Failed to send document status for document {}: {}", document.getId(), ex.getMessage());
            }
        });
    }

} 