package com.lautaroroldan.epakerompimo;
import org.springframework.scheduling.annotation.EnableAsync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAsync
public class EpaKeRompimoApplication {

	public static void main(String[] args) {
		SpringApplication.run(EpaKeRompimoApplication.class, args);
	}

}
