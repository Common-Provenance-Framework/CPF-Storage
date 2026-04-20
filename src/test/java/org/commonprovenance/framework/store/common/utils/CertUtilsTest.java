package org.commonprovenance.framework.store.common.utils;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.vavr.control.Either;

@DisplayName("Pem Utils Test")
public class CertUtilsTest {
  private final String PRIV_KEY_VALUE = "MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgYE5KM9NYsL7H1rTj6XWJiDLYO4lFfXv6xGbZqyRqWd2hRANCAATnkiytLMZoASPFbyOCz2HLoVeF3Xv+2pHgSXfuvYMzFWrdjOs2V27stRYgIVI85zGvNGrCQae1FyNrgwDJOdnO";
  private final String PRIV_KEY_FILE_CONTENT = """
      -----BEGIN PRIVATE KEY-----
      MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgYE5KM9NYsL7H1rTj
      6XWJiDLYO4lFfXv6xGbZqyRqWd2hRANCAATnkiytLMZoASPFbyOCz2HLoVeF3Xv+
      2pHgSXfuvYMzFWrdjOs2V27stRYgIVI85zGvNGrCQae1FyNrgwDJOdnO
      -----END PRIVATE KEY-----
      """;

  private final String PEM_CERTIFICATE = """
      -----BEGIN CERTIFICATE-----
      MIICMDCCAdWgAwIBAgIUFee7S+vA93BqXXNGsrlEhAPdHfkwCgYIKoZIzj0EAwIw
      YzELMAkGA1UEBhMCQ1oxNTAzBgNVBAoMLERpc3RyaWJ1dGVkIFByb3ZlbmFuY2Ug
      RGVtbyBJbnRlcm1lZGlhdGUgVHdvMR0wGwYDVQQDDBREUEQgSW50ZXJtZWRpYXRl
      IFR3bzAeFw0yNTA1MDgxODQ4MDlaFw0zNTA1MDYxODQ4MDlaMEsxCzAJBgNVBAYT
      AlNLMSkwJwYDVQQKDCBEaXN0cmlidXRlZCBQcm92ZW5hbmNlIERlbW8gT1JHMTER
      MA8GA1UEAwwIRFBEIE9SRzEwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAATnkiyt
      LMZoASPFbyOCz2HLoVeF3Xv+2pHgSXfuvYMzFWrdjOs2V27stRYgIVI85zGvNGrC
      Qae1FyNrgwDJOdnOo38wfTAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQEAwIBojAd
      BgNVHQ4EFgQUyGnSPPl7NxTsqPfepuNB222Ily4wHQYDVR0lBBYwFAYIKwYBBQUH
      AwIGCCsGAQUFBwMBMB8GA1UdIwQYMBaAFIl9rtw6uPW5e+Ol0F2WlbbGNpeaMAoG
      CCqGSM49BAMCA0kAMEYCIQD7UyLiEMxGUrsOKUAp9fb8XyoEhaYAwB3p/QcQJHfO
      xAIhAPjZszEH4rYc5bhtojbLIKz+v0UD0bd8wF0Q4tG1Cti4
      -----END CERTIFICATE-----
      """;

  private void handleLeft(ApplicationException appException) {
    fail("Left side has not been expected: " + appException.getMessage(), appException);
  }

  @Test
  @DisplayName("should preprocess 'pkcs8.key' file content")
  public void shouldPreprocessFileContent() {
    assertEquals(
        this.PRIV_KEY_VALUE,
        CertUtils.preprocessFileContent(this.PRIV_KEY_FILE_CONTENT));
  }

  @Test
  @DisplayName("should load valid private key from .key file")
  public void shouldReturnPrivKeyFromKeyFile_EC() {
    // Load EC private key (SEC1 converted to PKCS#8)
    Either<ApplicationException, ECPrivateKey> ecPkOrException = CertUtils.loadPrivateKey(this.PRIV_KEY_VALUE)
        .peek(key -> assertNotNull(key, "should not be a NULL - private key"))
        .peek(key -> assertTrue(key instanceof ECPrivateKey, "should be instanceof ECPrivateKey - private key"))
        .map(ECPrivateKey.class::cast)
        .peekLeft(this::handleLeft);

    // Check curve
    ecPkOrException
        .map(ECPrivateKey::getParams)
        .peek(params -> assertEquals(256, params.getCurve().getField().getFieldSize(),
            "should be 256 bits long - the private key field size"));

    // Extract private scalar. Equivalent to OpenSSL:
    // `openssl ec -in ./cpf-utils/src/test/resources/cert/org_pkcs8.key -text
    // -noout`
    String expected = "60:4e:4a:33:d3:58:b0:be:c7:d6:b4:e3:e9:75:89:88:32:d8:3b:89:45:7d:7b:fa:c4:66:d9:ab:24:6a:59:dd"
        .replace(":", "");

    ecPkOrException
        .map(ECPrivateKey::getS)
        .peek(s -> assertNotNull(s))
        .peek(s -> assertEquals(1, s.signum()))
        .peek(s -> assertEquals(expected, s.toString(16), "should be equal to private key loaded by openssl"));
  }

  @Test
  @DisplayName("should load valid public key from .pem files")
  public void shouldReturnPubKeyFromPemFile_EC() {
    // Derive public key from private key (expected value)
    Either<ApplicationException, ECPrivateKey> keyOrException = CertUtils.loadPrivateKey(this.PRIV_KEY_VALUE)
        .map(ECPrivateKey.class::cast)
        .peekLeft(this::handleLeft);

    Either<ApplicationException, ECPublicKey> derivedPublicKeyOrException = keyOrException
        .flatMap(CertUtils::derivePublicKey)
        .peekLeft(this::handleLeft);

    // Load public key from certificate
    Either<ApplicationException, ECPublicKey> certPublicKeyOrException = CertUtils.loadPublicKey(PEM_CERTIFICATE)
        .peekLeft(this::handleLeft)
        .peek(certPublicKey -> assertTrue(certPublicKey instanceof ECPublicKey, "should be instanceof ECPublicKey"))
        .map(ECPublicKey.class::cast);

    // Compare public key points
    EITHER.zip(derivedPublicKeyOrException, certPublicKeyOrException)
        .peek(tuple -> assertEquals(
            tuple._1.getW(),
            tuple._2.getW(),
            "Public key derived from private key must match certificate - Compare public key points"))
        .peek(tuple -> assertEquals(
            tuple._1.getAlgorithm(),
            tuple._2.getAlgorithm(),
            "Public key derived from private key must match certificate - Compare public key algorithm"))
        .peek(tuple -> assertEquals(
            tuple._1.getFormat(),
            tuple._2.getFormat(),
            "Public key derived from private key must match certificate - Compare public key format"))
        .peek(tuple -> assertEquals(
            tuple._1.getParams().getGenerator(),
            tuple._2.getParams().getGenerator(),
            "Public key derived from private key must match certificate - Compare public key Generator Point"))
        .flatMap(tuple -> EITHER.zip(
            BytesUtils.bytesToHex(tuple._1.getEncoded()),
            BytesUtils.bytesToHex(tuple._2.getEncoded())))
        .peek(tuple -> assertEquals(tuple._1, tuple._2));

  };

  @Test
  @DisplayName("should load certificate from String")
  public void shouldReturnPubKeyFromString_EC() {
    // Load public key from certificate
    // String pem = FileUtils.readFileString("org.pem", "cert");
    String pem = """
        -----BEGIN CERTIFICATE-----
          MIICMDCCAdWgAwIBAgIUFee7S+vA93BqXXNGsrlEhAPdHfkwCgYIKoZIzj0EAwIw
          YzELMAkGA1UEBhMCQ1oxNTAzBgNVBAoMLERpc3RyaWJ1dGVkIFByb3ZlbmFuY2Ug
          RGVtbyBJbnRlcm1lZGlhdGUgVHdvMR0wGwYDVQQDDBREUEQgSW50ZXJtZWRpYXRl
          IFR3bzAeFw0yNTA1MDgxODQ4MDlaFw0zNTA1MDYxODQ4MDlaMEsxCzAJBgNVBAYT
          AlNLMSkwJwYDVQQKDCBEaXN0cmlidXRlZCBQcm92ZW5hbmNlIERlbW8gT1JHMTER
          MA8GA1UEAwwIRFBEIE9SRzEwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAATnkiyt
          LMZoASPFbyOCz2HLoVeF3Xv+2pHgSXfuvYMzFWrdjOs2V27stRYgIVI85zGvNGrC
          Qae1FyNrgwDJOdnOo38wfTAMBgNVHRMBAf8EAjAAMA4GA1UdDwEB/wQEAwIBojAd
          BgNVHQ4EFgQUyGnSPPl7NxTsqPfepuNB222Ily4wHQYDVR0lBBYwFAYIKwYBBQUH
          AwIGCCsGAQUFBwMBMB8GA1UdIwQYMBaAFIl9rtw6uPW5e+Ol0F2WlbbGNpeaMAoG
          CCqGSM49BAMCA0kAMEYCIQD7UyLiEMxGUrsOKUAp9fb8XyoEhaYAwB3p/QcQJHfO
          xAIhAPjZszEH4rYc5bhtojbLIKz+v0UD0bd8wF0Q4tG1Cti4
          -----END CERTIFICATE-----
          """;
    CertUtils.loadCertificate(pem)
        .peek(cert -> assertTrue(cert.getPublicKey() instanceof ECPublicKey, "should be instanceof ECPublicKey"));

  };

  @Test
  @DisplayName("should return algorithm from .pem file")
  public void shouldReturnAlgorithmFromPemFile() {
    CertUtils.getAlgorithm(PEM_CERTIFICATE)
        .peek(algorithm -> assertEquals("SHA256withECDSA", algorithm));
  }

  @Test
  @DisplayName("should return certificate from .pem file")
  public void shouldReturnCertFromPemFile() {
    CertUtils.loadCertificate(PEM_CERTIFICATE)
        .peek(cert -> assertEquals("EC", cert.getPublicKey().getAlgorithm(),
            "should be 'EC' - the name of the algorithm associated with this public key"))
        .peek(cert -> assertEquals("X.509", cert.getPublicKey().getFormat(),
            "should be X.509 - the primary encoding format of the public key"))
        .peek(cert -> assertEquals("X.509", cert.getType(),
            "should be X.509 - the type of this certificate"));
  }

  @Test
  @DisplayName("should return true if massage has been sign by valid private key")
  public void testEcdsaSignatureVerification() {
    // Load keys
    Either<ApplicationException, PrivateKey> privateKeyOrException = CertUtils.loadPrivateKey(this.PRIV_KEY_VALUE);
    Either<ApplicationException, PublicKey> publicKeyOrException = CertUtils.loadPublicKey(PEM_CERTIFICATE);
    Either<ApplicationException, PublicKey> derivedPublicKeyOrException = privateKeyOrException
        .map(ECPrivateKey.class::cast)
        .flatMap(CertUtils::derivePublicKey);

    // Set message
    Either<ApplicationException, byte[]> messageOrException = BytesUtils.stringToBytes_UTF8("Hello world!");

    // Sign
    Either<ApplicationException, byte[]> signatureOrException = EITHER.combineM(
        messageOrException,
        privateKeyOrException,
        CertUtils.sign("SHA256withECDSA"))
        .peek(signature -> assertNotNull(signature, "should not be NULL - signature"))
        .peek(signature -> assertTrue(signature.length > 0, "should not be EMPTY - signature"));

    // Verify
    EITHER.combineM(messageOrException, signatureOrException, publicKeyOrException,
        CertUtils.verify("SHA256withECDSA"))
        .peek(isValid -> assertTrue(isValid, "should be true - check signature with public key from cert"));

    EITHER.combineM(messageOrException, signatureOrException, derivedPublicKeyOrException,
        CertUtils.verify("SHA256withECDSA"))
        .peek(isValid -> assertTrue(isValid,
            "should be true - check signature with public key derived from private key"));
  }
}
