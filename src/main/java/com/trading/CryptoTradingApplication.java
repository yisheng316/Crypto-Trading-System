package com.trading;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "Crypto Trading API",
				version = "1.0",
				description = "API for cryptocurrency trading system"
		)
)
public class CryptoTradingApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptoTradingApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofSeconds(10));
		factory.setReadTimeout(Duration.ofSeconds(10));

		return new RestTemplate(factory);
	}

}
