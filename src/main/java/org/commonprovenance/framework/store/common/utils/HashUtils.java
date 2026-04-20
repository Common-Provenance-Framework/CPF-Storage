package org.commonprovenance.framework.store.common.utils;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

import io.vavr.Function2;
import io.vavr.control.Either;

public class HashUtils {
  private static Either<ApplicationException, MessageDigest> getMessageDigestInstance(String algorithm) {
    try {
      return Either.right(MessageDigest.getInstance(algorithm));
    } catch (NoSuchAlgorithmException exception) {
      return Either.left(new InternalApplicationException(
          "No Provider supports a MessageDigestSpi implementation for the specified algorithm '" + algorithm + "'!",
          exception));
    } catch (NullPointerException exception) {
      return Either.left(new InternalApplicationException(
          "Algorithm can not be null!", exception));
    } catch (Throwable throwable) {
      return Either.left(new InternalApplicationException(
          "Can not get MessageDigestSpi instance: " + throwable.getMessage() + "!", throwable));
    }
  }

  public static Either<ApplicationException, String> sha256(String data) {
    Function2<byte[], MessageDigest, Either<ApplicationException, String>> combiner = (bytes, digest) -> Either
        .<ApplicationException, byte[]>right(bytes)
        .flatMap(EITHER.liftEither(digest::digest))
        .flatMap(BytesUtils::bytesToHex);

    return EITHER.combineM(
        Either.<ApplicationException, String>right(data)
            .flatMap(EITHER::makeSureNotNull)
            .flatMap(BytesUtils::stringToBytes_UTF8),
        HashUtils.getMessageDigestInstance("SHA-256"),
        combiner);
  }

}
