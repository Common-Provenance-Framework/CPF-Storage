package org.commonprovenance.framework.store.config;

import org.commonprovenance.framework.store.controller.resolver.OrganizationArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

@Configuration
public class WebFluxConfig implements WebFluxConfigurer {
  private final OrganizationArgumentResolver organizationArgumentResolver;

  public WebFluxConfig(OrganizationArgumentResolver organizationArgumentResolver) {
    this.organizationArgumentResolver = organizationArgumentResolver;
  }

  @Override
  public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
    configurer.addCustomResolver(this.organizationArgumentResolver);
  }
}
