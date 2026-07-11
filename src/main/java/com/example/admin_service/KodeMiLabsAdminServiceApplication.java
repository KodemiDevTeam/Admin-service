package com.example.admin_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableFeignClients
@SpringBootApplication
public class KodeMiLabsAdminServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(KodeMiLabsAdminServiceApplication.class, args);
	}

}
