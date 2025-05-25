package com.project.document_qa.repository;

import com.project.document_qa.model.Document;
import com.project.document_qa.enums.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByStatus(DocumentStatus status);
    Page<Document> findByUser_Id(Long userId, Pageable pageable);
    List<Document> findByUser_IdAndStatus(Long userId, DocumentStatus status);


    @Query("SELECT d FROM Document d JOIN d.keywords k WHERE k.name = :keyword AND d.user.id = :userId")
    List<Document> findByUser_IdAndKeywords_Name(@Param("keyword") String keyword, @Param("userId") Long userId);

    @Query("SELECT d FROM Document d WHERE d.title LIKE %:searchTerm% OR d.description LIKE %:searchTerm% AND d.user.id = :userId")
    List<Document> searchByTitleOrDescription(@Param("searchTerm") String searchTerm, @Param("userId") Long userId);

}