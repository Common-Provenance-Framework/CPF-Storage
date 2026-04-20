package org.commonprovenance.framework.store.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.vavr.control.Either;

@DisplayName("Base64 Utils Test")
public class Base64UtilsTest {

  private void handleLeft(ApplicationException appException) {
    fail("Left side has not been expected: " + appException.getMessage());
  }

  @Test
  @DisplayName("should encode to base64 byte[]")
  public void shouldEncodeToBase64() {
    byte[] bytes = { 72, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 33 };
    Base64Utils.encode(bytes)
        .peek(result -> assertEquals("SGVsbG8gd29ybGQh", result))
        .peekLeft(this::handleLeft);
  }

  @Test
  @DisplayName("should encode to base64 String (UTF-8)")
  public void shouldEncodeToBase64String() {
    Base64Utils.encodeFromString("Hello world!")
        .peek(result -> assertEquals("SGVsbG8gd29ybGQh", result))
        .peekLeft(this::handleLeft);
  }

  @Test
  @DisplayName("should encode to base64 String (UTF-32)")
  public void shouldEncodeToBase64String_UTF32() {
    Either.<ApplicationException, String>right("Hello world!")
        .flatMap(Base64Utils.encodeFromString(StandardCharsets.UTF_32))
        .peek(result -> assertEquals("AAAASAAAAGUAAABsAAAAbAAAAG8AAAAgAAAAdwAAAG8AAAByAAAAbAAAAGQAAAAh", result))
        .peekLeft(this::handleLeft);
  }

  @Test
  @DisplayName("should decode from base64 String (byte[])")
  public void shouldDecodeFromBase64String_byte() {
    Base64Utils.decode("SGVsbG8gd29ybGQh")
        .flatMap(BytesUtils::bytesToHex)
        .peek(result -> assertEquals("48656c6c6f20776f726c6421", result))
        .peekLeft(this::handleLeft);
  }

  @Test
  @DisplayName("should decode from base64 String (UTF-8)")
  public void shouldDecodeFromBase64String() {
    Base64Utils.decodeToString("SGVsbG8gd29ybGQh")
        .peek(result -> assertEquals("Hello world!", result))
        .peekLeft(this::handleLeft);
  }

  @Test
  @DisplayName("should decode from base64 String (UTF-32)")
  public void shouldDecodeFromBase64String_UTF32() {
    Either.<ApplicationException, String>right("AAAASAAAAGUAAABsAAAAbAAAAG8AAAAgAAAAdwAAAG8AAAByAAAAbAAAAGQAAAAh")
        .flatMap(Base64Utils.decodeToString(StandardCharsets.UTF_32))
        .peek(result -> assertEquals("Hello world!", result))
        .peekLeft(this::handleLeft);
  }
}
