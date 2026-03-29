package org.commonprovenance.framework.store.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.models.GroupedOpenApi;

@Configuration
public class OpenApiConfiguration {
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
