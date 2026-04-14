package org.commonprovenance.framework.store.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
// import java.util.Base64;

/**
 * Utility class for parsing JWT tokens.
 */
public class JwtUtils {

  private static final ObjectMapper mapper = new ObjectMapper();

  /**
   * Extracts the tokenTimestamp from a JWT payload.
   * JWT format: header.payload.signature
   * The payload is base64url encoded JSON containing claims.
   *
   * @param jwt the JWT string
   * @return the tokenTimestamp as Long, or null if not found or invalid
   */
  public static Long extractTokenTimestamp(String jwt) {
    if (jwt == null || jwt.isBlank()) {
      return null;
    }

    try {
      // Split JWT into parts: header.payload.signature
      String[] parts = jwt.split("\\.");
      if (parts.length < 2) {
        return null;
      }

      // Decode payload (base64url)
      String payload = Base64Utils.decodeBase64UrlToString(parts[1]);

      // Parse JSON and extract tokenTimestamp
      JsonNode jsonNode = mapper.readTree(payload);
      JsonNode tokenTimestampNode = jsonNode.get("tokenTimestamp");

      if (tokenTimestampNode != null && tokenTimestampNode.isNumber()) {
        return tokenTimestampNode.asLong();
      }

      return null;
    } catch (IOException | IllegalArgumentException e) {
      // IOException: JSON parsing error
      // IllegalArgumentException: Base64 decoding error
      return null;
    }
  }

}
