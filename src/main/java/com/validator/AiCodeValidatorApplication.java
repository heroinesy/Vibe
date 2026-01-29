package com.validator;

import com.validator.infrastructure.config.OpenAiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(OpenAiProperties.class)
public class AiCodeValidatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCodeValidatorApplication.class, args);
    }
}
