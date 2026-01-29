package org.commonprovenance.framework.store.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Base64 Utils Test")
public class Base64UtilsTest {

  @Test
  @DisplayName("should encode to base64 byte[]")
  public void shouldEncodeToBase64() {
    byte[] bytes = { 72, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 33 };
    String result = Base64Utils.encode(bytes);
    assertEquals("SGVsbG8gd29ybGQh", result);
  }

  @Test
  @DisplayName("should encode to base64 String (UTF-8)")
  public void shouldEncodeToBase64String() {
    String result = Base64Utils.encodeFromString("Hello world!");
    assertEquals("SGVsbG8gd29ybGQh", result);
  }

  @Test
  @DisplayName("should encode to base64 String (UTF-32)")
  public void shouldEncodeToBase64String_UTF32() {
    String result = Base64Utils.encodeFromString("Hello world!", StandardCharsets.UTF_32);
    assertEquals("AAAASAAAAGUAAABsAAAAbAAAAG8AAAAgAAAAdwAAAG8AAAByAAAAbAAAAGQAAAAh", result);
  }

  @Test
  @DisplayName("should decode from base64 String (byte[])")
  public void shouldDecodeFromBase64String_byte() {
    byte[] result = Base64Utils.decode("SGVsbG8gd29ybGQh");
    assertEquals("48656c6c6f20776f726c6421", BytesUtils.bytesToHex(result));
  }

  @Test
  @DisplayName("should decode from base64 String (UTF-8)")
  public void shouldDecodeFromBase64String() {
    String result = Base64Utils.decodeToString("SGVsbG8gd29ybGQh");
    assertEquals("Hello world!", result);
  }

  @Test
  @DisplayName("should decode from base64 String (UTF-32)")
  public void shouldDecodeFromBase64String_UTF32() {
    String result = Base64Utils.decodeToString(
        "AAAASAAAAGUAAABsAAAAbAAAAG8AAAAgAAAAdwAAAG8AAAByAAAAbAAAAGQAAAAh",
        StandardCharsets.UTF_32);
    assertEquals("Hello world!", result);
  }
}
