package org.commonprovenance.framework.store.config;

import org.commonprovenance.framework.store.controller.resolver.DocumentArgumentResolver;
import org.commonprovenance.framework.store.controller.resolver.OrganizationArgumentResolver;
import org.commonprovenance.framework.store.controller.resolver.OrganizationDocumentArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

@Configuration
public class WebFluxConfig implements WebFluxConfigurer {
  private final OrganizationArgumentResolver organizationArgumentResolver;
  private final DocumentArgumentResolver documentArgumentResolver;
  private final OrganizationDocumentArgumentResolver organizationDocumentArgumentResolver;

  public WebFluxConfig(
      OrganizationArgumentResolver organizationArgumentResolver,
      DocumentArgumentResolver documentArgumentResolver,
      OrganizationDocumentArgumentResolver organizationDocumentArgumentResolver) {
    this.organizationArgumentResolver = organizationArgumentResolver;
    this.documentArgumentResolver = documentArgumentResolver;
    this.organizationDocumentArgumentResolver = organizationDocumentArgumentResolver;
  }

  @Override
  public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
    configurer.addCustomResolver(this.organizationArgumentResolver);
    configurer.addCustomResolver(this.documentArgumentResolver);
    configurer.addCustomResolver(this.organizationDocumentArgumentResolver);
  }
}
