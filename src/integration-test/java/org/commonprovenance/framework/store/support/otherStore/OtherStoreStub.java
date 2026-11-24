package org.commonprovenance.framework.store.support.otherStore;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import java.net.URI;
import java.util.UUID;

import org.commonprovenance.framework.store.support.WireMockInitializer;
import org.commonprovenance.framework.store.support.fixture.ProvDocumentFixture;

import com.github.tomakehurst.wiremock.WireMockServer;

public class OtherStoreStub {

  private final WireMockServer wireMock;

  public OtherStoreStub(WireMockServer wireMock) {
    this.wireMock = wireMock;
  }

  public ProvDocumentFixture newDocument() {
    return ProvDocumentFixture.of(ProvDocumentFixture.MAIN_ACTIVITY_ONLY, WireMockInitializer.otherStoreUrl(), UUID.randomUUID().toString());
  }

  public void holds(ProvDocumentFixture document, String token) {
    String bundle = URI.create(document.bundleUrl()).getPath();
    wireMock.stubFor(head(urlPathEqualTo(bundle)).willReturn(aResponse().withStatus(200)));
    wireMock.stubFor(head(urlPathEqualTo(URI.create(document.metaBundleUrl()).getPath())).willReturn(aResponse().withStatus(200)));
    wireMock.stubFor(get(urlPathEqualTo(bundle + "/token")).willReturn(okJson("{\"jwt\":\"" + token + "\"}")));
  }
}
