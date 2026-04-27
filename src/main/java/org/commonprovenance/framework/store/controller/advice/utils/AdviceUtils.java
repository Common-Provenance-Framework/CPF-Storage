package org.commonprovenance.framework.store.controller.advice.utils;

import java.util.function.BiFunction;

import org.commonprovenance.framework.store.exceptions.ApplicationException;

public final class AdviceUtils {
  public static String buildMessage(Throwable throwable) {
    return AdviceUtils.buildMessage(throwable, "");
  }

  public static String buildMessage(Throwable throwable, String acc) {
    BiFunction<Throwable, String, String> build = (exception, message) -> message
        + "\n- [" + exception.getClass().getSimpleName() + "]: "
        + exception.getMessage()
        + " (" + throwable.getStackTrace()[0].getFileName() + "/" + throwable.getStackTrace()[0].getMethodName() + ":" + throwable.getStackTrace()[0].getLineNumber() + ")";

    if (throwable == null)
      return acc;

    if (throwable instanceof ApplicationException appException)
      buildMessage(appException.getCause(), build.apply(appException, acc));

    return build.apply(throwable, acc);
  }
}
