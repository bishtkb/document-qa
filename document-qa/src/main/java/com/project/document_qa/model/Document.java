package com.project.document_qa.model;

import com.project.document_qa.enums.DocumentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String filePath;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    @Column(nullable = false)
    private String author;

    @Column
    private String errorMessage;

    @Column(nullable = false)
    @Field(type = FieldType.Date, format = DateFormat.date_optional_time, pattern = "yyyy-MM-dd")
    private LocalDate uploadedAt;

    @Column
    @Field(type = FieldType.Date, format = DateFormat.date_optional_time, pattern = "yyyy-MM-dd")
    private LocalDate processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_type", referencedColumnName = "id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private DocumentType documentType;

    @Column
    private String title;

    @Column(length = 1000)
    private String description;

    @Column
    private String language;

    @Column
    private Integer pageCount;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "document_keywords",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "keyword_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Set<Keyword> keywords = new HashSet<>();

    public void addKeyword(Keyword keyword) {
        keywords.add(keyword);
        keyword.getDocuments().add(this);
    }

    public void removeKeyword(Keyword keyword) {
        keywords.remove(keyword);
        keyword.getDocuments().remove(this);
    }

    @Transient
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
} 