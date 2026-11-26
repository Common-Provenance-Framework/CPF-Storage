package org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j.client;

import org.commonprovenance.framework.store.persistence.config.Neo4jConfig;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TokenNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableReactiveNeo4jRepositories;
import org.springframework.test.context.TestPropertySource;

// The name does not match surefire (*Test, *Spec) or failsafe (*IT) patterns, so no build runs it.
// Run from CLI:  ./mvnw test -Dtest=StoreWebSandbox   (-Dtest overrides the includes)
// Or run it from the IDE like any JUnit test. Uses the real TokenNeo4jRepositoryClient, no mocks.
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {
    Neo4jConfig.class,
    TokenNeo4jRepositoryClient.class,
    TokenNeo4jRepositoryClientSandbox.RepositoryConfig.class })
@TestPropertySource(locations = "classpath:sandbox.properties")
public class TokenNeo4jRepositoryClientSandbox {
  @Configuration(proxyBeanMethods = false)
  @EnableReactiveNeo4jRepositories(basePackageClasses = TokenNeo4jRepositoryClient.class)
  static class RepositoryConfig {
  }

  @Autowired
  private TokenNeo4jRepositoryClient client;

  @Value("${sandbox.organization-id}")
  private String organizationIdentifier;

  @Value("${sandbox.document-id}")
  private String documentIdentifier;

  @Value("${trusted-party.id}")
  private String trustedPartyIdentifier;

  @Value("${sandbox.graph}")
  private String graph;

  @Value("${sandbox.jwt}")
  private String jwt;

  @Value("${sandbox.client-cert}")
  private String cert;

  @Value("${sandbox.int1-cert}")
  private String int1Cert;

  @Value("${sandbox.int2-cert}")
  private String int2Cert;

  private String TOKEN_ID = "4:c07ee6e7-57c3-477f-9201-b05fab4c0316:4";

  @Test
  void saveGraph() {
    TrustedPartyNode trustedPartyNode = new TrustedPartyNode(trustedPartyIdentifier, cert, true, true, true);
    TokenNode tokenNode = new TokenNode(jwt)
        .withTrustedParty(trustedPartyNode);

    client.save(tokenNode)
        .doOnSuccess(value -> {
          System.out.println("..........................");
          System.out.println("Token saved");
          System.out.println(value);
          System.out.println("..........................");
        })
        .doOnError(throwable -> {
          System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
          System.err.println(throwable.getClass().getName());
          System.err.println(throwable.getMessage());
          System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
        })
        .onErrorComplete()
        .block();
  }

  @Test
  void saveToken() {
    TokenNode tokenNode = new TokenNode(jwt);

    client.save(tokenNode)
        .doOnSuccess(value -> {
          System.out.println("..........................");
          System.out.println("Token saved");
          System.out.println(value);
          System.out.println("..........................");
        })
        .doOnError(throwable -> {
          System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
          System.err.println(throwable.getClass().getName());
          System.err.println(throwable.getMessage());
          System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
        })
        .onErrorComplete()
        .block();
  }

  @Test
  void findById() {
    client.findById(TOKEN_ID)
        .doOnSuccess(value -> {
          System.out.println("..........................");
          System.out.println(value);
          System.out.println("..........................");
        })
        .doOnError(throwable -> {
          System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
          System.err.println(throwable.getClass().getName());
          System.err.println(throwable.getMessage());
          System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
        })
        .onErrorComplete()
        .block();
  }

  @Test
  void findTokenIdsByDocumentIdentifier() {
    client.findTokenIdsByDocumentIdentifier(documentIdentifier)
        .doOnNext(value -> {
          System.out.println("..........................");
          System.out.println(value);
          System.out.println("..........................");
        })
        .doOnError(throwable -> {
          System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
          System.err.println(throwable.getClass().getName());
          System.err.println(throwable.getMessage());
          System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
        })
        .onErrorComplete()
        .collectList()
        .block();
  }

  @Test
  void createWasIssuedByRelationship() {
    client.createWasIssuedByRelationship(TOKEN_ID, trustedPartyIdentifier)
        .doOnSuccess(value -> {
          System.out.println("..........................");
          System.out.println(value);
          System.out.println("..........................");
        })
        .doOnError(throwable -> {
          System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
          System.err.println(throwable.getClass().getName());
          System.err.println(throwable.getMessage());
          System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
        })
        .onErrorComplete()
        .block();
  }
}
