package org.commonprovenance.framework.store.support.fixture;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.commonprovenance.framework.store.support.ProvGraph;

import com.nimbusds.jwt.SignedJWT;

public final class MetaDocuments {

  private static final List<String> TEMPLATES = List.of(
      "fixtures/meta/one-version.json",
      "fixtures/meta/two-versions.json");
  private static final DateTimeFormatter PROV_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

  private MetaDocuments() {
  }

  public static ProvGraph metaDocumentWithVersions(StoredDocument... versions) {
    ProvDocumentFixture firstVersion = versions[0].document();
    Map<String, String> values = new HashMap<>(Map.of(
        "documentsUrl", firstVersion.documentsUrl(),
        "metaBundlesUrl", firstVersion.metaBundlesUrl(),
        "metaBundleId", firstVersion.metaBundleId(),
        "trustedPartyUrl", TrustedPartyFixture.url(),
        "trustedPartyCertificate", Base64.getEncoder().encodeToString(TrustedPartyFixture.CERTIFICATE.getBytes(StandardCharsets.UTF_8))));
    for (int version = 1; version <= versions.length; version++) {
      StoredDocument stored = versions[version - 1];
      values.put("bundleId" + version, stored.document().bundleId());
      values.put("token" + version, stored.token());
      values.put("tokenIssuedAt" + version, issuedAt(stored.token()));
    }
    return ProvGraph.of(Template.render(TEMPLATES.get(versions.length - 1), values));
  }

  private static String issuedAt(String token) {
    try {
      return PROV_TIME.format(SignedJWT.parse(token).getJWTClaimsSet().getIssueTime().toInstant());
    } catch (ParseException e) {
      throw new IllegalStateException("Can not read issue time of token '" + token + "'", e);
    }
  }
}
