package org.commonprovenance.framework.storage.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    try {
      // Load EC private key (SEC1 converted to PKCS#8)
      PrivateKey key = CertUtils.loadPrivateKey(this.PRIV_KEY_VALUE);

      assertNotNull(key, "should not be a NULL - private key");
      assertTrue(key instanceof ECPrivateKey, "should be instanceof ECPrivateKey - private key");

      ECPrivateKey ecPrivateKey = (ECPrivateKey) key;

      // Check curve
      ECParameterSpec params = ecPrivateKey.getParams();
      assertEquals(256, params.getCurve().getField().getFieldSize(),
          "should be 256 bits long - the private key field size");

      // Extract private scalar. Equivalent to OpenSSL:
      // `openssl ec -in ./cpf-utils/src/test/resources/cert/org_pkcs8.key -text
      // -noout`
      String expected = "60:4e:4a:33:d3:58:b0:be:c7:d6:b4:e3:e9:75:89:88:32:d8:3b:89:45:7d:7b:fa:c4:66:d9:ab:24:6a:59:dd"
          .replace(":", "");

      BigInteger d = ecPrivateKey.getS();
      assertNotNull(d);
      assertEquals(1, d.signum());
      assertEquals(expected, d.toString(16), "should be equal to private key loaded by openssl");
    } catch (Exception e) {
      fail(e.getMessage(), e.getCause());
    }
  }

  @Test
  @DisplayName("should load valid public key from .pem files")
  public void shouldReturnPubKeyFromPemFile_EC() {
    try {
      // Derive public key from private key (expected value)
      PrivateKey key = CertUtils.loadPrivateKey(this.PRIV_KEY_VALUE);
      ECPublicKey derivedPublicKey = CertUtils.derivePublicKey((ECPrivateKey) key);

      // Load public key from certificate
      PublicKey certPublicKey = CertUtils.loadPublicKey(PEM_CERTIFICATE);

      assertTrue(certPublicKey instanceof ECPublicKey, "should be instanceof ECPublicKey");

      ECPublicKey ecCertPublicKey = (ECPublicKey) certPublicKey;

      // Compare public key points
      assertEquals(
          ecCertPublicKey.getW(),
          derivedPublicKey.getW(),
          "Public key derived from private key must match certificate - Compare public key points");

      assertEquals(
          ecCertPublicKey.getAlgorithm(),
          derivedPublicKey.getAlgorithm(),
          "Public key derived from private key must match certificate - Compare public key algorithm");
      assertEquals(ecCertPublicKey.getFormat(),
          derivedPublicKey.getFormat(),
          "Public key derived from private key must match certificate - Compare public key format");
      assertEquals(
          ecCertPublicKey.getParams().getGenerator(),
          derivedPublicKey.getParams().getGenerator(),
          "Public key derived from private key must match certificate - Compare public key Generator Point");

      assertEquals(
          BytesUtils.bytesToHex(ecCertPublicKey.getEncoded()),
          BytesUtils.bytesToHex(derivedPublicKey.getEncoded()));
    } catch (Exception e) {
      fail(e.getMessage(), e.getCause());
    }
  };

  @Test
  @DisplayName("should load certificate from String")
  public void shouldReturnPubKeyFromString_EC() {
    try {
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
      X509Certificate cert = CertUtils.loadCertificate(pem);

      assertTrue(cert.getPublicKey() instanceof ECPublicKey, "should be instanceof ECPublicKey");

    } catch (Exception e) {
      fail(e.getMessage(), e.getCause());
    }
  };

  @Test
  @DisplayName("should return algorithm from .pem file")
  public void shouldReturnAlgorithmFromPemFile() {
    try {
      String algorithm = CertUtils.getAlgorithm(PEM_CERTIFICATE);
      assertEquals("SHA256withECDSA", algorithm);
    } catch (Exception e) {
      fail(e.getMessage(), e.getCause());
    }
  }

  @Test
  @DisplayName("should return certificate from .pem file")
  public void shouldReturnCertFromPemFile() {
    try {
      X509Certificate cert = CertUtils.loadCertificate(PEM_CERTIFICATE);
      assertEquals("EC", cert.getPublicKey().getAlgorithm(),
          "should be 'EC' - the name of the algorithm associated with this public key");
      assertEquals("X.509", cert.getPublicKey().getFormat(),
          "should be X.509 - the primary encoding format of the public key");
      assertEquals("X.509", cert.getType(), "should be X.509 - the type of this certificate");
    } catch (Exception e) {
      fail(e.getMessage(), e.getCause());
    }
  }

  @Test
  @DisplayName("should return true if massage has been sign by valid private key")
  public void testEcdsaSignatureVerification() {
    try {

      // Load keys
      PrivateKey privateKey = CertUtils.loadPrivateKey(this.PRIV_KEY_VALUE);
      PublicKey publicKey = CertUtils.loadPublicKey(PEM_CERTIFICATE);
      ECPublicKey derivedPublicKey = CertUtils.derivePublicKey((ECPrivateKey) privateKey);

      // Set message
      byte[] message = BytesUtils.stringToBytes_UTF8("Hello world!");

      // Sign
      byte[] signature = CertUtils.sign(message, privateKey, "SHA256withECDSA");

      assertNotNull(signature, "should not be NULL - signature");
      assertTrue(signature.length > 0, "should not be EMPTY - signature");

      // Verify
      assertTrue(
          CertUtils.verify(message, signature, publicKey, "SHA256withECDSA"),
          "should be true - check signature with public key from cert");
      assertTrue(
          CertUtils.verify(message, signature, derivedPublicKey, "SHA256withECDSA"),
          "should be true - check signature with public key derived from private key");
    } catch (Exception e) {
      fail(e.getMessage(), e.getCause());
    }
  }
}
