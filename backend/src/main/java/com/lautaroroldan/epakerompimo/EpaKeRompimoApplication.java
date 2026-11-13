package com.lautaroroldan.epakerompimo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableAsync
@SpringBootApplication
public class EpaKeRompimoApplication {

	public static void main(String[] args) {
		SpringApplication.run(EpaKeRompimoApplication.class, args);
	}

}
