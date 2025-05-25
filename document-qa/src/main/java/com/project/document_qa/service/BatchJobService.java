package com.project.document_qa.service;

import com.project.document_qa.enums.DocumentStatus;
import com.project.document_qa.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchJobService {

    private final JobLauncher jobLauncher;
    private final Job documentProcessingJob;
    private final DocumentRepository documentRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void launchDocumentProcessingJob(Long documentId) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("documentId", documentId.toString())
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(documentProcessingJob, jobParameters);
            log.info("Batch job triggered for document: {}. Job execution id: {}",
                    documentId, jobExecution.getId());
        } catch (JobExecutionAlreadyRunningException | JobRestartException |
                 JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            log.error("Error triggering batch job for document: {}", documentId, e);
            documentRepository.findById(documentId).ifPresent(document -> {
                document.setStatus(DocumentStatus.FAILED);
                document.setErrorMessage("Failed to start processing: " + e.getMessage());
                documentRepository.save(document);
            });
        }
    }
} 