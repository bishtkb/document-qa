package com.project.document_qa.repository.elasticsearch;

import com.project.document_qa.model.elasticsearch.DocumentIndex;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentSearchRepository extends ElasticsearchRepository<DocumentIndex, String> {
    List<DocumentIndex> findByUserIdAndDocumentType(Long userId, String documentType);
    Page<DocumentIndex> findByUserIdAndKeywordsIn(Long userId, List<String> keywords, Pageable pageable);
} 