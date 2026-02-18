package org.commonprovenance.framework.store.web.store.impl;

import org.commonprovenance.framework.store.web.store.TokenClient;
import org.commonprovenance.framework.store.web.store.client.Client;
import org.springframework.stereotype.Component;

@Component
public class TokenClientImpl implements TokenClient {
  private final Client client;

  public TokenClientImpl(Client client) {
    this.client = client;
  }
}
