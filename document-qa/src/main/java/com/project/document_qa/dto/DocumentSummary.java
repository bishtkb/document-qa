package com.project.document_qa.dto;

import com.project.document_qa.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentSummary implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String description;
    private String fileName;
    private String documentType;
    private String language;
    private Set<String> keywords;
    private DocumentStatus status;
    private LocalDate uploadedAt;
    private LocalDate processedAt;
    private Long fileSize;
} 