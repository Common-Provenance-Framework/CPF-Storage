package org.commonprovenance.framework.store.common.utils;

import java.util.Objects;
import java.util.Vector;
import java.util.function.Function;
import java.util.function.Predicate;

import org.commonprovenance.framework.store.common.validation.ValidatableDTO;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.ConstraintException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

import io.vavr.control.Either;

public interface EitherUtils {
  EitherHelper EITHER = new EitherHelper();

  // Mono implementation
  class EitherHelper {
    public <R extends ValidatableDTO> Either<ApplicationException, R> validateDTO(R value) {
      Vector<String> result = value.validate();
      return result.isEmpty()
          ? Either.right(value)
          : Either.left(new ConstraintException(
              "Validation of class '" + value.getClass().getSimpleName() + "' faild with message: "
                  + result.stream().reduce("", (acc, i) -> acc.isEmpty() ? i : acc + ", " + i)));
    }

    public <R> Either<ApplicationException, R> makeSureNotNull(R value) {
      return this.<R>makeSureNotNullWithMessage("Input parameter can not be null.").apply(value);
    }

    public <R> Function<R, Either<ApplicationException, R>> makeSureNotNullWithMessage(String message) {
      return this.<R>makeSure(Objects::nonNull, message);
    }

    public <R> Function<R, Either<ApplicationException, R>> makeSure(Predicate<R> validator, String message) {
      return this.<R>makeSure(validator, _ -> new InternalApplicationException(message));
    }

    public <R> Function<R, Either<ApplicationException, R>> makeSure(
        Predicate<R> validator,
        Function<R, ApplicationException> applicationExceptionBuilder) {
      return (R value) -> validator.test(value)
          ? Either.right(value)
          : Either.left(applicationExceptionBuilder.apply(value));
    }

  }
}
