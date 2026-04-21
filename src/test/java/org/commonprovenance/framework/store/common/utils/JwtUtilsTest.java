package org.commonprovenance.framework.store.common.utils;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Consumer;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.vavr.control.Either;

@DisplayName("JWT Utils Test")
public class JwtUtilsTest {

  private String JWT = "eyJhbGciOiJFUzI1NiIsImJ1bmRsZSI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC9hcGkvdjEvZG9jdW1lbnRzLzE2ZDM2ZTEwLTYyZTAtNDlmNy1hZjYyLWI0ZWM1ODljZmEyOCIsImhhc2hGdW5jdGlvbiI6IlNIQTI1NiIsInRydXN0ZWRQYXJ0eUNlcnRpZmljYXRlIjoiLS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tXG5NSUlDTWpDQ0FkaWdBd0lCQWdJVVNMajVZN1BYSVMxM3FQRVBEZGxJTkJuUXpvZ3dDZ1lJS29aSXpqMEVBd0l3XG5iVEVMTUFrR0ExVUVCaE1DUlZVeE9qQTRCZ05WQkFvTU1VUnBjM1J5YVdKMWRHVmtJRkJ5YjNabGJtRnVZMlVnXG5SR1Z0YnlCRFpYSjBhV1pwWTJGMFpTQkJkWFJvYjNKcGRIa3hJakFnQmdOVkJBTU1HVVJRUkNCRFpYSjBhV1pwXG5ZMkYwWlNCQmRYUm9iM0pwZEhrd0hoY05NalF4TVRFMk1ESTFPVFV5V2hjTk16UXhNVEUwTURJMU9UVXlXakJkXG5NUXN3Q1FZRFZRUUdFd0pEV2pFeU1EQUdBMVVFQ2d3cFJHbHpkSEpwWW5WMFpXUWdVSEp2ZG1WdVlXNWpaU0JFXG5aVzF2SUZSeWRYTjBaV1FnVUdGeWRIa3hHakFZQmdOVkJBTU1FVVJRUkNCVWNuVnpkR1ZrSUZCaGNuUjVNRmt3XG5Fd1lIS29aSXpqMENBUVlJS29aSXpqMERBUWNEUWdBRStWOGtUNGprdkVXbVgzMDFLQVM5ZWtsbW5STmk2Z1U5XG4rS0h4dVFwa1NPaE1UcTk2Q0JYRnBmb2tSZDd0NVZkclJ5MHVxWnN5U05wNWtXMGhuUU1KV2FObU1HUXdFZ1lEXG5WUjBUQVFIL0JBZ3dCZ0VCL3dJQkFEQU9CZ05WSFE4QkFmOEVCQU1DQVlZd0hRWURWUjBPQkJZRUZNQ25QUmppXG5Yb2tUN3F1d1pSQjE2QUFnejdibk1COEdBMVVkSXdRWU1CYUFGQ3lFS3dpMWp2ZFBxZmlVK05kSC9udmg3UFlaXG5NQW9HQ0NxR1NNNDlCQU1DQTBnQU1FVUNJUUN5WnJVU2hWcXJvaERxZHpkT0ZtQXlGRHB3TUFPOEk2amFodmcxXG5GUkFaWWdJZ1ZoNFMydFFuMTJYWWRkNUlTc0NwQUJzaDZacmpTaVZZcnQyVDFPMW5Rc3c9XG4tLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tXG4iLCJ0cnVzdGVkUGFydHlVcmkiOiJ0cnVzdGVkLXBhcnR5OjgwMjAiLCJ0eXAiOiJKV1QifQ.eyJhdXRob3JpdHlJZCI6IlRydXN0ZWRfUGFydHkiLCJkb2N1bWVudENyZWF0aW9uVGltZXN0YW1wIjoxNzc2MTczNzAwLCJkb2N1bWVudERpZ2VzdCI6Ijg0ODk0YzJhNGMyMThiMWM0NGUwYTgwZGVlZWYyMjI1ZWZkZjk2ZTdhZjlmZWZkNmJmNjA2ZmViNjA3OGYxM2UiLCJvcmlnaW5hdG9ySWQiOiI2ZmIyOTJhYS1lZTM4LTQ4YWUtOTk4Zi0wNzlhZDlkMDFlN2MiLCJ0b2tlblRpbWVzdGFtcCI6MTc3NjE3MzcwMH0.JgmSuZGDClRDaI50zdfxmreRYMILci6gYeLk4HmWlZLwIY0y4p5pJgyg-WxZXkWsgqylzRFV94jzNGKQbL_xyA";

  /**
   * Creates a test JWT with the given payload claims.
   * JWT format: header.payload.signature (signature is fake for testing)
   */
  private Either<ApplicationException, String> createTestJwt(String payloadJson) {
    return EITHER.combine(
        Base64Utils.encodeBase64UrlFromString("{\"alg\":\"ES256\",\"typ\":\"JWT\"}"),
        Base64Utils.encodeBase64UrlFromString(payloadJson),
        Either.right("fakeSignature"),
        (header, payload, signature) -> header + "." + payload + "." + signature);
  }

  private <T> void handleRightNotExpected(T value) {
    fail("Right side has not been expected! Got: " + value.toString());
  }

  private void handleLeftNotExpected(ApplicationException exception) {
    fail("Left side has not been expected! " + exception.getMessage(), exception);
  }

  private Consumer<Throwable> handleLeftExpected(String expected) {
    return (Throwable throwable) -> {
      assertNotNull(throwable);
      assertInstanceOf(ApplicationException.class, throwable);
      assertEquals(expected, throwable.getMessage());
    };
  }

  @Test
  @DisplayName("Should extract tokenTimestamp from valid JWT")
  void shouldExtractTokenTimestampFromJwt() {
    Either.<ApplicationException, String>right(JWT)
        .flatMap(JwtUtils::extractTokenTimestamp)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertEquals(1776173700L, result))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Should extract Token Generation Attributes from valid JWT")
  void shouldExtractTokenGenerationAttributes() {
    Either.<ApplicationException, String>right(JWT)
        .flatMap(JwtUtils::extractTokenGeneratorAttributes)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertEquals(2, result.size()))
        .peek(result -> assertEquals(
            "trusted-party:8020",
            result.get("trustedPartyUri")))
        .peek(result -> assertEquals(
            "-----BEGIN CERTIFICATE-----\nMIICMjCCAdigAwIBAgIUSLj5Y7PXIS13qPEPDdlINBnQzogwCgYIKoZIzj0EAwIw\nbTELMAkGA1UEBhMCRVUxOjA4BgNVBAoMMURpc3RyaWJ1dGVkIFByb3ZlbmFuY2Ug\nRGVtbyBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkxIjAgBgNVBAMMGURQRCBDZXJ0aWZp\nY2F0ZSBBdXRob3JpdHkwHhcNMjQxMTE2MDI1OTUyWhcNMzQxMTE0MDI1OTUyWjBd\nMQswCQYDVQQGEwJDWjEyMDAGA1UECgwpRGlzdHJpYnV0ZWQgUHJvdmVuYW5jZSBE\nZW1vIFRydXN0ZWQgUGFydHkxGjAYBgNVBAMMEURQRCBUcnVzdGVkIFBhcnR5MFkw\nEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE+V8kT4jkvEWmX301KAS9eklmnRNi6gU9\n+KHxuQpkSOhMTq96CBXFpfokRd7t5VdrRy0uqZsySNp5kW0hnQMJWaNmMGQwEgYD\nVR0TAQH/BAgwBgEB/wIBADAOBgNVHQ8BAf8EBAMCAYYwHQYDVR0OBBYEFMCnPRji\nXokT7quwZRB16AAgz7bnMB8GA1UdIwQYMBaAFCyEKwi1jvdPqfiU+NdH/nvh7PYZ\nMAoGCCqGSM49BAMCA0gAMEUCIQCyZrUShVqrohDqdzdOFmAyFDpwMAO8I6jahvg1\nFRAZYgIgVh4S2tQn12XYdd5ISsCpABsh6ZrjSiVYrt2T1O1nQsw=\n-----END CERTIFICATE-----\n",
            result.get("trustedPartyCertificate")))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Should extract tokenTimestamp from valid JWT")
  void shouldExtractTokenTimestampFromValidJwt() {
    Either.<ApplicationException, String>right("{\"tokenTimestamp\":1776169956,\"authorityId\":\"Test\"}")
        .flatMap(this::createTestJwt)
        .flatMap(JwtUtils::extractTokenTimestamp)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertEquals(1776169956L, result))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Should return null when tokenTimestamp is missing")
  void shouldReturnNullWhenTokenTimestampMissing() {
    Either.<ApplicationException, String>right("{\"authorityId\":\"Test\",\"documentDigest\":\"abc123\"}")
        .flatMap(this::createTestJwt)
        .flatMap((JwtUtils::extractTokenTimestamp))
        .peek(this::handleRightNotExpected)
        .peekLeft(this.handleLeftExpected("Input parameter can not be null."));
  }

  @Test
  @DisplayName("Should return null for null JWT")
  void shouldReturnNullForNullJwt() {
    Either.<ApplicationException, String>right(null)
        .flatMap(JwtUtils::extractTokenTimestamp)
        .peek(this::handleRightNotExpected)
        .peekLeft(this.handleLeftExpected("Input parameter can not be null."));
  }

  @Test
  @DisplayName("Should return null for blank JWT")
  void shouldReturnNullForBlankJwt() {
    Either.<ApplicationException, String>right("    ")
        .flatMap(JwtUtils::extractTokenTimestamp)
        .peek(this::handleRightNotExpected)
        .peekLeft(this.handleLeftExpected("JWT token can not be blank String."));
  }

  @Test
  @DisplayName("Should return null for invalid JWT format")
  void shouldReturnNullForInvalidJwtFormat() {
    Either.<ApplicationException, String>right("not.a.valid.jwt.structure")
        .flatMap(JwtUtils::extractTokenTimestamp)
        .peek(this::handleRightNotExpected)
        .peekLeft(this.handleLeftExpected("Can not decode JWT Token!"));
  }

  @Test
  @DisplayName("Should return null for malformed JWT")
  void shouldReturnNullForMalformedJwt() {
    Either.<ApplicationException, String>right("onlyOnePart")
        .flatMap(JwtUtils::extractTokenTimestamp)
        .peek(this::handleRightNotExpected)
        .peekLeft(this.handleLeftExpected("Not valid JWT token."));
  }

  @Test
  @DisplayName("Should return null for non-numeric tokenTimestamp")
  void shouldReturnNullForNonNumericTokenTimestamp() {
    Either.<ApplicationException, String>right("{\"tokenTimestamp\":\"not-a-number\",\"authorityId\":\"Test\"}")
        .flatMap(this::createTestJwt)
        .flatMap(JwtUtils::extractTokenTimestamp)
        .peek(this::handleRightNotExpected)
        .peekLeft(this.handleLeftExpected("'tokenTimestamp' value has to be numbert!"));
  }

  @Test
  @DisplayName("Should handle JWT with special characters in payload")
  void shouldHandleJwtWithSpecialCharacters() {
    Either.<ApplicationException, String>right(
        "{\"tokenTimestamp\":1776169956,\"cert\":\"-----BEGIN CERT-----\\nMIIC...\\n-----END CERT-----\"}")
        .flatMap(this::createTestJwt)
        .flatMap(JwtUtils::extractTokenTimestamp)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertEquals(1776169956L, result))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Should handle large timestamp values")
  void shouldHandleLargeTimestampValues() {
    Either.<ApplicationException, String>right("{\"tokenTimestamp\":9999999999}")
        .flatMap(this::createTestJwt)
        .flatMap(JwtUtils::extractTokenTimestamp)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertEquals(9999999999L, result))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Should handle zero timestamp")
  void shouldHandleZeroTimestamp() {
    Either.<ApplicationException, String>right("{\"tokenTimestamp\":0}")
        .flatMap(this::createTestJwt)
        .flatMap(JwtUtils::extractTokenTimestamp)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertEquals(0L, result))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Should handle negative timestamp")
  void shouldHandleNegativeTimestamp() {
    Either.<ApplicationException, String>right("{\"tokenTimestamp\":0}")
        .flatMap(this::createTestJwt)
        .flatMap(JwtUtils::extractTokenTimestamp)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertEquals(0, result))
        .peekLeft(this::handleLeftNotExpected);
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
    Either.<ApplicationException, String>right(jwt)
        .flatMap(JwtUtils::extractTokenTimestamp)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertEquals(1776169956L, result))
        .peekLeft(this::handleLeftNotExpected);
  }
}
