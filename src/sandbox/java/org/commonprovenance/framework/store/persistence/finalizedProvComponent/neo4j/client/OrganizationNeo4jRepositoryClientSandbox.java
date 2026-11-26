package org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j.client;

import java.util.List;

import org.commonprovenance.framework.store.persistence.config.Neo4jConfig;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.OrganizationNode;
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
// Or run it from the IDE like any JUnit test. Uses the real OrganizationNeo4jRepositoryClient, no mocks.
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {
    Neo4jConfig.class,
    OrganizationNeo4jRepositoryClient.class,
    OrganizationNeo4jRepositoryClientSandbox.RepositoryConfig.class })
@TestPropertySource(locations = "classpath:sandbox.properties")
public class OrganizationNeo4jRepositoryClientSandbox {
  @Configuration(proxyBeanMethods = false)
  @EnableReactiveNeo4jRepositories(basePackageClasses = OrganizationNeo4jRepositoryClient.class)
  static class RepositoryConfig {
  }

  @Autowired
  private OrganizationNeo4jRepositoryClient organizationClient;

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

  private String ORGANIZATION_ID = "4:c07ee6e7-57c3-477f-9201-b05fab4c0316:3";

  @Test
  void saveGraph() {
    TrustedPartyNode trustedPartyNode = new TrustedPartyNode(trustedPartyIdentifier, cert, true, true, true);
    TokenNode tokenNode = new TokenNode(jwt)
        .withTrustedParty(trustedPartyNode);
    DocumentNode documentNode = new DocumentNode(documentIdentifier, graph, "JSON")
        .withToken(tokenNode);
    OrganizationNode organizationNode = new OrganizationNode(organizationIdentifier, cert, List.of(int1Cert, int2Cert))
        .withDocument(documentNode)
        .withTrustedParty(trustedPartyNode);

    organizationClient.save(organizationNode)
        .doOnSuccess(value -> {
          System.out.println("..........................");
          System.out.println("Organization saved");
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
  void saveOrganization() {
    OrganizationNode organizationNode = new OrganizationNode(organizationIdentifier, cert, List.of(int1Cert, int2Cert));

    organizationClient.save(organizationNode)
        .doOnSuccess(value -> {
          System.out.println("..........................");
          System.out.println("Organization saved");
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
    organizationClient.findById(ORGANIZATION_ID)
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
    organizationClient.getIdByIdentifier(organizationIdentifier)
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
  void getByIdentifier() {
    organizationClient.getByIdentifier(organizationIdentifier)
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
  void updateCertificates() {
    organizationClient.updateCertificates(organizationIdentifier, cert, List.of(int1Cert, int2Cert))
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
  void createTrustsRelationship() {
    organizationClient.createTrustsRelationship(organizationIdentifier, trustedPartyIdentifier)
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
  void createOwnsRelationship() {
    organizationClient.createOwnsRelationship(organizationIdentifier, documentIdentifier)
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
  void existsByIdentifier() {
    organizationClient.existsByIdentifier(organizationIdentifier)
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
    organizationClient.countByIdentifier(organizationIdentifier)
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
