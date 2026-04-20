package org.commonprovenance.framework.store.common.utils;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

import io.vavr.control.Either;

public class BytesUtils {
  public static Either<ApplicationException, String> bytesToHex(byte[] bytes) {
    try {
      StringBuilder hex = new StringBuilder(bytes.length * 2);
      for (byte b : bytes) {
        hex.append(String.format("%02x", b));
      }
      return Either.right(hex.toString());
    } catch (Throwable throwable) {
      return Either.left(new InternalApplicationException(
          "Can not convert bytes to hex: " + throwable.getMessage(),
          throwable));
    }
  }

  public static Either<ApplicationException, String> bytesToString_UTF8(byte[] bytes) {
    return BytesUtils.bytesToString(StandardCharsets.UTF_8)
        .apply(bytes);
  }

  public static Function<byte[], Either<ApplicationException, String>> bytesToString(Charset charset) {
    return (byte[] bytes) -> EITHER.<byte[]>makeSureNotNull(bytes)
        .flatMap(EITHER.liftEither(bs -> new String(bytes, charset)));
  }

  public static Either<ApplicationException, byte[]> stringToBytes_UTF8(String stringData) {
    return BytesUtils.stringToBytes(StandardCharsets.UTF_8)
        .apply(stringData);
  }

  public static Function<String, Either<ApplicationException, byte[]>> stringToBytes(Charset charset) {
    return (String stringData) -> EITHER.makeSureNotNull(stringData)
        .flatMap(EITHER.liftEither(data -> data.getBytes(charset)));
  }
}
