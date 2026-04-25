package org.commonprovenance.framework.store.service.web.store.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import org.commonprovenance.framework.store.common.utils.CpmDocumentUtils;
import org.commonprovenance.framework.store.exceptions.ConstraintException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.service.web.store.StoreWebService;
import org.commonprovenance.framework.store.web.store.PingClient;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.QualifiedName;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class StoreWebServiceImpl implements StoreWebService {
  private final PingClient pingClient;

  public StoreWebServiceImpl(
      PingClient pingClient) {
    this.pingClient = pingClient;
  }

  private String buildHeader(String methodName, String message) {
    return "[StoreWebService].[" + methodName + "].(" + message + ")";
  }

  @Override
  public Mono<Boolean> pingUrl(String url) {
    return this.pingClient.pingByUrl(url)
        .thenReturn(true)
        .onErrorReturn(NotFoundException.class, false);
  }

  @Override
  public Mono<Boolean> pingQualifiedName(QualifiedName qn) {
    return this.pingClient.pingByUrl(qn.getNamespaceURI() + qn.getLocalPart())
        .thenReturn(true)
        .onErrorReturn(NotFoundException.class, false);
  }

  @Override
  public Mono<Boolean> pingBundleId(Entity connector) {
    return MONO.makeSureNotNull(connector)
        .flatMap(MONO.liftEffectToMono(CpmDocumentUtils.FUNCTIONAL::getCpmReferencedBundleId))
        .onErrorMap(ApplicationExceptionFactory.build(ConstraintException::new, "Can not get 'referencedBundleId'"))
        .flatMap(this::pingQualifiedName)
        .onErrorMap(ApplicationExceptionFactory.header(buildHeader("pingBundleId", "ConnectorId: " + connector.getId())));
  }

  @Override
  public Mono<Boolean> pingMetaBundleId(Entity connector) {
    return MONO.makeSureNotNull(connector)
        .flatMap(MONO.liftEffectToMono(CpmDocumentUtils.FUNCTIONAL::getCpmReferencedMetaBundleId))
        .onErrorMap(ApplicationExceptionFactory.build(ConstraintException::new, "Can not get 'referencedMetaBundleId'"))
        .flatMap(this::pingQualifiedName)
        .onErrorMap(ApplicationExceptionFactory.header(buildHeader("pingMetaBundleId", "ConnectorId: " + connector.getId())));
  }
}
