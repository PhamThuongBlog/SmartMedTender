package com.medbid.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI medTenderOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SmartMedTender API")
                        .description("Hệ thống Quản lý và Chuẩn bị Hồ sơ Dự thầu Thiết bị Y tế Thông minh")
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("MedTender Team")
                                .email("admin@medtender.vn"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://medtender.vn")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .components(new Components()
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .name("Bearer")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
