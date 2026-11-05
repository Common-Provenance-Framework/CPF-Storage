package org.commonprovenance.framework.store.web.trustedParty;

import static org.mockito.Mockito.when;

import java.util.Optional;

import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.web.client.reactive.ClientReactive;
import org.commonprovenance.framework.store.web.config.WebConfig;
import org.commonprovenance.framework.store.web.trustedParty.impl.DocumentWebImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openprovenance.prov.model.QualifiedName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClientResponseException;

// The name does not match surefire (*Test, *Spec) or failsafe (*IT) patterns, so no build runs it.
// Run from CLI:  ./mvnw test -Dtest=DocumentWebSandbox   (-Dtest overrides the includes)
// Or run it from the IDE like any JUnit test. Uses the real DocumentWebImpl, no mocks.
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = { WebConfig.class, ClientReactive.class, DocumentWebImpl.class })
@TestPropertySource(locations = "classpath:sandbox.properties")
class DocumentWebSandbox {

  @Autowired
  private DocumentWeb documentWeb;

  @Value("${store.url}")
  private String storeUrl;

  @Value("${sandbox.organization-id}")
  private String organizationId;

  @Value("${sandbox.document-id}")
  private String documentId;

  @Mock
  private QualifiedName bundleIdentifier;

  @Test
  void getById() {
    when(bundleIdentifier.getLocalPart()).thenReturn(documentId);
    when(bundleIdentifier.getUri()).thenReturn(storeUrl + "organizations/" + organizationId + "/documents/" + documentId);

    documentWeb.getById(organizationId, bundleIdentifier, GraphFormat.JSON, Optional.empty())
        .doOnNext(data -> {
          System.out.println("..........................");
          System.out.println(data);
          System.out.println("..........................");
        })
        .doOnError(throwable -> {
          System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
          System.err.println(throwable.getClass().getName());
          System.err.println(throwable.getMessage());
          if (throwable.getCause() instanceof WebClientResponseException wcrException)
            System.out.println(wcrException.getResponseBodyAsString());
          System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!");
        })
        .onErrorComplete()
        .block();
  }

}
