package com.project.document_qa.model.kafka;

import com.project.document_qa.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMessage {
    private Long documentId;
    private String fileName;
    private String filePath;
    private Long userId;
    private DocumentStatus status;
    private LocalDateTime timestamp;
    private String errorMessage;
    private String processingNode;
    private String action;
    private String nodeId;
} 