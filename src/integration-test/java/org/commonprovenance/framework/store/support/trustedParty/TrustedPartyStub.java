package org.commonprovenance.framework.store.support.trustedParty;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.util.List;
import java.util.Map;

import org.commonprovenance.framework.store.support.fixture.OrganizationData;
import org.commonprovenance.framework.store.support.fixture.ProvDocumentFixture;
import org.commonprovenance.framework.store.support.fixture.TrustedPartyFixture;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;

import tools.jackson.databind.json.JsonMapper;

public class TrustedPartyStub {

  private static final String API = "/api/v1";
  private static final JsonMapper MAPPER = JsonMapper.builder().build();

  private final WireMockServer wireMock;

  public TrustedPartyStub(WireMockServer wireMock) {
    this.wireMock = wireMock;
  }

  public void providesItsInfo() {
    wireMock.stubFor(get(urlPathEqualTo(API + "/info"))
        .willReturn(json(200, Map.of("id", TrustedPartyFixture.NAME, "clientCertificate", TrustedPartyFixture.CERTIFICATE))));
  }

  public void doesNotKnow(OrganizationData organization) {
    wireMock.stubFor(get(urlPathEqualTo(API + "/organizations/" + organization.id()))
        .willReturn(json(404, Map.of(
            "timestamp", "2026-11-15T12:00:00",
            "status", "NOT_FOUND",
            "message", "Organization with id '" + organization.id() + "' not found",
            "path", API + "/organizations/" + organization.id()))));
  }

  public void knows(OrganizationData organization) {
    wireMock.stubFor(get(urlPathEqualTo(API + "/organizations/" + organization.id()))
        .willReturn(json(200, Map.of("id", organization.id(), "clientCertificate", organization.clientCertificate(), "revokedCertificates", List.of()))));
  }

  public void acceptsRegistrationOf(OrganizationData organization) {
    wireMock.stubFor(post(urlPathEqualTo(API + "/organizations/" + organization.id()))
        .withRequestBody(equalToJson(certificatesOf(organization)))
        .willReturn(aResponse().withStatus(201)));
  }

  public void acceptsCertificateUpdateOf(OrganizationData organization) {
    wireMock.stubFor(put(urlPathEqualTo(API + "/organizations/" + organization.id() + "/certs"))
        .withRequestBody(equalToJson(certificatesOf(organization)))
        .willReturn(aResponse().withStatus(201)));
  }

  public void issuesGraphTokenFor(ProvDocumentFixture document, String token) {
    wireMock.stubFor(issueToken()
        .withRequestBody(equalToJson(MAPPER.writeValueAsString(Map.of(
            "organizationId", document.organizationId(),
            "graph", document.base64(),
            "graphFormat", "JSON",
            "signature", document.signature(),
            "graphType", "GRAPH",
            "createdOn", "${json-unit.any-string}"))))
        .willReturn(json(201, Map.of("jwt", token))));
  }

  public void issuesDomainSpecificTokenFor(ProvDocumentFixture document, String token) {
    issuesTokenForPartOf(document, "DOMAIN_SPECIFIC", token);
  }

  public void issuesBackboneTokenFor(ProvDocumentFixture document, String token) {
    issuesTokenForPartOf(document, "BACKBONE", token);
  }

  public void issuesMetaTokenFor(ProvDocumentFixture document, String token) {
    issuesTokenForPartOf(document, "META", token);
  }

  private void issuesTokenForPartOf(ProvDocumentFixture document, String graphType, String token) {
    wireMock.stubFor(issueToken()
        .withRequestBody(matchingJsonPath("$.organizationId", equalTo(document.organizationId())))
        .withRequestBody(matchingJsonPath("$.graphType", equalTo(graphType)))
        .willReturn(json(201, Map.of("jwt", token))));
  }

  private static MappingBuilder issueToken() {
    return post(urlPathEqualTo(API + "/issueToken"));
  }

  private static String certificatesOf(OrganizationData organization) {
    return MAPPER.writeValueAsString(Map.of(
        "id", organization.id(),
        "clientCertificate", organization.clientCertificate(),
        "intermediateCertificates", organization.intermediateCertificates()));
  }

  private static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder json(int status, Object body) {
    return okJson(MAPPER.writeValueAsString(body)).withStatus(status);
  }
}
