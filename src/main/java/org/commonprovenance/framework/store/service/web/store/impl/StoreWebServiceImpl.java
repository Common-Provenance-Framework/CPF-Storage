package org.commonprovenance.framework.store.service.web.store.impl;

import org.commonprovenance.framework.store.service.web.store.StoreWebService;
import org.commonprovenance.framework.store.web.store.PingClient;
import org.openprovenance.prov.model.QualifiedName;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.NotFoundException;
import reactor.core.publisher.Mono;

@Service
public class StoreWebServiceImpl implements StoreWebService {
  private final PingClient pingClient;

  public StoreWebServiceImpl(
      PingClient pingClient) {
    this.pingClient = pingClient;
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
}