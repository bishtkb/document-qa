package com.project.document_qa.config;

import com.project.document_qa.model.Document;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableBatchProcessing
@EnableTransactionManagement
public class BatchConfig {

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
        executor.setConcurrencyLimit(10);
        return executor;
    }

    @Bean
    @Qualifier("batchTransactionTemplate")
    public TransactionTemplate batchTransactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRES_NEW");
        transactionTemplate.setIsolationLevelName("ISOLATION_READ_COMMITTED");
        return transactionTemplate;
    }

    @Bean
    @Qualifier("documentTransactionTemplate")
    public TransactionTemplate documentTransactionTemplate(PlatformTransactionManager transactionManager) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehaviorName("PROPAGATION_REQUIRES_NEW");
        transactionTemplate.setIsolationLevelName("ISOLATION_READ_COMMITTED");
        return transactionTemplate;
    }

    @Bean
    public Step documentIngestionStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<Document> itemReader,
            ItemProcessor<Document, Document> itemProcessor,
            ItemWriter<Document> itemWriter) {
        return new StepBuilder("documentIngestionStep", jobRepository)
                .<Document, Document>chunk(100, transactionManager)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public Job documentIngestionJob(JobRepository jobRepository, Step documentIngestionStep) {
        return new JobBuilder("documentIngestionJob", jobRepository)
                .start(documentIngestionStep)
                .build();
    }
} 