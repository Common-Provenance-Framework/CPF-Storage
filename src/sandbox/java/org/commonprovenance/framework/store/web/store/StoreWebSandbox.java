package org.commonprovenance.framework.store.web.store;

import static org.mockito.Mockito.when;

import java.util.Optional;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.web.config.WebConfig;
import org.commonprovenance.framework.store.web.store.impl.StoreWebImpl;
import org.commonprovenance.framework.store.web.client.reactive.ClientReactive;
import org.commonprovenance.framework.store.web.trustedParty.TrustedPartyWeb;
import org.commonprovenance.framework.store.web.trustedParty.impl.TrustedPartyWebImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// The name does not match surefire (*Test, *Spec) or failsafe (*IT) patterns, so no build runs it.
// Run from CLI:  ./mvnw test -Dtest=StoreWebSandbox   (-Dtest overrides the includes)
// Or run it from the IDE like any JUnit test. Uses the real StoreWebImpl, no mocks.
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = { WebConfig.class, ClientReactive.class, StoreWebImpl.class })
@TestPropertySource(locations = "classpath:sandbox.properties")
class StoreWebSandbox {

  @Autowired
  private StoreWebImpl storeWeb;

  @Value("${store.url}")
  private String storeUrl;

  @Value("${sandbox.organization-id}")
  private String organizationId;

  @Value("${sandbox.document-id}")
  private String documentId;

  @Test
  void pingDocument() {
    storeWeb.pingByResourcePath(storeUrl + "/organizations/" + organizationId + "/documents/" + documentId)
        .doOnSuccess(_ -> {
          System.err.println("..........................");
          System.err.println("Document exists");
          System.err.println("..........................");
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
  void getToken() {
    storeWeb.getTokenByResourcePath(storeUrl + "/organizations/" + organizationId + "/documents/" + documentId)
        .doOnSuccess(token -> {
          System.err.println("..........................");
          System.err.println(token);
          System.err.println("..........................");
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

  // @Test
  // void issueToken_Graph() {
  // when(document.getGraph()).thenReturn(graph);
  // when(document.getFormat()).thenReturn(GraphFormat.JSON);

  // when(trustedParty.getUrl()).thenReturn(Optional.of(trustedPartyUrl));

  // when(organization.getDocument()).thenReturn(Optional.of(document));
  // when(organization.getTrustedParty()).thenReturn(Optional.of(trustedParty));
  // when(organization.getIdentifier()).thenReturn(organizationId);

  // trustedPartyWeb.issueGraphToken(validSignature).apply(organization)
  // .doOnNext(data -> {
  // System.err.println("..........................");
  // System.err.println("Signature valid: " + data);
  // System.err.println("..........................");
  // })
  // .doOnError(throwable -> {
  // System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
  // System.err.println(throwable.getClass().getName());
  // System.err.println(throwable.getMessage());
  // System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
  // })
  // .onErrorComplete()
  // .block();
  // }

  // @Test
  // void verifySignature() {
  // when(document.getGraph()).thenReturn(graph);

  // when(trustedParty.getUrl()).thenReturn(Optional.of(trustedPartyUrl));

  // when(organization.getDocument()).thenReturn(Optional.of(document));
  // when(organization.getTrustedParty()).thenReturn(Optional.of(trustedParty));
  // when(organization.getIdentifier()).thenReturn(organizationId);

  // trustedPartyWeb.verifySignature(validSignature).apply(organization)
  // .doOnNext(data -> {
  // System.err.println("..........................");
  // System.err.println("Signature valid: " + data);
  // System.err.println("..........................");
  // })
  // .doOnError(throwable -> {
  // System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
  // System.err.println(throwable.getClass().getName());
  // System.err.println(throwable.getMessage());
  // System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
  // })
  // .onErrorComplete()
  // .block();
  // }
}
