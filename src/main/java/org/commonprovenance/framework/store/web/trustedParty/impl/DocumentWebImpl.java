package org.commonprovenance.framework.store.web.trustedParty.impl;

import java.util.Map;
import java.util.Optional;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.model.factory.DocumentFactory;
import org.commonprovenance.framework.store.web.client.Client;
import org.commonprovenance.framework.store.web.trustedParty.DocumentWeb;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.ErrorResponseDTO;
import org.openprovenance.prov.model.QualifiedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;

import reactor.core.publisher.Mono;

@Component
public class DocumentWebImpl implements DocumentWeb {
  private final String LOG_PREFIX = "DocumentWebImpl: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentWebImpl.class);

  private final Client client;

  public DocumentWebImpl(
      Client client) {
    this.client = client;
  }

  @Override
  public Mono<Document> getById(
      String organizationIdentifer,
      QualifiedName bundleIdentifier,
      GraphFormat documentFormat,
      Optional<String> optTrustedPartyBaseUrl) {
    String uri = "organizations/" + organizationIdentifer + "/documents/" + bundleIdentifier.getLocalPart() + "/" + documentFormat.toString();
    return this.client
        .sendGetOneRequest(this.client.trustedParty(optTrustedPartyBaseUrl), uri, DocumentResponseDTO.class, Map.of())
        .map(DocumentFactory::build)
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX
            + "Document with identifier '"
            + bundleIdentifier.getUri()
            + "' has been fetched from Trusted Party at url '"
            + this.getTrustedPartyUrlOrDefault(optTrustedPartyBaseUrl)
            + "'."))
        .doOnError(throwable -> {
          LOGGER.error(LOG_PREFIX
              + "Document with identifier '"
              + bundleIdentifier.getUri()
              + "' has not been fetched from Trusted Party at url '"
              + this.getTrustedPartyUrlOrDefault(optTrustedPartyBaseUrl)
              + "'!\n"
              + throwable.getMessage());
          if (throwable instanceof WebClientResponseException wcrException)
            LOGGER.trace(LOG_PREFIX + wcrException.getResponseBodyAsString());
        })
        .onErrorMap(NotFound.class, notFound -> {
          ErrorResponseDTO response = notFound.getResponseBodyAs(ErrorResponseDTO.class);
          return new NotFoundException("TrustedParty responded with status '" + response.status() + "' and message '" + response.message() + "'!");
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Document with identifier '" + bundleIdentifier.getUri() + "' has been fetched!")));
  }

  private String getTrustedPartyUrlOrDefault(Optional<String> maybyUrl) {
    return maybyUrl.orElse(this.client.getDefaultTrustedPartyUrl());
  }
}
