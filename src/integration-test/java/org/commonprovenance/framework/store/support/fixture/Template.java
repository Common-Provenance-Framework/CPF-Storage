package org.commonprovenance.framework.store.support.fixture;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.PropertyPlaceholderHelper;

final class Template {

  private static final PropertyPlaceholderHelper PLACEHOLDERS = new PropertyPlaceholderHelper("${", "}", null, null, false);

  private Template() {
  }

  static String render(String path, Map<String, String> values) {
    return PLACEHOLDERS.replacePlaceholders(read(path), values::get);
  }

  private static String read(String path) {
    try {
      return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Can not read fixture template '" + path + "'", e);
    }
  }
}
