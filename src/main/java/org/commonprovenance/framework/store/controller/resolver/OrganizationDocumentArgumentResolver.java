package org.commonprovenance.framework.store.controller.resolver;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;
import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import java.util.Map;
import java.util.function.Function;

import org.commonprovenance.framework.store.controller.resolver.annotation.LoadOrganizationDocument;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.BadRequestException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.utils.OrganizationUtils;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.DocumentService;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.OrganizationService;
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
public class OrganizationDocumentArgumentResolver implements HandlerMethodArgumentResolver {
  private final OrganizationService organizationService;
  private final DocumentService documentService;

  private final ProvFactory provFactory;
  private final ICpmFactory cpmFactory;
  private final ICpmProvFactory cpmProvFactory;

  public OrganizationDocumentArgumentResolver(
      OrganizationService organizationService,
      DocumentService documentService,
      ProvFactory provFactory,
      ICpmFactory cpmFactory,
      ICpmProvFactory cpmProvFactory) {
    this.organizationService = organizationService;
    this.documentService = documentService;

    this.provFactory = provFactory;
    this.cpmFactory = cpmFactory;
    this.cpmProvFactory = cpmProvFactory;
  }

  private Either<ApplicationException, Map<String, String>> getPathVariables(ServerWebExchange exchange) {
    return Either.<ApplicationException, Map<String, String>> right(exchange.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
        .flatMap(EITHER.makeSureNotNull(_ -> new BadRequestException("Request does not contain any path variables.")));
  }

  private Function<Map<String, String>, Either<ApplicationException, String>> getOrganizationIdentifier(MethodParameter parameter) {
    return (Map<String, String> pathVariables) -> Either.<ApplicationException, LoadOrganizationDocument> right(parameter.getParameterAnnotation(LoadOrganizationDocument.class))
        .flatMap(EITHER.makeSureNotNullWithMessage("MethodParameter does not contain LoadOrganization annotation."))
        .flatMap(annotation -> Either.<ApplicationException, String> right(pathVariables.get(annotation.organizationIdentifier()))
            .flatMap(EITHER.makeSureNotNull(_ -> new BadRequestException("Request path variable '" + annotation.organizationIdentifier() + "' can not be null!")))
            .flatMap(EITHER.makeSureNot(String::isBlank, _ -> new BadRequestException("Request path variable '" + annotation.organizationIdentifier() + "' can not be empty!"))));
  }

  private Function<Map<String, String>, Either<ApplicationException, String>> getDocumentIdentifier(MethodParameter parameter) {
    return (Map<String, String> pathVariables) -> Either.<ApplicationException, LoadOrganizationDocument> right(parameter.getParameterAnnotation(LoadOrganizationDocument.class))
        .flatMap(EITHER.makeSureNotNullWithMessage("MethodParameter does not contain LoadOrganization annotation."))
        .flatMap(annotation -> Either.<ApplicationException, String> right(pathVariables.get(annotation.documentIdentifier()))
            .flatMap(EITHER.makeSureNotNull(_ -> new BadRequestException("Request path variable '" + annotation.documentIdentifier() + "' can not be null!")))
            .flatMap(EITHER.makeSureNot(String::isBlank, _ -> new BadRequestException("Request path variable '" + annotation.documentIdentifier() + "' can not be empty!"))));
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(LoadOrganizationDocument.class)
        && Organization.class.isAssignableFrom(parameter.getParameterType());
  }

  @Override
  public Mono<Object> resolveArgument(MethodParameter parameter, BindingContext bindingContext, ServerWebExchange exchange) {

    return MONO.<Organization, Document, Organization> combineM(
        MONO.fromEither(this.getPathVariables(exchange)
            .flatMap(this.getOrganizationIdentifier(parameter)))
            .flatMap(this.organizationService::getOrganizationByIdentifier)
            .delayUntil(MONO.liftEffectToMono(OrganizationUtils::validateTrustedParty)),
        MONO.fromEither(this.getPathVariables(exchange)
            .flatMap(this.getDocumentIdentifier(parameter)))
            .flatMap(this.documentService::getDocumentByIdentifier)
            .flatMap(MONO.liftEffectToMono(doc -> doc.withCpmDocument(provFactory, cpmProvFactory, cpmFactory))),
        (organization, document) -> Mono.just(document)
            .flatMap(MONO.makeSureAsync(
                doc -> Mono.just(doc)
                    .flatMap(MONO.liftEffectToMono(Document::getIdentifier))
                    .flatMap(this.documentService::getOrganizationIdentifierByIdentifier)
                    .map(organization.getIdentifier()::equals),
                _ -> new BadRequestException(
                    "Document with identifier '" + document.getIdentifier().get() + "' do not belogs to Organization with identifier '" + organization.getIdentifier() + "'!")))
            .map(organization::withDocument))
        .cast(Object.class);
  }
}
