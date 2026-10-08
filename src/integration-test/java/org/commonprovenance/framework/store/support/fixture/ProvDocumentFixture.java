package org.commonprovenance.framework.store.support.fixture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.model.GraphFormat;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.PropertyPlaceholderHelper;

public record ProvDocumentFixture(String templatePath, Map<String, String> values) {

  private static final PropertyPlaceholderHelper PLACEHOLDERS = new PropertyPlaceholderHelper("${", "}", null, null, false);

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

  public String documentsUrl() {
    return value("storeUrl") + "organizations/" + organizationId() + "/documents/";
  }

  public String metaBundlesUrl() {
    return value("storeUrl") + "documents/meta/";
  }

  public String bundleUrl() {
    return documentsUrl() + bundleId();
  }

  public String json() {
    Map<String, String> placeholders = new HashMap<>(values);
    placeholders.putIfAbsent("documentsUrl", documentsUrl());
    placeholders.putIfAbsent("metaBundlesUrl", metaBundlesUrl());
    return PLACEHOLDERS.replacePlaceholders(readTemplate(), placeholders::get);
  }

  public String base64() {
    return Base64.getEncoder().encodeToString(json().getBytes(StandardCharsets.UTF_8));
  }

  public DocumentFormDTO toForm(String signature) {
    return new DocumentFormDTO(base64(), GraphFormat.JSON, signature);
  }

  private String readTemplate() {
    try {
      return new ClassPathResource(templatePath).getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Can not read PROV document template '" + templatePath + "'", e);
    }
  }
}
