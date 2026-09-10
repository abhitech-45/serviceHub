package com.servicehubai.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Campus Services Hub API",
                version = "v1",
                description = "Versioned student support, campus services, authentication, and administration API."),
        servers = @Server(url = "/api/v1", description = "ServiceHub API v1"))
public class OpenApiConfig {
}