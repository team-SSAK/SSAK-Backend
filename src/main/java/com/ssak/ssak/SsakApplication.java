package com.ssak.ssak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class SsakApplication {

	public static void main(String[] args) {
		SpringApplication.run(SsakApplication.class, args);
	}
}