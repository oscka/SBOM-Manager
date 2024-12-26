package com.osckorea.sbommanager.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@OpenAPIDefinition(info = @Info(title = "SBOM-Manager 명세서", description = "SBOM-Manager 명세서 설명 "))
@Configuration
public class OpenApiConfig {
    @Profile({"local", "dev"})
    @Bean
    public GroupedOpenApi demoApi() {
        String[] paths = {"/sample-api/v1/test/**"};
        return GroupedOpenApi.builder().group("sample-api").pathsToMatch(paths).build();
    }
}
