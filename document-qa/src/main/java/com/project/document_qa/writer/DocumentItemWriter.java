package com.project.document_qa.writer;

import com.project.document_qa.model.Document;
import com.project.document_qa.enums.DocumentStatus;
import com.project.document_qa.repository.DocumentRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class DocumentItemWriter implements ItemWriter<Document> {

    private final DocumentRepository documentRepository;

    public DocumentItemWriter(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public void write(Chunk<? extends Document> chunk) throws Exception {
        for (Document document : chunk) {
            document.setStatus(DocumentStatus.PROCESSING);
            documentRepository.save(document);
        }
    }
} 