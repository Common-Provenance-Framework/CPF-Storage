package org.commonprovenance.framework.storage.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
  public static String sha256(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(BytesUtils.stringToBytes_UTF8(input));
      return BytesUtils.bytesToHex(hash);
    } catch (NoSuchAlgorithmException e) {
      // SHA-256 is guaranteed to exist in Java
      throw new RuntimeException(e);
    }
  }

}
