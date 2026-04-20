package org.commonprovenance.framework.store.common.utils;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.ApplicationException;

import io.vavr.control.Either;

public class Base64Utils {

  public static Either<ApplicationException, String> decodeToString(String base64Data) {
    return Either.<ApplicationException, String>right(base64Data)
        .flatMap(Base64Utils.decodeToString(StandardCharsets.UTF_8));
  }

  public static Function<String, Either<ApplicationException, String>> decodeToString(Charset charset) {
    return (String base64Data) -> Either.<ApplicationException, String>right(base64Data)
        .flatMap(Base64Utils::decode)
        .flatMap(BytesUtils.bytesToString(charset));
  }

  public static Either<ApplicationException, byte[]> decode(String base64Data) {
    return Either.<ApplicationException, String>right(base64Data)
        .flatMap(EITHER::<String>makeSureNotNull)
        .flatMap(EITHER.liftEither(Base64.getDecoder()::decode));
  }

  public static Either<ApplicationException, String> encodeFromString(String stringData) {
    return Either.<ApplicationException, String>right(stringData)
        .flatMap(Base64Utils.encodeFromString(StandardCharsets.UTF_8));
  }

  public static Function<String, Either<ApplicationException, String>> encodeFromString(Charset charset) {
    return (String stringData) -> Either.<ApplicationException, String>right(stringData)
        .flatMap(EITHER::makeSureNotNull)
        .flatMap(BytesUtils.stringToBytes(charset))
        .flatMap(Base64Utils::encode);
  }

  public static Either<ApplicationException, String> encode(byte[] bytesData) {
    return Either.<ApplicationException, byte[]>right(bytesData)
        .flatMap(EITHER::makeSureNotNull)
        .flatMap(EITHER.liftEither(Base64.getEncoder()::encodeToString));
  }

  // Base64Url methods (for JWT and URL-safe encoding)

  public static Either<ApplicationException, String> decodeBase64UrlToString(String base64UrlData) {
    return Either.<ApplicationException, String>right(base64UrlData)
        .flatMap(Base64Utils.decodeBase64UrlToString(StandardCharsets.UTF_8));
  }

  public static Function<String, Either<ApplicationException, String>> decodeBase64UrlToString(Charset charset) {
    return (String base64UrlData) -> Either.<ApplicationException, String>right(base64UrlData)
        .flatMap(Base64Utils::decodeBase64Url)
        .flatMap(BytesUtils.bytesToString(charset));
  }

  public static Either<ApplicationException, byte[]> decodeBase64Url(String base64UrlData) {
    return Either.<ApplicationException, String>right(base64UrlData)
        .flatMap(EITHER::makeSureNotNull)
        .flatMap(EITHER.liftEither(Base64.getUrlDecoder()::decode));
  }

  public static Either<ApplicationException, String> encodeBase64UrlFromString(String stringData) {
    return Either.<ApplicationException, String>right(stringData)
        .flatMap(Base64Utils.encodeBase64UrlFromString(StandardCharsets.UTF_8));
  }

  public static Function<String, Either<ApplicationException, String>> encodeBase64UrlFromString(Charset charset) {
    return (String stringData) -> Either.<ApplicationException, String>right(stringData)
        .flatMap(BytesUtils.stringToBytes(charset))
        .flatMap(Base64Utils::encodeBase64Url);
  }

  public static Either<ApplicationException, String> encodeBase64Url(byte[] bytesData) {
    return Either.<ApplicationException, byte[]>right(bytesData)
        .flatMap(EITHER::makeSureNotNull)
        .flatMap(EITHER.liftEither(Base64.getUrlEncoder().withoutPadding()::encodeToString));
  }
}
