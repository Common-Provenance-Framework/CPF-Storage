package org.commonprovenance.framework.store.support.store;

import java.util.List;

import org.commonprovenance.framework.store.support.StoreApi;
import org.commonprovenance.framework.store.support.fixture.OrganizationData;
import org.commonprovenance.framework.store.support.fixture.ProvDocumentFixture;
import org.springframework.http.HttpMethod;
import org.springframework.http.codec.json.JacksonJsonDecoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.assertj.WebTestClientResponse;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

public class StoreClient {

  private static final JsonMapper STRICT = JsonMapper.builder()
      .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build();

  private final WebTestClient webTestClient;

  public StoreClient(WebTestClient webTestClient) {
    this.webTestClient = webTestClient.mutate()
        .codecs(codecs -> codecs.defaultCodecs().jacksonJsonDecoder(new JacksonJsonDecoder(STRICT)))
        .build();
  }

  public WebTestClientResponse registerOrganization(OrganizationData organization) {
    return send(HttpMethod.POST, new RegistrationForm(organization.id(), organization.clientCertificate(), organization.intermediateCertificates(), 30), StoreApi.ORGANIZATIONS);
  }

  public WebTestClientResponse getOrganization(OrganizationData organization) {
    return send(HttpMethod.GET, null, StoreApi.ORGANIZATION, organization.id());
  }

  public WebTestClientResponse updateCertificates(OrganizationData organization) {
    return send(HttpMethod.PUT, new UpdateForm(organization.clientCertificate(), organization.intermediateCertificates(), 30), StoreApi.ORGANIZATION, organization.id());
  }

  public WebTestClientResponse uploadDocument(ProvDocumentFixture document) {
    return send(HttpMethod.POST, new DocumentForm(document.base64(), "JSON", document.signature()), StoreApi.DOCUMENTS, document.organizationId());
  }

  public WebTestClientResponse getDocument(ProvDocumentFixture document) {
    return send(HttpMethod.GET, null, StoreApi.DOCUMENT, document.organizationId(), document.bundleId());
  }

  public WebTestClientResponse getDocumentToken(ProvDocumentFixture document) {
    return send(HttpMethod.GET, null, StoreApi.DOCUMENT_TOKEN, document.organizationId(), document.bundleId());
  }

  public WebTestClientResponse checkDocumentExists(ProvDocumentFixture document) {
    return send(HttpMethod.HEAD, null, StoreApi.DOCUMENT, document.organizationId(), document.bundleId());
  }

  public WebTestClientResponse getDomainSpecificView(ProvDocumentFixture document) {
    return send(HttpMethod.GET, null, StoreApi.DOCUMENT_DOMAIN_SPECIFIC, document.organizationId(), document.bundleId());
  }

  public WebTestClientResponse getBackboneView(ProvDocumentFixture document) {
    return send(HttpMethod.GET, null, StoreApi.DOCUMENT_BACKBONE, document.organizationId(), document.bundleId());
  }

  public WebTestClientResponse checkMetaDocumentExists(ProvDocumentFixture document) {
    return send(HttpMethod.HEAD, null, StoreApi.META_DOCUMENT, document.metaBundleId());
  }

  public WebTestClientResponse getMetaDocument(ProvDocumentFixture document) {
    return send(HttpMethod.GET, null, StoreApi.META_DOCUMENT, document.metaBundleId());
  }

  private WebTestClientResponse send(HttpMethod method, Object body, String path, Object... uriVariables) {
    WebTestClient.RequestBodySpec request = webTestClient.method(method).uri(path, uriVariables);
    return WebTestClientResponse.from(body == null ? request.exchange() : request.bodyValue(body).exchange());
  }

  private record RegistrationForm(String id, String clientCertificate, List<String> intermediateCertificates, Integer clearancePeriod) {
  }

  private record UpdateForm(String clientCertificate, List<String> intermediateCertificates, Integer clearancePeriod) {
  }

  private record DocumentForm(String graph, String graphFormat, String signature) {
  }
}
