package org.commonprovenance.framework.storage.common.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Bytes Utils Test")
public class BytesUtilsTest {

  @Test
  @DisplayName("should convert bytes to Hex String")
  public void shouldConvertBytesToStringHex() {
    byte[] data = { 72, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 33 };
    String result = BytesUtils.bytesToHex(data);
    assertEquals("48656c6c6f20776f726c6421", result);
  }

  @Test
  @DisplayName("should convert String to byte[] (UTF-8)")
  public void shouldConvertStringToBytes_UTF8() {
    byte[] expected = { 72, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 33 };
    byte[] result = BytesUtils.stringToBytes_UTF8("Hello world!");
    assertArrayEquals(expected, result);
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
    byte[] result = BytesUtils.stringToBytes("Hello world!", StandardCharsets.UTF_32);
    assertArrayEquals(expected, result);
  }

  @Test
  @DisplayName("should convert byte[] to String (UTF-8)")
  public void shouldConvertBytesToString_UTF8() {
    byte[] data = { 72, 101, 108, 108, 111, 32, 119, 111, 114, 108, 100, 33 };
    String result = BytesUtils.bytesToString_UTF8(data);
    assertEquals("Hello world!", result);
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
    String result = BytesUtils.bytesToString(data, StandardCharsets.UTF_32);
    assertEquals("Hello world!", result);
  }

}
