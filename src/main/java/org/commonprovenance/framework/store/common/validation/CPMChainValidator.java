package org.commonprovenance.framework.store.common.validation;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.BadRequestException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.service.web.store.StoreWebService;

import reactor.core.publisher.Mono;

public final class CPMChainValidator {

  private static Function<Document, Mono<Void>> checkBackwardConnectorsResolvable(StoreWebService storeWebService) {
    return (Document document) -> Mono.just(document)
        .flatMapMany(MONO.liftEffectToFlux(Document::getBackwardConnectors))
        .flatMap(MONO.makeSureAsync(
            storeWebService::pingBundleId,
            BadRequestException::new,
            element -> "Invalid backwardConnector with id '" + element.getId().toString() + "'. Attribute 'referencedBundleId' is not resolvable"))
        .flatMap(MONO.makeSureAsync(
            storeWebService::pingMetaBundleId,
            BadRequestException::new,
            element -> "Invalid backwardConnector with id '" + element.getId().toString() + "'. Attribute 'referencedMetaBundleId' is not resolvable"))
        .then()
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("checkBackwardConnectorsResolvable failed!")));
  }

  private static Function<Document, Mono<Void>> checkSpecForwardConnectorsResolvable(StoreWebService storeWebService) {
    return (Document document) -> Mono.just(document)
        .flatMapMany(MONO.liftEffectToFlux(Document::getSpecForwardConnectors))
        .flatMap(MONO.makeSureAsync(
            storeWebService::pingBundleId,
            BadRequestException::new,
            element -> "Invalid specForwardConnector with id '" + element.getId().toString() + "'. Attribute 'referencedBundleId' is not resolvable"))
        .flatMap(MONO.makeSureAsync(
            storeWebService::pingMetaBundleId,
            BadRequestException::new,
            element -> "Invalid specForwardConnector with id '" + element.getId().toString() + "'. Attribute 'referencedMetaBundleId' is not resolvable"))
        .then()
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("checkSpecForwardConnectorsResolvable failed!")));
  }

  public static Function<Organization, Mono<Void>> validate(StoreWebService storeWebService) {
    return (organization) -> Mono.just(organization)
        .flatMap(MONO.liftOptionalToMono(
            Organization::getDocument,
            _ -> new InvalidValueException("Document has not been deserialized yet!")))
        .delayUntil(CPMChainValidator.checkBackwardConnectorsResolvable(storeWebService))
        .delayUntil(CPMChainValidator.checkSpecForwardConnectorsResolvable(storeWebService))
        .then();
  }
}
