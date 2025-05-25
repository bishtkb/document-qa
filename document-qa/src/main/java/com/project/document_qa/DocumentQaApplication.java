package com.project.document_qa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class DocumentQaApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentQaApplication.class, args);
	}

}
