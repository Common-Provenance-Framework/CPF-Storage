package org.commonprovenance.framework.store.common.validation;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;
import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import java.util.function.Function;

import org.commonprovenance.framework.store.common.utils.ProvDocumentUtils;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.BadRequestException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.HashFunction;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.service.web.store.StoreWebService;
import org.openprovenance.prov.model.Entity;

import io.vavr.control.Either;
import reactor.core.publisher.Mono;

public final class CPMChainValidator {

  private static Function<Entity, Mono<Entity>> checkBundleIdResolvable(StoreWebService storeWebService) {
    return (Entity connector) -> Mono.just(connector)
        .flatMap(MONO.makeSureAsync(
            storeWebService::pingBundleId,
            BadRequestException::new,
            element -> "Invalid connector with id '" + element.getId().toString() + "'. Attribute 'referencedBundleId' is not resolvable"));
  }

  private static Function<Entity, Mono<Entity>> checkMetaBundleIdResolvable(StoreWebService storeWebService) {
    return (Entity connector) -> Mono.just(connector)
        .flatMap(MONO.makeSureAsync(
            storeWebService::pingMetaBundleId,
            BadRequestException::new,
            element -> "Invalid connector with id '" + element.getId().toString() + "'. Attribute 'referencedMetaBundleId' is not resolvable"));
  }

  private static Function<Token, Either<ApplicationException, Token>> checkHashAlg(Entity connector) {
    return (Token token) -> EITHER.combineM(
        token.getHashFunction(),
        ProvDocumentUtils.getCpmHashAlg(connector),
        (HashFunction expected, HashFunction actual) -> actual.equals(expected)
            ? Either.right(token)
            : Either.left(new BadRequestException("Invalid connector with id '" + connector.getId().toString() + "'. Invalid hashAlg attribute value: '" + actual.toString())));
  }

  private static Function<Token, Either<ApplicationException, Token>> checkHashValue(Entity connector) {
    return (Token token) -> EITHER.combineM(
        token.getDocumentDigest(),
        ProvDocumentUtils.getCpmReferencedBundleHashValue(connector),
        (String expected, String actual) -> actual.equals(expected)
            ? Either.right(token)
            : Either.left(new BadRequestException(
                "Invalid connector with id '" + connector.getId().toString() + "'. Invalid referencedBundleHashValue attribute value: '" + actual.toString())));
  }

  private static Function<Token, Either<ApplicationException, Void>> checkHash(Entity connector) {
    return (Token token) -> Either.<ApplicationException, Token> right(token)
        .flatMap(CPMChainValidator.checkHashAlg(connector))
        .flatMap(CPMChainValidator.checkHashValue(connector))
        .mapToVoid();
  }

  private static Function<Entity, Mono<Entity>> checkConnectorHash(StoreWebService storeWebService) {
    return (Entity connector) -> Mono.just(connector)
        .flatMap(storeWebService::getBundleToken)
        .delayUntil(MONO.liftEffectToMono(CPMChainValidator.checkHash(connector)))
        .thenReturn(connector);
  }

  private static Function<Document, Mono<Void>> checkBackwardConnectorsResolvable(StoreWebService storeWebService) {
    return (Document document) -> Mono.just(document)
        .flatMapMany(MONO.liftEffectToFlux(Document::getBackwardConnectors))
        .delayUntil(CPMChainValidator.checkBundleIdResolvable(storeWebService))
        .delayUntil(CPMChainValidator.checkMetaBundleIdResolvable(storeWebService))
        .delayUntil(CPMChainValidator.checkConnectorHash(storeWebService))
        .then()
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("checkBackwardConnectorsResolvable failed!")));
  }

  private static Function<Document, Mono<Void>> checkSpecForwardConnectorsResolvable(StoreWebService storeWebService) {
    return (Document document) -> Mono.just(document)
        .flatMapMany(MONO.liftEffectToFlux(Document::getSpecForwardConnectors))
        .delayUntil(CPMChainValidator.checkBundleIdResolvable(storeWebService))
        .delayUntil(CPMChainValidator.checkMetaBundleIdResolvable(storeWebService))
        .delayUntil(CPMChainValidator.checkConnectorHash(storeWebService))
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
