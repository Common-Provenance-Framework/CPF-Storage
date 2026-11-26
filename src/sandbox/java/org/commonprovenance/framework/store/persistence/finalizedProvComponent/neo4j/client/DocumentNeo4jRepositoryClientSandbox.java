package org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j.client;

import org.commonprovenance.framework.store.persistence.config.Neo4jConfig;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
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
// Or run it from the IDE like any JUnit test. Uses the real DocumentNeo4jRepositoryClient, no mocks.
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {
    Neo4jConfig.class,
    DocumentNeo4jRepositoryClient.class,
    DocumentNeo4jRepositoryClientSandbox.RepositoryConfig.class })
@TestPropertySource(locations = "classpath:sandbox.properties")
public class DocumentNeo4jRepositoryClientSandbox {
  @Configuration(proxyBeanMethods = false)
  @EnableReactiveNeo4jRepositories(basePackageClasses = DocumentNeo4jRepositoryClient.class)
  static class RepositoryConfig {
  }

  @Autowired
  private DocumentNeo4jRepositoryClient client;

  @Value("${sandbox.document-id}")
  private String documentIdentifier;

  @Value("${sandbox.graph}")
  private String graph;

  @Value("${sandbox.jwt}")
  private String jwt;

  @Value("${sandbox.client-cert}")
  private String cert;

  private String DOCUMENT_ID = "4:c07ee6e7-57c3-477f-9201-b05fab4c0316:71";

  @Test
  void saveGraph() {
    TrustedPartyNode trustedPartyNode = new TrustedPartyNode("TrustedParty", cert, true, true, true);
    TokenNode tokenNode = new TokenNode(jwt)
        .withTrustedParty(trustedPartyNode);
    DocumentNode documentNode = new DocumentNode(documentIdentifier, graph, "JSON")
        .withToken(tokenNode);

    client.save(documentNode)
        .doOnSuccess(value -> {
          System.out.println("..........................");
          System.out.println("Document saved");
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
  void saveDocument() {
    DocumentNode documentNode = new DocumentNode(documentIdentifier, graph, "JSON");

    client.save(documentNode)
        .doOnSuccess(value -> {
          System.out.println("..........................");
          System.out.println("Document saved");
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
  void delete() {
    client.deleteById(DOCUMENT_ID)
        .doOnSuccess(_ -> {
          System.out.println("..........................");
          System.out.println("Document deleted");
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
    client.findById(DOCUMENT_ID)
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
  void getIdByIdentifier() {
    client.getIdByIdentifier(documentIdentifier)
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
  void existsByIdentifier() {
    client.existsByIdentifier(documentIdentifier)
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
  void countByIdentifier() {
    client.countByIdentifier(documentIdentifier)
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
  void findOrganizationIdentifierByIdentifier() {
    client.findOrganizationIdentifierByIdentifier(documentIdentifier)
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
}
