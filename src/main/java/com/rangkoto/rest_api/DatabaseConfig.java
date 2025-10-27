package com.rangkoto.rest_api;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.rangkoto.rest_api")
public class DatabaseConfig {
}
