package org.commonprovenance.framework.store.service.web.store.impl;

import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;

import org.commonprovenance.framework.store.common.utils.ProvDocumentUtils;
import org.commonprovenance.framework.store.exceptions.ConstraintException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.service.web.store.StoreWebService;
import org.commonprovenance.framework.store.web.store.StoreWeb;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.QualifiedName;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class StoreWebServiceImpl implements StoreWebService {
  private final StoreWeb storeWeb;

  public StoreWebServiceImpl(
      StoreWeb storeWeb) {
    this.storeWeb = storeWeb;
  }

  @Override
  public Mono<Boolean> pingUrl(String url) {
    return this.storeWeb.pingByResourcePath(url)
        .thenReturn(true)
        .onErrorReturn(NotFoundException.class, false);
  }

  @Override
  public Mono<Boolean> pingQualifiedName(QualifiedName qn) {
    return this.storeWeb.pingByResourcePath(qn.getNamespaceURI() + qn.getLocalPart())
        .thenReturn(true)
        .onErrorReturn(NotFoundException.class, false);
  }

  @Override
  public Mono<Boolean> pingBundleId(Entity connector) {
    return MONO.makeSureNotNull(connector)
        .flatMap(MONO.liftEffectToMono(ProvDocumentUtils::getCpmReferencedBundleId))
        .onErrorMap(ApplicationExceptionFactory.build(ConstraintException::new, "Can not get 'referencedBundleId'"))
        .flatMap(this::pingQualifiedName)
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Ping bundle id '" + connector.getId() + "'' failed!")));
  }

  @Override
  public Mono<Token> getBundleToken(Entity connector) {
    return MONO.makeSureNotNull(connector)
        .flatMap(MONO.liftEffectToMono(ProvDocumentUtils::getCpmReferencedBundleId))
        .onErrorMap(ApplicationExceptionFactory.build(ConstraintException::new, "Can not get 'referencedBundleId'"))
        .map(qn -> qn.getUri())
        .flatMap(this.storeWeb::getTokenByResourcePath)
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Get Token '" + connector.getId() + "'' failed!")));
  }

  @Override
  public Mono<Boolean> pingMetaBundleId(Entity connector) {
    return MONO.makeSureNotNull(connector)
        .flatMap(MONO.liftEffectToMono(ProvDocumentUtils::getCpmReferencedMetaBundleId))
        .onErrorMap(ApplicationExceptionFactory.build(ConstraintException::new, "Can not get 'referencedMetaBundleId'"))
        .flatMap(this::pingQualifiedName)
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Ping meta bundle id '" + connector.getId() + "'' failed!")));
  }
}
