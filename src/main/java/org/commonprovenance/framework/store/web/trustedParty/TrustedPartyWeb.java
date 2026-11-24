package org.commonprovenance.framework.store.web.trustedParty;

import java.util.Optional;
import java.util.function.Function;

import org.commonprovenance.framework.store.model.GraphType;
import org.commonprovenance.framework.store.model.MetaDocument;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;

import reactor.core.publisher.Mono;

/**
 * Provides reactive operations for retrieving trusted-party information, issuing graph tokens, and
 * verifying graph signatures.
 */
public interface TrustedPartyWeb {
  /**
   * Retrieves trusted-party information from the specified base URL or the configured default.
   *
   * @param optTrustedPartyBaseUrl optional custom trusted-party base URL
   * @return a publisher that emits the retrieved {@link TrustedParty}, or an error if retrieval
   *     fails
   */
  Mono<TrustedParty> getTrustedParty(Optional<String> optTrustedPartyBaseUrl);

  /**
   * Creates a function that issues a token for an organization's graph using the supplied
   * signature.
   *
   * @param signature signature associated with the graph
   * @return a function that accepts an {@link Organization} and publishes its issued
   *     {@link Token}
   */
  Function<Organization, Mono<Token>> issueGraphToken(String signature);

  /**
   * Creates a function that issues a token for an organization's graph of the specified type.
   *
   * @param graphType type of graph for which to issue a token
   * @return a function that accepts an {@link Organization} and publishes its issued
   *     {@link Token}
   */
  Function<Organization, Mono<Token>> issueGraphToken(GraphType graphType);

  /**
   * Issues a token for the supplied metadata document.
   *
   * @param metaDocument metadata document for which to issue a token
   * @return a publisher that emits the issued {@link Token}, or an error if issuance fails
   */
  Mono<Token> issueGraphToken(MetaDocument metaDocument);

  /**
   * Creates a function that verifies a signature for an organization's graph.
   *
   * @param singature signature to verify
   * @return a function that accepts an {@link Organization} and publishes {@code true} if its
   *     signature is valid, or {@code false} if it is invalid
   */
  Function<Organization, Mono<Boolean>> verifySignature(String singature);
}
