package org.commonprovenance.framework.store.exceptions.factory;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

public final class ApplicationExceptionFactory {
  private ApplicationExceptionFactory() {
  }

  public static <E extends ApplicationException> Function<Throwable, E> build(
      String message,
      Function<String, E> factory) {
    return _ -> factory.apply(message);
  }

  public static <E extends ApplicationException, T> Function<Throwable, E> build(
      Function<T, String> messageBuilder,
      T value,
      Function<String, E> factory) {
    return _ -> factory.compose(messageBuilder).apply(value);
  }

  public static <E extends ApplicationException> Function<Throwable, E> build(
      BiFunction<String, Throwable, E> factory,
      String message) {
    return (Throwable cause) -> factory.apply(message, cause);
  }

  public static <E extends ApplicationException> Function<Throwable, E> to(
      BiFunction<String, Throwable, E> factory) {
    return (Throwable cause) -> {
      if (cause == null) {
        return factory.apply("Unknown error", null);
      }

      String message = cause.getLocalizedMessage() != null
          ? cause.getLocalizedMessage()
          : cause.getClass().getSimpleName();

      return factory.apply(message, cause.getCause());
    };
  }

  public static Function<Throwable, ApplicationException> handleThrowable(ApplicationException exception) {
    return throwable -> (throwable instanceof ApplicationException applicationException)
        ? applicationException
        : (ApplicationException) exception.initCause(throwable);
  }

  public static Function<Throwable, ApplicationException> handleThrowable(String className, String methodName) {
    return throwable -> (throwable instanceof ApplicationException applicationException)
        ? ApplicationExceptionFactory.setHeader(className, methodName)
            .apply(applicationException)
        : ApplicationExceptionFactory.setHeader(className, methodName)
            .apply(new InternalApplicationException("Internal Application exception!", throwable));
  }

  public static Function<Throwable, ApplicationException> handleThrowable(String className, String methodName, String detail) {
    return throwable -> (throwable instanceof ApplicationException applicationException)
        ? ApplicationExceptionFactory.setHeader(className, methodName, detail)
            .apply(applicationException)
        : ApplicationExceptionFactory.setHeader(className, methodName, detail)
            .apply(new InternalApplicationException("Internal Application exception!", throwable));
  }

  public static <E extends ApplicationException, T> Function<Throwable, E> build(
      BiFunction<String, Throwable, E> factory,
      T value,
      Function<T, String> messageBuilder) {
    return (Throwable cause) -> factory.apply(messageBuilder.apply(value), cause);
  }

  private static Function<ApplicationException, ApplicationException> setHeader(String className, String methodName) {
    return ApplicationExceptionFactory.setHeader(className, methodName, null);
  }

  private static Function<ApplicationException, ApplicationException> setHeader(String className, String methodName, String detail) {
    return (ApplicationException exception) -> {
      exception.setHeader(className, methodName, detail);
      return exception;
    };
  }

  public static Function<Throwable, ApplicationException> setHeader(String header) {
    return (Throwable t) -> {
      if (!(t instanceof ApplicationException cause)) {
        // ApplicationException exception =
        return new InternalApplicationException(header + ": *** Unhandled application exception *** " + t.getMessage(), t);
      }

      String newMessage = header + ": " + cause.getMessage();
      Class<? extends ApplicationException> clazz = cause.getClass();

      try {
        return clazz.getConstructor(String.class, Throwable.class)
            .newInstance(newMessage, cause.getCause());
      } catch (ReflectiveOperationException e) {
        try {
          return clazz.getConstructor(String.class).newInstance(newMessage);
        } catch (ReflectiveOperationException e2) {
          return cause;
        }
      } catch (Throwable throwable) {
        return cause;
      }
    };
  }
}
