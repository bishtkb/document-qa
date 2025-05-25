package com.project.document_qa.model.elasticsearch;

import com.project.document_qa.model.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.annotations.DateFormat;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.elasticsearch.annotations.Document(indexName = "documents")
@Setting(settingPath = "elasticsearch/settings.json")
public class DocumentIndex {
    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "custom_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "custom_analyzer")
    private String description;

    @Field(type = FieldType.Keyword)
    private String fileName;

    @Field(type = FieldType.Keyword)
    private String contentType;

    @Field(type = FieldType.Long)
    private Long fileSize;

    @Field(type = FieldType.Keyword)
    private String documentType;

    @Field(type = FieldType.Keyword)
    private String language;

    @Field(type = FieldType.Keyword)
    private Set<String> keywords;

    @Field(type = FieldType.Text, analyzer = "custom_analyzer")
    private String content;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Date, format = DateFormat.date_optional_time, pattern = "yyyy-MM-dd")
    private LocalDate uploadedAt;

    public static DocumentIndex fromDocument(Document document) {
        return DocumentIndex.builder()
                .id(document.getId().toString())
                .title(document.getTitle())
                .description(document.getDescription())
                .fileName(document.getFileName())
                .contentType(document.getContentType())
                .fileSize(document.getFileSize())
                .documentType(document.getDocumentType() != null ? document.getDocumentType().getName() : null)
                .language(document.getLanguage())
                .keywords(document.getKeywords().stream()
                        .map(keyword -> keyword.getName())
                        .collect(Collectors.toSet()))
                .userId(document.getUser().getId())
                .uploadedAt(document.getUploadedAt())
                .build();
    }
} 