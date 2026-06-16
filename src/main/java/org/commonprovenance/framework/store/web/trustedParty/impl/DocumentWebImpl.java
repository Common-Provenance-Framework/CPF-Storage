package org.commonprovenance.framework.store.web.trustedParty.impl;

import java.util.Map;
import java.util.Optional;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.factory.DocumentFactory;
import org.commonprovenance.framework.store.web.trustedParty.DocumentWeb;
import org.commonprovenance.framework.store.web.trustedParty.client.ClientTrustedParty;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.DocumentTPResponseDTO;
import org.openprovenance.prov.model.QualifiedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class DocumentWebImpl implements DocumentWeb {
  private final String LOG_PREFIX = "DocumentWebImpl: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(DocumentWebImpl.class);

  private final ClientTrustedParty client;

  public DocumentWebImpl(
      ClientTrustedParty client) {
    this.client = client;
  }

  @Override
  public Mono<Document> getById(
      String organizationIdentifer,
      QualifiedName bundleIdentifier,
      Format documentFormat,
      Optional<String> optTrustedPartyBaseUrl) {
    String uri = "organizations/" + organizationIdentifer + "/documents/" + bundleIdentifier.getUri() + "/" + documentFormat.toString();
    return optTrustedPartyBaseUrl
        .map(this.client.sendCustomGetOneRequest(uri, DocumentTPResponseDTO.class, Map.of()))
        .orElse(this.client.sendGetOneRequest(uri, DocumentTPResponseDTO.class, Map.of()))
        .map(DocumentFactory::build)
        .doOnSuccess(_ -> LOGGER.trace(LOG_PREFIX + "Document with identifier '" + bundleIdentifier.getUri() + "' has been fetched."))
        .doOnError(throwable -> {
          if (throwable instanceof NotFoundException notFound)
            LOGGER.trace(LOG_PREFIX + notFound.getMessage());
          else
            LOGGER.error(LOG_PREFIX + "Document with identifier '" + bundleIdentifier.getUri() + "' has not been fetched!\n" + throwable.getMessage());
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Document with identifier '" + bundleIdentifier.getUri() + "' has been fetched!")));
  }

}
