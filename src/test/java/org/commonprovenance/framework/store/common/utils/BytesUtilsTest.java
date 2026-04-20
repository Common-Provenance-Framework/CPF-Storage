package org.commonprovenance.framework.store.common.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.charset.StandardCharsets;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.vavr.control.Either;

@DisplayName("Bytes Utils Test")
public class BytesUtilsTest {

  private void handleLeft(ApplicationException appException) {
    fail("Left side has not been expected: " + appException.getMessage());
  }

  @Test
  @DisplayName("should convert bytes to Hex String")
  public void shouldConvertBytesToStringHex() {
    byte[] data = { 72, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 33 };
    BytesUtils.bytesToHex(data)
        .peek(result -> assertEquals("48656c6c6f20776f726c6421", result))
        .peekLeft(this::handleLeft);
  }

  @Test
  @DisplayName("should convert String to byte[] (UTF-8)")
  public void shouldConvertStringToBytes_UTF8() {
    byte[] expected = { 72, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 33 };
    BytesUtils.stringToBytes_UTF8("Hello world!")
        .peek(result -> assertArrayEquals(expected, result))
        .peekLeft(this::handleLeft);
  }

  @Test
  @DisplayName("should convert String to byte[] (UTF-32)")
  public void shouldConvertStringToBytes_UTF32() {
    byte[] expected = {
        0, 0, 0, 72,
        0, 0, 0, 101,
        0, 0, 0, 108,
        0, 0, 0, 108,
        0, 0, 0, 111,
        0, 0, 0, 32,
        0, 0, 0, 119,
        0, 0, 0, 111,
        0, 0, 0, 114,
        0, 0, 0, 108,
        0, 0, 0, 100,
        0, 0, 0, 33 };
    Either.<ApplicationException, String>right("Hello world!")
        .flatMap(BytesUtils.stringToBytes(StandardCharsets.UTF_32))
        .peek(result -> assertArrayEquals(expected, result))
        .peekLeft(this::handleLeft);
  }

  @Test
  @DisplayName("should convert byte[] to String (UTF-8)")
  public void shouldConvertBytesToString_UTF8() {
    byte[] data = { 72, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 33 };
    BytesUtils.bytesToString_UTF8(data)
        .peek(result -> assertEquals("Hello world!", result))
        .peekLeft(this::handleLeft);
  }

  @Test
  @DisplayName("should convert byte[] to String (UTF-32)")
  public void shouldConvertBytesToString_UTF32() {
    byte[] data = {
        0, 0, 0, 72,
        0, 0, 0, 101,
        0, 0, 0, 108,
        0, 0, 0, 108,
        0, 0, 0, 111,
        0, 0, 0, 32,
        0, 0, 0, 119,
        0, 0, 0, 111,
        0, 0, 0, 114,
        0, 0, 0, 108,
        0, 0, 0, 100,
        0, 0, 0, 33 };

    Either.<ApplicationException, byte[]>right(data)
        .flatMap(BytesUtils.bytesToString(StandardCharsets.UTF_32))
        .peek(result -> assertEquals("Hello world!", result))
        .peekLeft(this::handleLeft);
  }

}
