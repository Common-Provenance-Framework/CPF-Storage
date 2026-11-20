package org.commonprovenance.framework.store.web.store;

import org.commonprovenance.framework.store.web.client.reactive.ClientReactive;
import org.commonprovenance.framework.store.web.config.WebConfig;
import org.commonprovenance.framework.store.web.store.impl.StoreWebImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
}
