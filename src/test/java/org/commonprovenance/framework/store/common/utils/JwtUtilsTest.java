package org.commonprovenance.framework.store.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

@DisplayName("JWT Utils Test")
public class JwtUtilsTest {

  private String JWT = "eyJhbGciOiJFUzI1NiIsImJ1bmRsZSI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC9hcGkvdjEvZG9jdW1lbnRzLzE2ZDM2ZTEwLTYyZTAtNDlmNy1hZjYyLWI0ZWM1ODljZmEyOCIsImhhc2hGdW5jdGlvbiI6IlNIQTI1NiIsInRydXN0ZWRQYXJ0eUNlcnRpZmljYXRlIjoiLS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tXG5NSUlDTWpDQ0FkaWdBd0lCQWdJVVNMajVZN1BYSVMxM3FQRVBEZGxJTkJuUXpvZ3dDZ1lJS29aSXpqMEVBd0l3XG5iVEVMTUFrR0ExVUVCaE1DUlZVeE9qQTRCZ05WQkFvTU1VUnBjM1J5YVdKMWRHVmtJRkJ5YjNabGJtRnVZMlVnXG5SR1Z0YnlCRFpYSjBhV1pwWTJGMFpTQkJkWFJvYjNKcGRIa3hJakFnQmdOVkJBTU1HVVJRUkNCRFpYSjBhV1pwXG5ZMkYwWlNCQmRYUm9iM0pwZEhrd0hoY05NalF4TVRFMk1ESTFPVFV5V2hjTk16UXhNVEUwTURJMU9UVXlXakJkXG5NUXN3Q1FZRFZRUUdFd0pEV2pFeU1EQUdBMVVFQ2d3cFJHbHpkSEpwWW5WMFpXUWdVSEp2ZG1WdVlXNWpaU0JFXG5aVzF2SUZSeWRYTjBaV1FnVUdGeWRIa3hHakFZQmdOVkJBTU1FVVJRUkNCVWNuVnpkR1ZrSUZCaGNuUjVNRmt3XG5Fd1lIS29aSXpqMENBUVlJS29aSXpqMERBUWNEUWdBRStWOGtUNGprdkVXbVgzMDFLQVM5ZWtsbW5STmk2Z1U5XG4rS0h4dVFwa1NPaE1UcTk2Q0JYRnBmb2tSZDd0NVZkclJ5MHVxWnN5U05wNWtXMGhuUU1KV2FObU1HUXdFZ1lEXG5WUjBUQVFIL0JBZ3dCZ0VCL3dJQkFEQU9CZ05WSFE4QkFmOEVCQU1DQVlZd0hRWURWUjBPQkJZRUZNQ25QUmppXG5Yb2tUN3F1d1pSQjE2QUFnejdibk1COEdBMVVkSXdRWU1CYUFGQ3lFS3dpMWp2ZFBxZmlVK05kSC9udmg3UFlaXG5NQW9HQ0NxR1NNNDlCQU1DQTBnQU1FVUNJUUN5WnJVU2hWcXJvaERxZHpkT0ZtQXlGRHB3TUFPOEk2amFodmcxXG5GUkFaWWdJZ1ZoNFMydFFuMTJYWWRkNUlTc0NwQUJzaDZacmpTaVZZcnQyVDFPMW5Rc3c9XG4tLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tXG4iLCJ0cnVzdGVkUGFydHlVcmkiOiJ0cnVzdGVkLXBhcnR5OjgwMjAiLCJ0eXAiOiJKV1QifQ.eyJhdXRob3JpdHlJZCI6IlRydXN0ZWRfUGFydHkiLCJkb2N1bWVudENyZWF0aW9uVGltZXN0YW1wIjoxNzc2MTczNzAwLCJkb2N1bWVudERpZ2VzdCI6Ijg0ODk0YzJhNGMyMThiMWM0NGUwYTgwZGVlZWYyMjI1ZWZkZjk2ZTdhZjlmZWZkNmJmNjA2ZmViNjA3OGYxM2UiLCJvcmlnaW5hdG9ySWQiOiI2ZmIyOTJhYS1lZTM4LTQ4YWUtOTk4Zi0wNzlhZDlkMDFlN2MiLCJ0b2tlblRpbWVzdGFtcCI6MTc3NjE3MzcwMH0.JgmSuZGDClRDaI50zdfxmreRYMILci6gYeLk4HmWlZLwIY0y4p5pJgyg-WxZXkWsgqylzRFV94jzNGKQbL_xyA";

  /**
   * Creates a test JWT with the given payload claims.
   * JWT format: header.payload.signature (signature is fake for testing)
   */
  private String createTestJwt(String payloadJson) {
    String header = Base64Utils.encodeBase64UrlFromString("{\"alg\":\"ES256\",\"typ\":\"JWT\"}");
    String payload = Base64Utils.encodeBase64UrlFromString(payloadJson);
    String signature = "fakeSignature";
    return header + "." + payload + "." + signature;
  }

  @Test
  @DisplayName("Should extract tokenTimestamp from valid JWT")
  void shouldExtractTokenTimestampFromJwt() {
    // Given
    // When
    Long result = JwtUtils.extractTokenTimestamp(JWT);

    // Then
    assertNotNull(result);
    assertEquals(1776173700L, result);
  }

  @Test
  @DisplayName("Should extract Token Generation Attributes from valid JWT")
  void shouldExtractTokenGenerationAttributes() {
    // Given
    // When
    Map<String, Object> result = JwtUtils.extractTokenGeneratorAttributes(JWT);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(
        "trusted-party:8020",
        result.get("trustedPartyUri"));
    assertEquals(
        "-----BEGIN CERTIFICATE-----\nMIICMjCCAdigAwIBAgIUSLj5Y7PXIS13qPEPDdlINBnQzogwCgYIKoZIzj0EAwIw\nbTELMAkGA1UEBhMCRVUxOjA4BgNVBAoMMURpc3RyaWJ1dGVkIFByb3ZlbmFuY2Ug\nRGVtbyBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkxIjAgBgNVBAMMGURQRCBDZXJ0aWZp\nY2F0ZSBBdXRob3JpdHkwHhcNMjQxMTE2MDI1OTUyWhcNMzQxMTE0MDI1OTUyWjBd\nMQswCQYDVQQGEwJDWjEyMDAGA1UECgwpRGlzdHJpYnV0ZWQgUHJvdmVuYW5jZSBE\nZW1vIFRydXN0ZWQgUGFydHkxGjAYBgNVBAMMEURQRCBUcnVzdGVkIFBhcnR5MFkw\nEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE+V8kT4jkvEWmX301KAS9eklmnRNi6gU9\n+KHxuQpkSOhMTq96CBXFpfokRd7t5VdrRy0uqZsySNp5kW0hnQMJWaNmMGQwEgYD\nVR0TAQH/BAgwBgEB/wIBADAOBgNVHQ8BAf8EBAMCAYYwHQYDVR0OBBYEFMCnPRji\nXokT7quwZRB16AAgz7bnMB8GA1UdIwQYMBaAFCyEKwi1jvdPqfiU+NdH/nvh7PYZ\nMAoGCCqGSM49BAMCA0gAMEUCIQCyZrUShVqrohDqdzdOFmAyFDpwMAO8I6jahvg1\nFRAZYgIgVh4S2tQn12XYdd5ISsCpABsh6ZrjSiVYrt2T1O1nQsw=\n-----END CERTIFICATE-----\n",
        result.get("trustedPartyCertificate"));
  }

  @Test
  @DisplayName("Should extract tokenTimestamp from valid JWT")
  void shouldExtractTokenTimestampFromValidJwt() {
    // Given
    String payload = "{\"tokenTimestamp\":1776169956,\"authorityId\":\"Test\"}";
    String jwt = createTestJwt(payload);

    // When
    Long result = JwtUtils.extractTokenTimestamp(jwt);

    // Then
    assertNotNull(result);
    assertEquals(1776169956L, result);
  }

  @Test
  @DisplayName("Should return null when tokenTimestamp is missing")
  void shouldReturnNullWhenTokenTimestampMissing() {
    // Given
    String payload = "{\"authorityId\":\"Test\",\"documentDigest\":\"abc123\"}";
    String jwt = createTestJwt(payload);

    // When
    Long result = JwtUtils.extractTokenTimestamp(jwt);

    // Then
    assertNull(result);
  }

  @Test
  @DisplayName("Should return null for null JWT")
  void shouldReturnNullForNullJwt() {
    // When
    Long result = JwtUtils.extractTokenTimestamp(null);

    // Then
    assertNull(result);
  }

  @Test
  @DisplayName("Should return null for blank JWT")
  void shouldReturnNullForBlankJwt() {
    // When
    Long result = JwtUtils.extractTokenTimestamp("   ");

    // Then
    assertNull(result);
  }

  @Test
  @DisplayName("Should return null for invalid JWT format")
  void shouldReturnNullForInvalidJwtFormat() {
    // Given - JWT without proper structure
    String invalidJwt = "not.a.valid.jwt.structure";

    // When
    Long result = JwtUtils.extractTokenTimestamp(invalidJwt);

    // Then
    assertNull(result);
  }

  @Test
  @DisplayName("Should return null for malformed JWT")
  void shouldReturnNullForMalformedJwt() {
    // Given - JWT with only one part
    String malformedJwt = "onlyOnePart";

    // When
    Long result = JwtUtils.extractTokenTimestamp(malformedJwt);

    // Then
    assertNull(result);
  }

  @Test
  @DisplayName("Should return null for non-numeric tokenTimestamp")
  void shouldReturnNullForNonNumericTokenTimestamp() {
    // Given
    String payload = "{\"tokenTimestamp\":\"not-a-number\",\"authorityId\":\"Test\"}";
    String jwt = createTestJwt(payload);

    // When
    Long result = JwtUtils.extractTokenTimestamp(jwt);

    // Then
    assertNull(result);
  }

  @Test
  @DisplayName("Should handle JWT with special characters in payload")
  void shouldHandleJwtWithSpecialCharacters() {
    // Given - JWT with various special characters
    String payload = "{\"tokenTimestamp\":1776169956,\"cert\":\"-----BEGIN CERT-----\\nMIIC...\\n-----END CERT-----\"}";
    String jwt = createTestJwt(payload);

    // When
    Long result = JwtUtils.extractTokenTimestamp(jwt);

    // Then
    assertNotNull(result);
    assertEquals(1776169956L, result);
  }

  @Test
  @DisplayName("Should handle large timestamp values")
  void shouldHandleLargeTimestampValues() {
    // Given - Far future timestamp
    String payload = "{\"tokenTimestamp\":9999999999}";
    String jwt = createTestJwt(payload);

    // When
    Long result = JwtUtils.extractTokenTimestamp(jwt);

    // Then
    assertNotNull(result);
    assertEquals(9999999999L, result);
  }

  @Test
  @DisplayName("Should handle zero timestamp")
  void shouldHandleZeroTimestamp() {
    // Given
    String payload = "{\"tokenTimestamp\":0}";
    String jwt = createTestJwt(payload);

    // When
    Long result = JwtUtils.extractTokenTimestamp(jwt);

    // Then
    assertNotNull(result);
    assertEquals(0L, result);
  }

  @Test
  @DisplayName("Should handle negative timestamp")
  void shouldHandleNegativeTimestamp() {
    // Given
    String payload = "{\"tokenTimestamp\":-1000}";
    String jwt = createTestJwt(payload);

    // When
    Long result = JwtUtils.extractTokenTimestamp(jwt);

    // Then
    assertNotNull(result);
    assertEquals(-1000L, result);
  }

  @Test
  @DisplayName("Should handle real-world JWT format")
  void shouldHandleRealWorldJwtFormat() {
    // Given - A JWT similar to what Trusted Party returns
    // This is a simplified version of the JWT from the error log
    String header = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9";
    String payload = "eyJhdXRob3JpdHlJZCI6IlRydXN0ZWRfUGFydHkiLCJ0b2tlblRpbWVzdGFtcCI6MTc3NjE2OTk1NiwiZG9jdW1lbnRDcmVhdGlvblRpbWVzdGFtcCI6MTc3NjE2OTk1Nn0";
    String signature = "fakeSignature";
    String jwt = header + "." + payload + "." + signature;

    // When
    Long result = JwtUtils.extractTokenTimestamp(jwt);

    // Then
    assertNotNull(result);
    assertEquals(1776169956L, result);
  }
}
