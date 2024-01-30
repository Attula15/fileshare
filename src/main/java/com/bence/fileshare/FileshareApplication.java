package com.bence.fileshare;

import com.bence.fileshare.service.InitializerService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FileshareApplication {

	@Autowired
	private InitializerService initializerService;

	public static void main(String[] args) {
		SpringApplication.run(FileshareApplication.class, args);
	}

	@PostConstruct
	private void init() throws Exception {
		initializerService.getRootDirectory();
	}
}
