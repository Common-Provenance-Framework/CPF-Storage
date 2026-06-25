package org.commonprovenance.framework.store.controller.resolver;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;
import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import java.util.Map;

import org.commonprovenance.framework.store.controller.resolver.annotation.LoadOrganization;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.BadRequestException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.utils.OrganizationUtils;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.OrganizationService;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;

import io.vavr.control.Either;
import reactor.core.publisher.Mono;

@Component
public class OrganizationArgumentResolver implements HandlerMethodArgumentResolver {
  private final OrganizationService organizationService;

  public OrganizationArgumentResolver(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  private Either<ApplicationException, String> getOrganizationIdentifier(MethodParameter parameter, ServerWebExchange exchange) {
    return EITHER.combineM(
        Either.<ApplicationException, LoadOrganization> right(parameter.getParameterAnnotation(LoadOrganization.class))
            .flatMap(EITHER.makeSureNotNullWithMessage("MethodParameter does not contain LoadOrganization annotation.")),
        Either.<ApplicationException, Map<String, String>> right(exchange.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
            .flatMap(EITHER.makeSureNotNull(_ -> new BadRequestException("Request does not contain any path variables."))),
        (annotation, pathVariables) -> Either.<ApplicationException, String> right(pathVariables.get(annotation.value()))
            .flatMap(EITHER.makeSureNotNull(_ -> new BadRequestException("Request path variable '" + annotation.value() + "' can not be null!")))
            .flatMap(EITHER.makeSureNot(String::isBlank, _ -> new BadRequestException("Request path variable '" + annotation.value() + "' can not be empty!"))));
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(LoadOrganization.class)
        && Organization.class.isAssignableFrom(parameter.getParameterType());
  }

  @Override
  public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
    return MONO.fromEither(this.getOrganizationIdentifier(parameter, exchange))
        .flatMap(this.organizationService::getOrganizationByIdentifier)
        .delayUntil(MONO.liftEffectToMono(OrganizationUtils::validateTrustedParty))
        .cast(Object.class);
  }
}
