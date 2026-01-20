package org.commonprovenance.framework.storage.common.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Utils {

  public static String decodeToString(String base64Data) {
    return Base64Utils.decodeToString(base64Data, StandardCharsets.UTF_8);
  }

  public static String decodeToString(String base64Data, Charset charset) {
    byte[] decodedBytes = Base64Utils.decode(base64Data);
    return BytesUtils.bytesToString(decodedBytes, charset);
  }

  public static byte[] decode(String base64Data) {
    return Base64.getDecoder().decode(base64Data);
  }

  public static String encodeFromString(String stringData) {
    return Base64Utils.encodeFromString(stringData, StandardCharsets.UTF_8);
  }

  public static String encodeFromString(String stringData, Charset charset) {
    return Base64Utils.encode(BytesUtils.stringToBytes(stringData, charset));
  }

  public static String encode(byte[] bytesData) {
    return Base64.getEncoder().encodeToString(bytesData);
  }
}
