package com.project.document_qa.processor;

import com.project.document_qa.model.Document;
import com.project.document_qa.enums.DocumentStatus;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Primary
public class DocumentItemProcessor implements ItemProcessor<Document, Document> {

    private final DocumentProcessor documentProcessor;

    public DocumentItemProcessor(DocumentProcessor documentProcessor) {
        this.documentProcessor = documentProcessor;
    }

    @Override
    public Document process(Document document) {
        try {
            document.setStatus(DocumentStatus.PROCESSING);
            documentProcessor.processDocument(document);
            document.setStatus(DocumentStatus.COMPLETED);
            document.setProcessedAt(LocalDate.now());
            return document;
        } catch (Exception e) {
            document.setStatus(DocumentStatus.FAILED);
            document.setErrorMessage(e.getMessage());
            return document;
        }
    }
} 