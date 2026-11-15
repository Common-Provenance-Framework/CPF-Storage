package org.commonprovenance.framework.store.support.fixture;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

public record ProvDocumentFixture(String templatePath, Map<String, String> values) {

  public static final String MAIN_ACTIVITY_ONLY = "fixtures/documents/main-activity-only.json";
  public static final String WITH_DOMAIN_SPECIFIC_PART = "fixtures/documents/with-domain-specific-part.json";
  public static final String CONNECTED_TO_REFERENCED_BUNDLE = "fixtures/documents/connected-to-referenced-bundle.json";

  private static final String SIGNATURE = "document-signature";

  public static ProvDocumentFixture of(String templatePath, String storeUrl, String organizationId) {
    return new ProvDocumentFixture(templatePath, Map.of(
        "storeUrl", storeUrl,
        "organizationId", organizationId,
        "bundleId", UUID.randomUUID().toString(),
        "metaBundleId", UUID.randomUUID().toString()));
  }

  public ProvDocumentFixture with(String placeholder, String value) {
    Map<String, String> values = new HashMap<>(this.values);
    values.put(placeholder, value);
    return new ProvDocumentFixture(templatePath, Map.copyOf(values));
  }

  public ProvDocumentFixture nextVersion() {
    return with("bundleId", UUID.randomUUID().toString());
  }

  public ProvDocumentFixture connectedTo(ProvDocumentFixture referenced) {
    return new ProvDocumentFixture(CONNECTED_TO_REFERENCED_BUNDLE, values)
        .with("referencedDocumentsUrl", referenced.documentsUrl())
        .with("referencedMetaBundlesUrl", referenced.metaBundlesUrl())
        .with("referencedBundleId", referenced.bundleId())
        .with("referencedMetaBundleId", referenced.metaBundleId())
        .with("referencedBundleHashValue", referenced.digest());
  }

  public String value(String placeholder) {
    return values.get(placeholder);
  }

  public String organizationId() {
    return value("organizationId");
  }

  public String bundleId() {
    return value("bundleId");
  }

  public String metaBundleId() {
    return value("metaBundleId");
  }

  public String signature() {
    return SIGNATURE;
  }

  public String documentsUrl() {
    return value("storeUrl") + "organizations/" + organizationId() + "/documents/";
  }

  public String metaBundlesUrl() {
    return value("storeUrl") + "documents/meta/";
  }

  public String bundleUrl() {
    return documentsUrl() + bundleId();
  }

  public String metaBundleUrl() {
    return metaBundlesUrl() + metaBundleId();
  }

  public String json() {
    return render(templatePath);
  }

  public String domainSpecificView() {
    return render(templatePath.replace(".json", ".domain-specific.json"));
  }

  public String backboneView() {
    return render(templatePath.replace(".json", ".backbone.json"));
  }

  private String render(String path) {
    Map<String, String> placeholders = new HashMap<>(values);
    placeholders.putIfAbsent("documentsUrl", documentsUrl());
    placeholders.putIfAbsent("metaBundlesUrl", metaBundlesUrl());
    return Template.render(path, placeholders);
  }

  public String base64() {
    return Base64.getEncoder().encodeToString(json().getBytes(StandardCharsets.UTF_8));
  }

  public String digest() {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(json().getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
