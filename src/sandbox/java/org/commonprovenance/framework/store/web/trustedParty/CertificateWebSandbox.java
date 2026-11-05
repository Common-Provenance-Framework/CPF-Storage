package org.commonprovenance.framework.store.web.trustedParty;

import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.client.reactive.ClientReactive;
import org.commonprovenance.framework.store.web.config.WebConfig;
import org.commonprovenance.framework.store.web.trustedParty.impl.CertificateWebImpl;
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
// Run from CLI:  ./mvnw test -Dtest=CertificateWebSandbox   (-Dtest overrides the includes)
// Or run it from the IDE like any JUnit test. Uses the real CertificateWebImpl, no mocks.
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = { WebConfig.class, ClientReactive.class, CertificateWebImpl.class })
@TestPropertySource(locations = "classpath:sandbox.properties")
class CertificateWebSandbox {

  @Autowired
  private CertificateWeb certificateWeb;

  @Value("${store.url}")
  private String storeUrl;

  @Value("${sandbox.organization-id}")
  private String organizationId;

  @Mock
  private Organization organization;

  @Value("${sandbox.client-cert}")
  private String clientCert;

  @Value("${sandbox.int1-cert}")
  private String int1Cert;

  @Value("${sandbox.int2-cert}")
  private String int2Cert;

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
  void getById() {

    certificateWeb.getOrganizationCertificate(Optional.empty())
        .apply(organizationId)
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

  @Test
  void updateOrganizationCertificate() {
    when(organization.getIdentifier()).thenReturn(organizationId);
    when(organization.getTrustedPartyBaseUrl()).thenReturn(Either.right(Optional.empty()));
    when(organization.getClientCertificate()).thenReturn(clientCert);
    when(organization.getIntermediateCertificates()).thenReturn(List.of(int2Cert, int1Cert));

    certificateWeb.updateOrganizationCertificate(organization)
        .doOnNext(_ -> {
          System.out.println("..........................");
          System.out.println("UPDATED");
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
