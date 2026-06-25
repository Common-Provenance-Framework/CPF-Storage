package org.commonprovenance.framework.store.controller.resolver;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;
import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import java.util.Map;

import org.commonprovenance.framework.store.controller.resolver.annotation.LoadDocument;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.BadRequestException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.DocumentService;
import org.openprovenance.prov.model.ProvFactory;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.BindingContext;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver;
import org.springframework.web.server.ServerWebExchange;

import cz.muni.fi.cpm.model.ICpmFactory;
import cz.muni.fi.cpm.model.ICpmProvFactory;
import io.vavr.control.Either;
import reactor.core.publisher.Mono;

@Component
public class DocumentArgumentResolver implements HandlerMethodArgumentResolver {
  private final DocumentService ducumentService;

  private final ProvFactory provFactory;
  private final ICpmFactory cpmFactory;
  private final ICpmProvFactory cpmProvFactory;

  public DocumentArgumentResolver(
      DocumentService ducumentService,
      ProvFactory provFactory,
      ICpmFactory cpmFactory,
      ICpmProvFactory cpmProvFactory) {
    this.ducumentService = ducumentService;

    this.provFactory = provFactory;
    this.cpmFactory = cpmFactory;
    this.cpmProvFactory = cpmProvFactory;
  }

  private Either<ApplicationException, String> getDocumentIdentifier(MethodParameter parameter, ServerWebExchange exchange) {
    return EITHER.combineM(
        Either.<ApplicationException, LoadDocument> right(parameter.getParameterAnnotation(LoadDocument.class))
            .flatMap(EITHER.makeSureNotNullWithMessage("MethodParameter does not contain LoadDocument annotation.")),
        Either.<ApplicationException, Map<String, String>> right(exchange.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
            .flatMap(EITHER.makeSureNotNull(_ -> new BadRequestException("Request does not contain any path variables."))),
        (annotation, pathVariables) -> Either.<ApplicationException, String> right(pathVariables.get(annotation.value()))
            .flatMap(EITHER.makeSureNotNull(_ -> new BadRequestException("Request path variable '" + annotation.value() + "' can not be null!")))
            .flatMap(EITHER.makeSureNot(String::isBlank, _ -> new BadRequestException("Request path variable '" + annotation.value() + "' can not be empty!"))));
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(LoadDocument.class)
        && Document.class.isAssignableFrom(parameter.getParameterType());
  }

  @Override
  public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {
    return MONO.fromEither(this.getDocumentIdentifier(parameter, exchange))
        .flatMap(this.ducumentService::getDocumentByIdentifier)
        .flatMap(MONO.liftEffectToMono(document -> document.withCpmDocument(this.provFactory, this.cpmProvFactory, this.cpmFactory)))
        .cast(Object.class);
  }
}
