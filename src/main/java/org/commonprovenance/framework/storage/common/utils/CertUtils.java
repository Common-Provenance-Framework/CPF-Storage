package org.commonprovenance.framework.storage.common.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.math.ec.ECPoint;

public class CertUtils {
  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  public static String preprocessFileContent(String fileContent) {
    return fileContent
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replaceAll("\\s", "");
  }

  public static PrivateKey loadPrivateKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
    final String ALGORITHM = "EC";

    // Java crypto APIs are PKCS#8-centric
    // Convert to if necessary
    // openssl pkcs8 -topk8 -nocrypt -in ./cpf-utils/src/test/resources/cert/org.key
    // -out ./cpf-utils/src/test/resources/cert/org_pkcs8.key

    byte[] der = Base64Utils.decode(key);
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);

    return KeyFactory.getInstance(ALGORITHM).generatePrivate(spec);
  }

  public static ECPublicKey derivePublicKey(ECPrivateKey privateKey)
      throws InvalidKeySpecException, NoSuchAlgorithmException {

    ECParameterSpec params = privateKey.getParams();
    BigInteger d = privateKey.getS();

    // Convert JDK generator to BC point
    org.bouncycastle.math.ec.ECCurve bcCurve = EC5Util.convertCurve(params.getCurve());

    ECPoint bcGenerator = EC5Util.convertPoint(bcCurve, params.getGenerator());

    // Q = d * G
    ECPoint bcQ = bcGenerator.multiply(d).normalize();

    // Convert BC point back to JDK ECPoint
    java.security.spec.ECPoint w = new java.security.spec.ECPoint(
        bcQ.getAffineXCoord().toBigInteger(),
        bcQ.getAffineYCoord().toBigInteger());

    ECPublicKeySpec pubSpec = new ECPublicKeySpec(w, params);
    return (ECPublicKey) KeyFactory.getInstance("EC")
        .generatePublic(pubSpec);
  }

  public static X509Certificate loadCertificate(String pem) throws IOException, CertificateException {
    try (InputStream stream = new ByteArrayInputStream(BytesUtils.stringToBytes_UTF8(pem))) {
      return CertUtils.loadCertificate(stream);
    }
  }

  public static X509Certificate loadCertificate(InputStream stream) throws CertificateException {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    return (X509Certificate) cf.generateCertificate(stream);
  }

  public static PublicKey loadPublicKey(String pem) throws IOException, CertificateException {
    return CertUtils.loadCertificate(pem).getPublicKey();
  }

  public static String getAlgorithm(String pem) throws IOException, CertificateException {
    String keyAlg = loadCertificate(pem).getPublicKey().getAlgorithm();
    return switch (keyAlg) {
      case "RSA" -> "SHA256withRSA";
      case "EC" -> "SHA256withECDSA";
      default -> throw new IllegalStateException("Unsupported key type: " + keyAlg);
    };
  }

  public static byte[] sign(byte[] data, PrivateKey privateKey, String algorithm)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

    Signature signature = Signature.getInstance(algorithm);
    signature.initSign(privateKey);
    signature.update(data);

    return signature.sign();
  }

  public static String sign_base64(String data, PrivateKey privateKey, String algorithm)
      throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    return Base64Utils.encode(CertUtils.sign(
        BytesUtils.stringToBytes_UTF8(data),
        privateKey,
        algorithm));
  }

  public static boolean verify(
      byte[] data,
      byte[] sig,
      PublicKey pubKey,
      String algorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

    Signature signature = Signature.getInstance(algorithm);
    signature.initVerify(pubKey);
    signature.update(data);

    return signature.verify(sig);
  }

  public static boolean verify(
      String data,
      String base64Signature,
      X509Certificate certificate,
      String algorithm) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

    return CertUtils.verify(
        BytesUtils.stringToBytes_UTF8(data),
        Base64Utils.decode(base64Signature),
        certificate.getPublicKey(),
        algorithm);
  }
}
