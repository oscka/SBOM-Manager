package com.osckorea.sbomgr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SbomgrApplication {

	public static void main(String[] args) {
		SpringApplication.run(SbomgrApplication.class, args);
	}

}
