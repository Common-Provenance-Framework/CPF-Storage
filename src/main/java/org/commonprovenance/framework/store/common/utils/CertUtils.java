package org.commonprovenance.framework.store.common.utils;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.io.ByteArrayInputStream;
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
import java.util.function.Function;

import org.bouncycastle.jcajce.provider.asymmetric.util.EC5Util;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECPoint;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;

import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.control.Either;

public class CertUtils {
  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  private static Either<ApplicationException, PrivateKey> generatePrivateKey(PKCS8EncodedKeySpec key) {
    final String ALGORITHM = "EC";

    try {
      return Either.right(KeyFactory.getInstance(ALGORITHM).generatePrivate(key));
    } catch (NoSuchAlgorithmException exception) {
      return Either
          .left(new InternalApplicationException(
              "[NoSuchAlgorithmException] Can not generate private key: " + exception.getMessage(), exception));
    } catch (InvalidKeySpecException exception) {
      return Either
          .left(new InvalidValueException(
              "[InvalidKeySpecException] Can not generate private key: " + exception.getMessage(), exception));
    } catch (Throwable throwable) {
      return Either
          .left(new InternalApplicationException("Can not generate private key: " + throwable.getMessage(), throwable));
    }
  }

  public static String preprocessFileContent(String fileContent) {
    return fileContent
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replaceAll("\\s", "");
  }

  public static Either<ApplicationException, PrivateKey> loadPrivateKey(String key) {
    // Java crypto APIs are PKCS#8-centric
    // Convert to if necessary
    // openssl pkcs8 -topk8 -nocrypt -in ./cpf-utils/src/test/resources/cert/org.key
    // -out ./cpf-utils/src/test/resources/cert/org_pkcs8.key
    return Either.<ApplicationException, String>right(key)
        .flatMap(EITHER::makeSureNotNull)
        .flatMap(Base64Utils::decode)
        .flatMap(EITHER.liftEither(der -> new PKCS8EncodedKeySpec(der)))
        .flatMap(CertUtils::generatePrivateKey);
  }

  public static Either<ApplicationException, ECPublicKey> derivePublicKey(ECPrivateKey privateKey) {
    try {
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
      return Either.right((ECPublicKey) KeyFactory.getInstance("EC").generatePublic(pubSpec));
    } catch (NoSuchAlgorithmException exception) {
      return Either
          .left(new InternalApplicationException(
              "[NoSuchAlgorithmException] Can not derive public key: " + exception.getMessage(), exception));
    } catch (InvalidKeySpecException exception) {
      return Either
          .left(new InvalidValueException(
              "[InvalidKeySpecException] Can not derive public key: " + exception.getMessage(), exception));
    } catch (Throwable throwable) {
      return Either
          .left(new InternalApplicationException("Can not derive public key: " + throwable.getMessage(), throwable));
    }

  }

  public static Either<ApplicationException, X509Certificate> loadCertificate(String pem) {
    return Either.<ApplicationException, String>right(pem)
        .flatMap(EITHER::makeSureNotNull)
        .flatMap(BytesUtils::stringToBytes_UTF8)
        .flatMap(EITHER.liftEither(bs -> new ByteArrayInputStream(bs)))
        .flatMap(CertUtils::loadCertificate);
  }

  public static Either<ApplicationException, X509Certificate> loadCertificate(InputStream stream) {
    try {
      CertificateFactory cf = CertificateFactory.getInstance("X.509");
      return Either.right((X509Certificate) cf.generateCertificate(stream));
    } catch (CertificateException exception) {
      return Either.left(new InternalApplicationException(
          "[CertificateException] Can not load certificate: " + exception.getMessage(),
          exception));
    } catch (Throwable throwable) {
      return Either.left(new InternalApplicationException(
          "Can not load certificate: " + throwable.getMessage(),
          throwable));
    }
  }

  public static Either<ApplicationException, PublicKey> loadPublicKey(String pem) {
    return CertUtils.loadCertificate(pem)
        .flatMap(EITHER.liftEither(X509Certificate::getPublicKey));
  }

  public static Either<ApplicationException, String> getAlgorithm(String pem) {
    return CertUtils.loadPublicKey(pem)
        .flatMap(EITHER.liftEither(PublicKey::getAlgorithm))
        .flatMap((String keyAlg) -> switch (keyAlg) {
          case "RSA" -> Either.right("SHA256withRSA");
          case "EC" -> Either.right("SHA256withECDSA");
          default ->
            Either.left(new InternalApplicationException("[IllegalStateException] Unsupported key type: " + keyAlg));
        });
  }

  private static Either<ApplicationException, Signature> getSignatureInstance(String algorithm) {
    try {
      return Either.right(Signature.getInstance(algorithm));
    } catch (NoSuchAlgorithmException exception) {
      return Either.left(new InternalApplicationException(
          "No Provider supports a Signature implementation for the specified algorithm '" + algorithm + "'!",
          exception));
    } catch (NullPointerException exception) {
      return Either.left(new InternalApplicationException(
          "algorithm is null!",
          exception));
    } catch (Throwable throwable) {
      return Either.left(new InternalApplicationException(
          "Can not get Signatuer instance object: " + throwable.getMessage(),
          throwable));
    }
  }

  public static Function2<byte[], PrivateKey, Either<ApplicationException, byte[]>> sign(String algorithm) {
    Function2<byte[], PrivateKey, Function<Signature, Either<ApplicationException, byte[]>>> dataSigner = (
        d, pk) -> signature -> {
          try {
            signature.initSign(pk);
            signature.update(d);

            return Either.right(signature.sign());
          } catch (InvalidKeyException exception) {
            return Either.left(new InternalApplicationException(
                "The key is invalid!",
                exception));
          } catch (SignatureException exception) {
            return Either.left(new InternalApplicationException(
                "This Signature object is not initialized properly!",
                exception));
          } catch (Throwable throwable) {
            return Either.left(new InternalApplicationException(
                "Can not sign data: " + throwable.getMessage(),
                throwable));
          }
        };

    return (byte[] data, PrivateKey privateKey) -> CertUtils.getSignatureInstance(algorithm)
        .flatMap(dataSigner.apply(data, privateKey));
  }

  public static Either<ApplicationException, String> sign_base64(String data, PrivateKey privateKey, String algorithm) {
    return BytesUtils.stringToBytes_UTF8(data)
        .flatMap((byte[] bs) -> CertUtils.sign(algorithm).apply(bs, privateKey))
        .flatMap(Base64Utils::encode);
  }

  public static Function3<byte[], byte[], PublicKey, Either<ApplicationException, Boolean>> verify(String algorithm) {
    Function3<byte[], byte[], PublicKey, Function<Signature, Either<ApplicationException, Boolean>>> signatureVerifier = (
        d, s, pk) -> signature -> {
          try {
            signature.initVerify(pk);
            signature.update(d);

            return Either.right(signature.verify(s));
          } catch (InvalidKeyException exception) {
            return Either.left(new InternalApplicationException(
                "The key is invalid!",
                exception));
          } catch (SignatureException exception) {
            return Either.left(new InternalApplicationException(
                "This Signature object is not initialized properly!",
                exception));
          } catch (Throwable throwable) {
            return Either.left(new InternalApplicationException(
                "Can not sign data: " + throwable.getMessage(),
                throwable));
          }
        };

    return (byte[] data, byte[] sig, PublicKey pubKey) -> CertUtils.getSignatureInstance(algorithm)
        .flatMap(signatureVerifier.apply(data, sig, pubKey));
  }

  public static Either<ApplicationException, Boolean> verify(
      String data,
      String base64Signature,
      X509Certificate certificate,
      String algorithm) {

    Function2<PublicKey, String, Function2<byte[], byte[], Either<ApplicationException, Boolean>>> signatureVerifier = (
        PublicKey pk, String alg) -> (byte[] d, byte[] s) -> CertUtils.verify(alg).apply(d, s, pk);

    return EITHER.combineM(
        BytesUtils.stringToBytes_UTF8(data),
        Base64Utils.decode(base64Signature),
        signatureVerifier.apply(certificate.getPublicKey(), algorithm));
  }
}
