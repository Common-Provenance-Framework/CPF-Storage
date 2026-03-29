package org.commonprovenance.framework.store.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.models.GroupedOpenApi;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;

@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", description = "Future authentication scheme for protected endpoints (*** NOT YET IMPLEMENTED ***)")
public class OpenApiConfiguration {
  // Configure OpenAPI with dynamic server URL variables for better Swagger UI
  // experience
  @Bean
  public OpenAPI customOpenAPI() {
    ServerVariable scheme = new ServerVariable()
        ._default("http")
        ._enum(java.util.List.of("http", "https"))
        .description("Protocol used to access the API");

    ServerVariable host = new ServerVariable()
        ._default("localhost:8080")
        .description("Host and port of the CPF Store service");

    Server configurableServer = new Server()
        .url("{scheme}://{host}")
        .description("Configurable server (edit variables in Swagger UI)");
    ServerVariables variables = new ServerVariables();
    variables.addServerVariable("scheme", scheme);
    variables.addServerVariable("host", host);
    configurableServer.setVariables(variables);

    return new OpenAPI()
        .info(new Info()
            .title("CPF Store API")
            .version("v1"))
        .addServersItem(configurableServer);
  }

  // Group endpoints with GroupedOpenApi to have better organization in Swagger UI
  @Bean
  public GroupedOpenApi documentsApi() {
    return GroupedOpenApi.builder()
        .group("Documents")
        .pathsToMatch("/api/v1/documents/**")
        .pathsToExclude("/api/v1/documents/meta/**")
        .build();
  }

  @Bean
  public GroupedOpenApi organizationsApi() {
    return GroupedOpenApi.builder()
        .group("Organizations")
        .pathsToMatch("/api/v1/organizations/**")
        .build();
  }

  @Bean
  public GroupedOpenApi metaApi() {
    return GroupedOpenApi.builder()
        .group("Meta")
        .pathsToMatch("/api/v1/documents/meta/**")
        .build();
  }
}
