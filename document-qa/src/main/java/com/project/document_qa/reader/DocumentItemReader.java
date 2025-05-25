package com.project.document_qa.reader;

import com.project.document_qa.model.Document;
import com.project.document_qa.enums.DocumentStatus;
import com.project.document_qa.repository.DocumentRepository;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
public class DocumentItemReader implements ItemReader<Document> {
    private final DocumentRepository documentRepository;
    private Iterator<Document> documentIterator;

    @Autowired
    public DocumentItemReader(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public Document read() {
        if (documentIterator == null || !documentIterator.hasNext()) {
            List<Document> pendingDocuments = documentRepository.findByStatus(DocumentStatus.PENDING);
            documentIterator = pendingDocuments.iterator();
            if (!documentIterator.hasNext()) {
                return null;
            }
        }
        return documentIterator.next();
    }
} 