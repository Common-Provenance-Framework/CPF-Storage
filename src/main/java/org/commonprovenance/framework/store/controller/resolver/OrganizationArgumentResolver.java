package org.commonprovenance.framework.store.controller.resolver;

import java.util.Map;

import org.commonprovenance.framework.store.controller.resolver.annotation.LoadOrganization;
import org.commonprovenance.framework.store.exceptions.BadRequestException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.OrganizationService;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class OrganizationArgumentResolver implements HandlerMethodArgumentResolver {
  private final OrganizationService organizationService;

  public OrganizationArgumentResolver(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(LoadOrganization.class)
        && Organization.class.isAssignableFrom(parameter.getParameterType());
  }

  @Override
  public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
    LoadOrganization annotation = parameter.getParameterAnnotation(LoadOrganization.class);
    if (annotation == null) {
      return Mono.error(new InternalApplicationException("MethodParameter does not contain LoadOrganization anotation."));
    }

    Map<String, String> pathVariables = exchange.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
    if (pathVariables == null) {
      return Mono.error(new BadRequestException("Request does not contain any path variables."));
    }

    String identifier = pathVariables.get(annotation.value());
    if (identifier == null || identifier.isBlank()) {
      return Mono.error(new BadRequestException("Request path variable '" + annotation.value() + "' can not be null or empty!"));
    }

    return this.organizationService.getOrganizationByIdentifier(identifier).cast(Object.class);
  }
}
