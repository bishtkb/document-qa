package com.project.document_qa.controller;

import com.project.document_qa.enums.ApiResponseCode;
import com.project.document_qa.exception.ResourceNotFoundException;
import com.project.document_qa.model.User;
import com.project.document_qa.dto.DocumentUploadRequest;
import com.project.document_qa.model.Document;
import com.project.document_qa.enums.DocumentStatus;
import com.project.document_qa.repository.UserRepository;
import com.project.document_qa.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = "APIs for document upload and management")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {
    private final DocumentService documentService;
    private final UserRepository userRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @Operation(summary = "Upload a document", description = "Upload a document with metadata for processing")
    @Transactional
    public ResponseEntity<Document> uploadDocument(@RequestParam("file") MultipartFile file,
                                                   @ModelAttribute DocumentUploadRequest metadata,
                                                   @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        if (userDetails == null) {
            throw new ResourceNotFoundException(ApiResponseCode.User.USER_NOT_FOUND);
        }
        Document document = documentService.uploadDocument(file, metadata, userDetails);
        return ResponseEntity.ok(document);
    }

    @GetMapping
    @Operation(summary = "Get user's documents", description = "Retrieve all documents for the current user")
    public ResponseEntity<Page<Document>> getUserDocuments(@AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {

        if (userDetails == null) {
            throw new AccessDeniedException("User must be authenticated to search documents");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(ApiResponseCode.User.USER_NOT_FOUND));

        Page<Document> documents = documentService.getUserDocuments(user.getId(), pageable);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get documents by status", description = "Retrieve documents filtered by processing status")
    public ResponseEntity<List<Document>> getDocumentsByStatus(@PathVariable DocumentStatus status,
                                                               @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("User must be authenticated to search documents");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(ApiResponseCode.User.USER_NOT_FOUND));

        List<Document> documents = documentService.getDocumentsByStatusAndUser(status, user.getId());
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID", description = "Retrieve a specific document by its ID")
    public ResponseEntity<Document> getDocument(@PathVariable Long id,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("User must be authenticated to search documents");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(ApiResponseCode.User.USER_NOT_FOUND));

        Document document = documentService.getDocument(id, user.getId());
        return ResponseEntity.ok(document);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
    @Operation(summary = "Delete document", description = "Delete a specific document by its ID")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("User must be authenticated to search documents");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(ApiResponseCode.User.USER_NOT_FOUND));

        documentService.deleteDocument(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get documents by type", description = "Retrieve documents filtered by document type")
    public ResponseEntity<List<Document>> getDocumentsByType(@PathVariable String type,
                                                             @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("User must be authenticated to search documents");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(ApiResponseCode.User.USER_NOT_FOUND));

        List<Document> documents = documentService.getDocumentsByTypeAndUser(type, user.getId());
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/search")
    @Operation(summary = "Search documents", description = "Search documents by keywords or query")
    public ResponseEntity<Page<Document>> searchDocuments(@RequestParam(required = false, defaultValue = "") String keyword,
                                                          @RequestParam(required = false, defaultValue = "") String query,
                                                          @AuthenticationPrincipal UserDetails userDetails,
                                                          Pageable pageable) {
        if (userDetails == null) {
            throw new AccessDeniedException("User must be authenticated to search documents");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(ApiResponseCode.User.USER_NOT_FOUND));

        Page<Document> documents;
        String searchTerm = !keyword.isEmpty() ? keyword : query;
        if (searchTerm.isEmpty()) {
            documents = documentService.getUserDocuments(user.getId(), pageable);
        } else {
            documents = documentService.searchDocumentsByKeyword(searchTerm, user.getId(), pageable);
        }
        return ResponseEntity.ok(documents);
    }
}