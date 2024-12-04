package com.example.devprojectlabstarter.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        in = SecuritySchemeIn.HEADER,
        bearerFormat = "JWT"
)
public class OpenApiConfig {
    @Value("${server.url}")
    private String serverUrl;
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DevProjectLabStarter Document API")
                        .version("v1.0.0")
                        .description("Dev Project Lab Starter By TuanDatCoder"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local server"),
                        //new Server().url("http://178.128.99.60:8081").description("Cloud server"), // Thêm server cloud
                        new Server().url(serverUrl).description("Domain server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth")); // Thêm BearerAuth vào Security Requirement
    }
}
