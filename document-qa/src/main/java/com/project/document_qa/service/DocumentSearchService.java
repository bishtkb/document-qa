package com.project.document_qa.service;

import com.project.document_qa.model.Document;
import com.project.document_qa.model.elasticsearch.DocumentIndex;
import com.project.document_qa.repository.DocumentRepository;
import com.project.document_qa.repository.elasticsearch.DocumentSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentSearchService {
    private final DocumentSearchRepository searchRepository;
    private final DocumentRepository documentRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    public void indexDocument(Document document) {
        DocumentIndex documentIndex = DocumentIndex.fromDocument(document);
        searchRepository.save(documentIndex);
        log.info("Indexed document: {}", document.getId());
    }

    public void deleteDocumentIndex(String id) {
        searchRepository.deleteById(id);
        log.info("Deleted document index: {}", id);
    }

    public List<Document> searchDocuments(String query, Long userId) {
        String[] searchTerms = query.split("\\s+");
        Criteria criteria = new Criteria("userId").is(userId);
        Criteria contentCriteria = new Criteria();
        for (String term : searchTerms) {
            if (!term.isEmpty()) {
                contentCriteria.or("title").matches(term)
                        .or("description").matches(term)
                        .or("content").matches(term);
            }
        }
        criteria.and(contentCriteria);
        CriteriaQuery searchQuery = new CriteriaQuery(criteria);
        SearchHits<DocumentIndex> searchHits = elasticsearchOperations.search(searchQuery, DocumentIndex.class);
        List<Long> documentIds = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(doc -> Long.parseLong(doc.getId()))
                .collect(Collectors.toList());
        return documentRepository.findAllById(documentIds);
    }

    public List<Document> findByDocumentType(String documentType, Long userId) {
        List<DocumentIndex> results = searchRepository.findByUserIdAndDocumentType(userId, documentType);
        List<Long> documentIds = results.stream()
                .map(doc -> Long.parseLong(doc.getId()))
                .collect(Collectors.toList());
        return documentRepository.findAllById(documentIds);
    }

    public Page<Document> findByKeyword(String keyword, Long userId, Pageable pageable) {
        String[] searchTerms = keyword.split("\\s+");
        Page<DocumentIndex> results = searchRepository.findByUserIdAndKeywordsIn(userId, List.of(searchTerms),pageable);
        List<Long> documentIds = results.stream()
                .map(doc -> Long.parseLong(doc.getId()))
                .collect(Collectors.toList());
        return (Page<Document>) documentRepository.findAllById(documentIds);
    }
} 