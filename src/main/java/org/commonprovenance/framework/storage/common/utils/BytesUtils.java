package org.commonprovenance.framework.storage.common.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class BytesUtils {
  public static String bytesToHex(byte[] bytes) {
    StringBuilder hex = new StringBuilder(bytes.length * 2);
    for (byte b : bytes) {
      hex.append(String.format("%02x", b));
    }
    return hex.toString();
  }

  public static String bytesToString_UTF8(byte[] bytes) {
    return bytesToString(bytes, StandardCharsets.UTF_8);
  }

  public static String bytesToString(byte[] bytes, Charset charset) {
    return new String(bytes, charset);
  }

  public static byte[] stringToBytes_UTF8(String stringData) {
    return stringToBytes(stringData, StandardCharsets.UTF_8);
  }

  public static byte[] stringToBytes(String stringData, Charset charset) {
    return stringData.getBytes(charset);
  }
}
