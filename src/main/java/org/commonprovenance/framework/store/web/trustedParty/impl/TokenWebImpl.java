package org.commonprovenance.framework.store.web.trustedParty.impl;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.NotFoundException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.factory.ModelFactory;
import org.commonprovenance.framework.store.web.trustedParty.TokenWeb;
import org.commonprovenance.framework.store.web.trustedParty.client.ClientTrustedParty;
import org.commonprovenance.framework.store.web.trustedParty.dto.response.TokenTPResponseDTO;
import org.openprovenance.prov.model.QualifiedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class TokenWebImpl implements TokenWeb {
  private final String LOG_PREFIX = "TokenWebImpl: ";
  private static final Logger LOGGER = LoggerFactory.getLogger(TokenWebImpl.class);

  private final ClientTrustedParty client;

  public TokenWebImpl(
      ClientTrustedParty client) {
    this.client = client;
  }

  private String getTokensUri(String id) {
    return "/organizations/" + id + "/tokens";
  }

  @Override
  public Function<String, Flux<Token>> getAllByOrganization(Optional<String> optTrustedPartyUrl) {
    Map<String, String> queryParams = Map.of("tokenFormat", "jwt");

    return (String organizationId) -> optTrustedPartyUrl
        .map(this.client::buildWebClient)
        .map(this.client.sendCustomGetManyRequest(getTokensUri(organizationId), TokenTPResponseDTO.class, queryParams))
        .orElse(this.client.sendGetManyRequest(getTokensUri(organizationId), TokenTPResponseDTO.class, queryParams))
        .flatMap(MONO.liftEffectToMono(ModelFactory::toDomain))
        .doOnComplete(() -> LOGGER.trace(LOG_PREFIX + "Tokens for organization with id '" + organizationId + "' has been fetched."))
        .doOnError(throwable -> LOGGER.error(LOG_PREFIX + "Tokens for organization with id '" + organizationId + "' has not been fetched!\n" + throwable.getMessage()))
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(
            new InternalApplicationException("Tokens for organization with id '" + organizationId + "' has not been fetched!")));
  }

  @Override
  public Mono<Token> getByDocumentId(
      String organizationId,
      QualifiedName bundleIdentifier,
      Format documentFormat,
      Optional<String> optTrustedPartyUrl) {
    String uri = getTokensUri(organizationId) + "/" + bundleIdentifier.getUri() + "/" + documentFormat.toString();
    Map<String, String> queryParams = Map.of("tokenFormat", "jwt");

    return optTrustedPartyUrl
        .map(this.client::buildWebClient)
        .map(this.client.sendCustomGetOneRequest(uri, TokenTPResponseDTO.class, queryParams))
        .orElse(client.sendGetOneRequest(uri, TokenTPResponseDTO.class, queryParams))
        .flatMap(MONO.liftEffectToMono(ModelFactory::toDomain))
        .doOnSuccess(_ -> LOGGER.trace(
            LOG_PREFIX + "Token has been fetched. Organization identifier is '" + organizationId + "'. Document identifier is '" + bundleIdentifier.getUri() + "'."))
        .doOnError(throwable -> {
          if (throwable instanceof NotFoundException notFound)
            LOGGER.trace(LOG_PREFIX + notFound.getMessage());
          else
            LOGGER.error(LOG_PREFIX + "Token has not been fetched. Organization identifier is '" + organizationId
                + "'. Document identifier is '" + bundleIdentifier.getUri() + "'!\n" + throwable.getMessage());
        })
        .onErrorMap(ApplicationExceptionFactory.handleThrowable(new InternalApplicationException("Token has not been fetched!")));
  }
}
