package org.commonprovenance.framework.store.web.store.impl;

import org.commonprovenance.framework.store.web.store.TokenClient;
import org.commonprovenance.framework.store.web.store.client.ClientStore;
import org.springframework.stereotype.Component;

@Component
public class TokenClientImpl implements TokenClient {
  private final ClientStore client;

  public TokenClientImpl(ClientStore client) {
    this.client = client;
  }
}
