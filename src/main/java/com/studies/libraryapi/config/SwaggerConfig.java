package com.studies.libraryapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
public class SwaggerConfig {

    public static final String basePackageDocket = "com.studies.libraryapi.api.resource";

    public static final String titleInfo = "Library API";
    public static final String descriptionInfo = "A project for book rent control";
    public static final String versionInfo = "1.0";

    public static final String nameContact = "Rafhael Souza";
    public static final String urlContact = "https://github.com/RafhaelSouza/library-api-studies";
    public static final String emailContact = "rafhael.dev@gmail.com";

    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis( RequestHandlerSelectors.basePackage(basePackageDocket) )
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(titleInfo)
                .description(descriptionInfo)
                .version(versionInfo)
                .contact(contact())
                .build();
    }

    private Contact contact() {
        return new Contact(nameContact, urlContact, emailContact);
    }

}
