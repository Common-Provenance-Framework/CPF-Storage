package org.commonprovenance.framework.store.common.utils;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.io.ByteArrayInputStream;
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

import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECPoint;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.control.Either;

public interface CertUtils {
  final static String ALGORITHM = "EC";

  CertFunctionalUtils FUNCTIONAL = new CertFunctionalUtils();
  CertImperativeUtils IMPERATIVE = new CertImperativeUtils();

  class CertFunctionalUtils {
    static {
      Security.addProvider(new BouncyCastleProvider());
    }

    public Either<ApplicationException, PrivateKey> loadPrivateKey(String key) {
      // Java crypto APIs are PKCS#8-centric
      // Convert to if necessary
      // openssl pkcs8 -topk8 -nocrypt -in ./cpf-utils/src/test/resources/cert/org.key
      // -out ./cpf-utils/src/test/resources/cert/org_pkcs8.key
      return Either.<ApplicationException, String>right(key)
          .flatMap(EITHER::makeSureNotNull)
          .flatMap(Base64Utils::decode)
          .flatMap(EITHER.liftEither(der -> new PKCS8EncodedKeySpec(der)))
          .flatMap(EITHER.liftEitherChecked(IMPERATIVE::generatePrivateKey));
    }

    public Either<ApplicationException, ECPublicKey> derivePublicKey(ECPrivateKey privateKey) {
      return Either.<ApplicationException, ECPrivateKey>right(privateKey)
          .flatMap(EITHER.liftEitherChecked(IMPERATIVE::derivePublicKey));
    }

    public Either<ApplicationException, X509Certificate> loadCertificate(String pem) {
      return Either.<ApplicationException, String>right(pem)
          .flatMap(EITHER::makeSureNotNull)
          .flatMap(BytesUtils::stringToBytes_UTF8)
          .flatMap(this::loadCertificate);
    }

    public Either<ApplicationException, X509Certificate> loadCertificate(byte[] bytes) {
      return Either.<ApplicationException, byte[]>right(bytes)
          .flatMap(EITHER.liftEitherChecked(IMPERATIVE::loadCertificate));
    }

    public Either<ApplicationException, PublicKey> loadPublicKey(String pem) {
      return this.loadCertificate(pem)
          .flatMap(EITHER.liftEither(X509Certificate::getPublicKey));
    }

    public Either<ApplicationException, String> getAlgorithm(String pem) {
      return this.loadPublicKey(pem)
          .flatMap(EITHER.liftEither(PublicKey::getAlgorithm))
          .flatMap((String keyAlg) -> switch (keyAlg) {
            case "RSA" -> Either.right("SHA256withRSA");
            case "EC" -> Either.right("SHA256withECDSA");
            default ->
              Either.left(new InternalApplicationException("[IllegalStateException] Unsupported key type: " + keyAlg));
          });
    }

    public Function2<byte[], PrivateKey, Either<ApplicationException, byte[]>> sign(String algorithm) {
      return (data, privateKey) -> EITHER.liftEitherChecked(alg -> IMPERATIVE.sign(data, privateKey, algorithm))
          .apply(algorithm);
    }

    public Function1<byte[], Either<ApplicationException, byte[]>> sign(PrivateKey privateKey, String algorithm) {
      return (data) -> EITHER.liftEitherChecked(alg -> IMPERATIVE.sign(data,
          privateKey, algorithm))
          .apply(algorithm);
    }

    public Either<ApplicationException, String> sign_base64(String data, PrivateKey privateKey, String algorithm) {
      return BytesUtils.stringToBytes_UTF8(data)
          .flatMap(this.sign(privateKey, algorithm))
          .flatMap(Base64Utils::encode);
    }

    public Function3<byte[], byte[], PublicKey, Either<ApplicationException, Boolean>> verify(String algorithm) {
      return (data, sig, publicKey) -> EITHER
          .liftEitherChecked(() -> IMPERATIVE.verify(data, sig, publicKey, algorithm));
    }

    public Either<ApplicationException, Boolean> verify(
        String data,
        String base64Signature,
        X509Certificate certificate,
        String algorithm) {

      Function2<PublicKey, String, io.vavr.CheckedFunction2<byte[], byte[], Boolean>> signatureVerifier = (
          PublicKey pk, String alg) -> (byte[] d, byte[] s) -> IMPERATIVE.verify(d, s, pk, alg);

      return EITHER.combineChecked(
          BytesUtils.stringToBytes_UTF8(data),
          Base64Utils.decode(base64Signature),
          signatureVerifier.apply(certificate.getPublicKey(), algorithm));
    }

  }

  class CertImperativeUtils {

    public PrivateKey loadPrivateKey(String key) {
      return FUNCTIONAL.loadPrivateKey(key)
          .getOrElseThrow(Function1.identity());
    }

    public ECPublicKey derivePublicKey(ECPrivateKey privateKey)
        throws NoSuchAlgorithmException, InvalidKeySpecException {
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
      return (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(pubSpec);
    }

    public PrivateKey generatePrivateKey(PKCS8EncodedKeySpec key)
        throws NoSuchAlgorithmException, InvalidKeySpecException {
      return KeyFactory.getInstance(ALGORITHM).generatePrivate(key);
    }

    public X509Certificate loadCertificate(String pem) throws CertificateException {
      return FUNCTIONAL.loadCertificate(pem)
          .getOrElseThrow(Function1.identity());
    }

    public X509Certificate loadCertificate(byte[] bytes) throws CertificateException {
      ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      return (X509Certificate) cf.generateCertificate(stream);

    }

    public byte[] sign(byte[] data, PrivateKey privateKey, String algorithm)
        throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
      Signature signature = Signature.getInstance(algorithm);

      signature.initSign(privateKey);
      signature.update(data);

      return signature.sign();
    }

    public Boolean verify(byte[] data, byte[] sig, PublicKey publicKey, String algorithm)
        throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
      Signature signature = Signature.getInstance(algorithm);

      signature.initVerify(publicKey);
      signature.update(data);
      return signature.verify(sig);
    }
  }
}
