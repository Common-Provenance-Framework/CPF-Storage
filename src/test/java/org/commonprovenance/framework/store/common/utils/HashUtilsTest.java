package org.commonprovenance.framework.store.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Hash Utils Test")
public class HashUtilsTest {

  @Test
  @DisplayName("should hash string (SHA-256)")
  public void shouldHashString_SHA256() {
    String result = HashUtils.sha256("Hello world!");
    assertEquals("c0535e4be2b79ffd93291305436bf889314e4a3faec05ecffcbb7df31ad9e51a", result);
  }
}
