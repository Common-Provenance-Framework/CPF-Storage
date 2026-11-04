package org.commonprovenance.framework.store.web.trustedParty;

import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.web.config.WebConfig;
import org.commonprovenance.framework.store.web.trustedParty.impl.OrganizationWebImpl;
import org.commonprovenance.framework.store.web.client.reactive.ClientReactive;
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

import io.vavr.control.Either;

// The name does not match surefire (*Test, *Spec) or failsafe (*IT) patterns, so no build runs it.
// Run from CLI:  ./mvnw test -Dtest=OrganizationWebSandbox   (-Dtest overrides the includes)
// Or run it from the IDE like any JUnit test. Uses the real OrganizationWebImpl, no mocks.
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = { WebConfig.class, ClientReactive.class, OrganizationWebImpl.class })
@TestPropertySource(locations = "classpath:sandbox.properties")
class OrganizationWebSandbox {

  @Autowired
  private OrganizationWeb organizationWeb;

  @Value("${trusted-party.url}")
  private String trustedPartyUrl;

  @Value("${sandbox.organization-id}")
  private String organizationId;

  @Value("${sandbox.document-id}")
  private String documentId;

  @Value("${sandbox.graph}")
  private String graph;

  @Value("${sandbox.valid.signature}")
  private String validSignature;

  @Value("${sandbox.invalid.signature}")
  private String invalidSignature;

  @Value("${sandbox.client-cert}")
  private String clientCert;

  @Value("${sandbox.int1-cert}")
  private String int1Cert;

  @Value("${sandbox.int2-cert}")
  private String int2Cert;

  @Mock
  private TrustedParty trustedParty;

  @Mock
  private Organization organization;

  @Mock
  private Document document;

  @Mock
  private QualifiedName bundleIdentifier;

  @Test
  void create() {
    when(organization.getTrustedPartyBaseUrl()).thenReturn(Either.right(Optional.empty()));
    when(organization.getIdentifier()).thenReturn(organizationId);
    when(organization.getClientCertificate()).thenReturn(clientCert);
    when(organization.getIntermediateCertificates()).thenReturn(List.of(int2Cert, int1Cert));

    organizationWeb.create(organization)
        .doOnNext(_ -> {
          System.out.println("..........................");
          System.out.println("CREATED");
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

  // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  // !!!!!!!!!!!! RETURNS REVOKED CERTIFICATES !!!!!!!!!!!!!!
  // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

  @Test
  void getAll() {
    organizationWeb.getAll(Optional.empty())
        .collectList()
        .doOnNext(data -> {
          System.out.println("..........................");
          data.forEach(System.out::println);
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

  @Test
  void getById() {
    when(organization.getTrustedPartyBaseUrl()).thenReturn(Either.right(Optional.empty()));
    when(organization.getIdentifier()).thenReturn(organizationId);

    organizationWeb.getById(organization)
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
