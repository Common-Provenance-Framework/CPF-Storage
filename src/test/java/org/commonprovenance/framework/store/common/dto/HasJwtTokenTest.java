package org.commonprovenance.framework.store.common.dto;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import org.commonprovenance.framework.store.common.utils.Base64Utils;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.HashFunction;
import org.commonprovenance.framework.store.model.Token;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.vanilla.ProvUtilities;

import io.vavr.control.Either;

@DisplayName("JWT Utils Test")
public class HasJwtTokenTest {

  private String JWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiIsInRydXN0ZWRQYXJ0eVVyaSI6InRydXN0ZWQtcGFydHk6ODAyMCIsIng1YyI6WyJNSUlDTWpDQ0FkaWdBd0lCQWdJVVNMajVZN1BYSVMxM3FQRVBEZGxJTkJuUXpvZ3dDZ1lJS29aSXpqMEVBd0l3YlRFTE1Ba0dBMVVFQmhNQ1JWVXhPakE0QmdOVkJBb01NVVJwYzNSeWFXSjFkR1ZrSUZCeWIzWmxibUZ1WTJVZ1JHVnRieUJEWlhKMGFXWnBZMkYwWlNCQmRYUm9iM0pwZEhreElqQWdCZ05WQkFNTUdVUlFSQ0JEWlhKMGFXWnBZMkYwWlNCQmRYUm9iM0pwZEhrd0hoY05NalF4TVRFMk1ESTFPVFV5V2hjTk16UXhNVEUwTURJMU9UVXlXakJkTVFzd0NRWURWUVFHRXdKRFdqRXlNREFHQTFVRUNnd3BSR2x6ZEhKcFluVjBaV1FnVUhKdmRtVnVZVzVqWlNCRVpXMXZJRlJ5ZFhOMFpXUWdVR0Z5ZEhreEdqQVlCZ05WQkFNTUVVUlFSQ0JVY25WemRHVmtJRkJoY25SNU1Ga3dFd1lIS29aSXpqMENBUVlJS29aSXpqMERBUWNEUWdBRStWOGtUNGprdkVXbVgzMDFLQVM5ZWtsbW5STmk2Z1U5K0tIeHVRcGtTT2hNVHE5NkNCWEZwZm9rUmQ3dDVWZHJSeTB1cVpzeVNOcDVrVzBoblFNSldhTm1NR1F3RWdZRFZSMFRBUUgvQkFnd0JnRUIvd0lCQURBT0JnTlZIUThCQWY4RUJBTUNBWVl3SFFZRFZSME9CQllFRk1DblBSamlYb2tUN3F1d1pSQjE2QUFnejdibk1COEdBMVVkSXdRWU1CYUFGQ3lFS3dpMWp2ZFBxZmlVK05kSC9udmg3UFlaTUFvR0NDcUdTTTQ5QkFNQ0EwZ0FNRVVDSVFDeVpyVVNoVnFyb2hEcWR6ZE9GbUF5RkRwd01BTzhJNmphaHZnMUZSQVpZZ0lnVmg0UzJ0UW4xMlhZZGQ1SVNzQ3BBQnNoNlpyalNpVllydDJUMU8xblFzdz0iXX0.eyJzdWIiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL3YxL29yZ2FuaXphdGlvbnMvNmZiMjkyYWEtZWUzOC00OGFlLTk5OGYtMDc5YWQ5ZDAxZTdjL2RvY3VtZW50cy9kYzhlZmVkMC0wMDM1LTQwMjktOTA2NS04YzQ2NjY3MTUxZGIiLCJoYXNoX2FsZyI6IlNIQTI1NiIsImRvY19kaWdlc3QiOiI0MWVhNWY5OWU1YjE5MzA5MWFhMTQ4MGY4ODMxNzdiM2M1MzU1ODM4M2E4NTE2Y2IyZGUxNmE0ZmRjOThiOWQyIiwib3JnX2lkIjoiNmZiMjkyYWEtZWUzOC00OGFlLTk5OGYtMDc5YWQ5ZDAxZTdjIiwiaXNzIjoiVHJ1c3RlZFBhcnR5IiwiaWF0IjoxNzgzNjAyMTc1LCJkb2NfaWF0IjoxNzgzNTk0OTc0fQ.BJFBeXknoVWvt3e6DzAKPjGNntHA7vNDetB8KPsYLcqN893nFlywoZknahsvDKJGDqHUotbdNHpBKeFuloHcng";

  /**
   * Creates a test JWT with the given payload claims. JWT format: header.payload.signature (signature is fake for testing)
   */
  private Either<ApplicationException, Token> createTestJwt(String payloadJson) {
    return EITHER.combine(
        Base64Utils.encodeBase64UrlFromString("{\"alg\":\"ES256\",\"typ\":\"JWT\"}"),
        Base64Utils.encodeBase64UrlFromString(payloadJson),
        (header, payload) -> header + "." + payload + ".fakeSignature")
        .map((new Token())::withJwt);
  }

  private <T> void handleRightNotExpected(T value) {
    fail("Right side has not been expected! Got: " + value.toString());
  }

  private void handleLeftNotExpected(ApplicationException exception) {
    fail("Left side has not been expected! Got: " + exception.getMessage(), exception);
  }

  private Consumer<ApplicationException> handleLeftExpected(String expected) {
    return (ApplicationException exception) -> {
      assertNotNull(exception);
      assertInstanceOf(ApplicationException.class, exception);
      assertInstanceOf(InvalidValueException.class, exception);
      assertEquals(expected, exception.getMessage());
    };
  }

  @Test
  @DisplayName("Should extract tokenTimestamp from valid JWT")
  void shouldExtractTokenTimestampFromJwt() {

    Either.<ApplicationException, String> right(JWT)
        .map((new Token())::withJwt)
        .flatMap(Token::getTokenTimestamp)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertEquals(Date.from(Instant.ofEpochSecond(1783602175L)), result))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Should extract tokenTimestamp from valid JWT as Long")
  void shouldExtractTokenTimestampFromJwtAsLong() {

    Either.<ApplicationException, String> right(JWT)
        .map((new Token())::withJwt)
        .flatMap(Token::getTokenTimestampAsLong)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertEquals(1783602175L, result))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Should extract tokenTimestamp from valid JWT as XMLGregorianCalendar")
  void shouldExtractTokenTimestampFromJwtAsXMLGregorianCalendar() {

    Either.<ApplicationException, String> right(JWT)
        .map((new Token())::withJwt)
        .flatMap(Token::getTokenTimestampAsXMLGregorianCalendar)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertEquals(
            ProvUtilities.toXMLGregorianCalendar(Date.from(Instant.ofEpochSecond(1783602175L))),
            result))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Should extract tokenTimestamp from valid JWT as String")
  void shouldExtractTokenTimestampFromJwtAsString() {

    Either.<ApplicationException, String> right(JWT)
        .map((new Token())::withJwt)
        .flatMap(Token::getTokenTimestampAsString)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertEquals(
            ProvUtilities.toXMLGregorianCalendar(Date.from(Instant.ofEpochSecond(1783602175L))).toString(),
            result))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Should extract hash function from valid JWT")
  void shouldExtractHashFunctionFromToken() {
    Either.<ApplicationException, String> right(JWT)
        .map((new Token())::withJwt)
        .flatMap(Token::getHashFunction)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertEquals(
            HashFunction.SHA256,
            result))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Should extract document digest from valid JWT")
  void shouldExtractDocumentDigestFromToken() {
    Either.<ApplicationException, String> right(JWT)
        .map((new Token())::withJwt)
        .flatMap(Token::getDocumentDigest)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertEquals(
            "41ea5f99e5b193091aa1480f883177b3c53558383a8516cb2de16a4fdc98b9d2",
            result))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Should extract organization id from valid JWT")
  void shouldExtractOrganizationIdFromToken() {
    Either.<ApplicationException, String> right(JWT)
        .map((new Token())::withJwt)
        .flatMap(Token::getOrganizationId)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertEquals(
            "6fb292aa-ee38-48ae-998f-079ad9d01e7c",
            result))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Should extract bundle identifier from valid JWT")
  void shouldExtractBundleIdentifierFromToken() {
    Either.<ApplicationException, String> right(JWT)
        .map((new Token())::withJwt)
        .flatMap(Token::getBundleIdentifier)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertEquals(
            "dc8efed0-0035-4029-9065-8c46667151db",
            result))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Should extract TrustedParty URI from valid JWT")
  void shouldExtractTrustedPartyUriFromToken() {
    Either.<ApplicationException, String> right(JWT)
        .map((new Token())::withJwt)
        .flatMap(Token::getTrustedPartyUri)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertEquals(
            "trusted-party:8020",
            result))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Should extract Certificate chain from valid JWT")
  void shouldExtractCertificateChainFromToken() {
    Either.<ApplicationException, String> right(JWT)
        .map((new Token())::withJwt)
        .flatMap(Token::getCertChain)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertInstanceOf(List.class, result))
        .peek(result -> assertEquals(
            1,
            result.size()))
        .peek(result -> assertEquals(
            "-----BEGIN CERTIFICATE-----\n" +
                "MIICMjCCAdigAwIBAgIUSLj5Y7PXIS13qPEPDdlINBnQzogwCgYIKoZIzj0EAwIw\n" +
                "bTELMAkGA1UEBhMCRVUxOjA4BgNVBAoMMURpc3RyaWJ1dGVkIFByb3ZlbmFuY2Ug\n" +
                "RGVtbyBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkxIjAgBgNVBAMMGURQRCBDZXJ0aWZp\n" +
                "Y2F0ZSBBdXRob3JpdHkwHhcNMjQxMTE2MDI1OTUyWhcNMzQxMTE0MDI1OTUyWjBd\n" +
                "MQswCQYDVQQGEwJDWjEyMDAGA1UECgwpRGlzdHJpYnV0ZWQgUHJvdmVuYW5jZSBE\n" +
                "ZW1vIFRydXN0ZWQgUGFydHkxGjAYBgNVBAMMEURQRCBUcnVzdGVkIFBhcnR5MFkw\n" +
                "EwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE+V8kT4jkvEWmX301KAS9eklmnRNi6gU9\n" +
                "+KHxuQpkSOhMTq96CBXFpfokRd7t5VdrRy0uqZsySNp5kW0hnQMJWaNmMGQwEgYD\n" +
                "VR0TAQH/BAgwBgEB/wIBADAOBgNVHQ8BAf8EBAMCAYYwHQYDVR0OBBYEFMCnPRji\n" +
                "XokT7quwZRB16AAgz7bnMB8GA1UdIwQYMBaAFCyEKwi1jvdPqfiU+NdH/nvh7PYZ\n" +
                "MAoGCCqGSM49BAMCA0gAMEUCIQCyZrUShVqrohDqdzdOFmAyFDpwMAO8I6jahvg1\n" +
                "FRAZYgIgVh4S2tQn12XYdd5ISsCpABsh6ZrjSiVYrt2T1O1nQsw=\n" +
                "-----END CERTIFICATE-----",
            result.getFirst().trim()))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Should extract Token Generation Attributes from valid JWT")
  void shouldExtractTokenGenerationAttributes() {
    Either.<ApplicationException, String> right(JWT)
        .map((new Token())::withJwt)
        .flatMap(Token::getTokenGeneratorAttributes)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertEquals(2, result.size()))
        .peek(result -> assertEquals(
            "trusted-party:8020",
            result.get("trustedPartyUri")))
        .peek(result -> {
          Object trustedPartyCertificate = result.get("trustedPartyCertificate");
          assertInstanceOf(List.class, trustedPartyCertificate);
          List<?> certChain = (List<?>) trustedPartyCertificate;
          assertEquals(1, certChain.size());
          assertInstanceOf(String.class, certChain.getFirst());
        })
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Should extract Token generator (Issuer) from valid JWT")
  void shouldExtractIssuerFromToken() {
    Either.<ApplicationException, String> right(JWT)
        .map((new Token())::withJwt)
        .flatMap(Token::getTokenGeneratorIdentifier)
        .peek(result -> assertNotNull(result))
        .peek(result -> assertEquals(
            "TrustedParty",
            result))
        .peekLeft(this::handleLeftNotExpected);
  }

  @Test
  @DisplayName("Should return Either with exact InvalidValueException in Left side, if tokenTimestamp is missing")
  void shouldBeLeftWhenTokenTimestampMissing() {
    Either.<ApplicationException, String> right("{\"iss\":\"Test\",\"doc_digest\":\"41ea5f99e5b193091aa1480f883177b3c53558383a8516cb2de16a4fdc98b9d2\"}")
        .flatMap(this::createTestJwt)
        .flatMap((Token::getTokenTimestamp))
        .peek(this::handleRightNotExpected)
        .peekLeft(exception -> assertInstanceOf(InvalidValueException.class, exception))
        .peekLeft(this.handleLeftExpected("IssueTime is not specified in JWT Token payload!"));
  }

  @Test
  @DisplayName("Should return Either with exact InvalidValueException in Left side, if null JWT")
  void shoulBeLeftForNullJwt() {
    Either.<ApplicationException, String> right(null)
        .map((new Token()::withJwt))
        .flatMap(Token::getTokenTimestamp)
        .peek(this::handleRightNotExpected)
        .peekLeft(this.handleLeftExpected("JWT Token can not be null!"));
  }

  @Test
  @DisplayName("Should return Either with exact InvalidValueException in Left side, if blank JWT")
  void shouldBeLeftForBlankJwt() {
    Either.<ApplicationException, String> right(" ")
        .map((new Token()::withJwt))
        .flatMap(Token::getTokenTimestamp)
        .peek(this::handleRightNotExpected)
        .peekLeft(this.handleLeftExpected("JWT Token can not be blank String."));
  }

  @Test
  @DisplayName("Should return Either with exact InvalidValueException in Left side, if invalid JWT format")
  void shouldBeLeftForInvalidJwtFormat() {
    Either.<ApplicationException, String> right("not.a.valid.jwt.structure")
        .map((new Token()::withJwt))
        .flatMap(Token::getTokenTimestamp)
        .peek(this::handleRightNotExpected)
        .peekLeft(this.handleLeftExpected("Error while parse JWT 'not.a.valid.jwt.structure'!"))
        .peekLeft(left -> assertNotNull(left.getCause()))
        .peekLeft(left -> assertInstanceOf(ParseException.class, left.getCause()))
        .peekLeft(left -> assertEquals("Unexpected number of Base64URL parts, must be three", left.getCause().getMessage()));
  }

  @Test
  @DisplayName("Should return Either with exact InvalidValueException in Left side, if malformed JWT")
  void shouldBeLeftForMalformedJwt() {
    Either.<ApplicationException, String> right("onlyOnePart")
        .map((new Token()::withJwt))
        .flatMap(Token::getTokenTimestamp)
        .peek(this::handleRightNotExpected)
        .peekLeft(this.handleLeftExpected("Error while parse JWT 'onlyOnePart'!"))
        .peekLeft(left -> assertNotNull(left.getCause()))
        .peekLeft(left -> assertInstanceOf(ParseException.class, left.getCause()))
        .peekLeft(left -> assertEquals("Invalid serialized unsecured/JWS/JWE object: Missing part delimiters", left.getCause().getMessage()));
  }

  @Test
  @DisplayName("Should return Either with exact InvalidValueException in Left side, if non-numeric tokenTimestamp")
  void shouldReturnNullForNonNumericTokenTimestamp() {
    Either.<ApplicationException, String> right("{\"iat\":\"not-a-number\",\"iss\":\"Test\"}")
        .flatMap(this::createTestJwt)
        .flatMap(Token::getTokenTimestamp)
        .peek(this::handleRightNotExpected)
        .peekLeft(this.handleLeftExpected("Error while getting JWT claims!"))
        .peekLeft(left -> assertNotNull(left.getCause()))
        .peekLeft(left -> assertInstanceOf(ParseException.class, left.getCause()))
        .peekLeft(left -> assertEquals("Unexpected type of JSON object member iat", left.getCause().getMessage()));
  }
}
