package org.commonprovenance.framework.store.common.publisher;

import java.util.Objects;
import java.util.Vector;
import java.util.function.Function;
import java.util.function.Predicate;

import org.commonprovenance.framework.store.common.validation.ValidatableDTO;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.ConstraintException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

import io.vavr.control.Either;
import reactor.core.publisher.Mono;

public interface PublisherHelper {
  MonoHelper MONO = new MonoHelper();

  // Mono implementation
  class MonoHelper {
    public <T extends ValidatableDTO> Mono<T> validateDTO(T value) {
      Vector<String> result = value.validate();
      return result.isEmpty()
          ? Mono.just(value)
          : Mono.error(new ConstraintException(
              "Validation of class '" + value.getClass().getSimpleName() + "' faild with message: "
                  + result.stream().reduce("", (acc, i) -> acc.isEmpty() ? i : acc + ", " + i)));
    }

    public <T> Mono<T> makeSureNotNull(T value) {
      return this.<T>makeSureNotNullWithMessage("Input parameter can not be null.").apply(value);
    }

    public <T> Function<T, Mono<T>> makeSureNotNullWithMessage(String message) {
      return this.<T>makeSure(Objects::nonNull, message);
    }

    public <T> Function<T, Mono<T>> makeSure(Predicate<T> validator, String message) {
      return this.<T>makeSure(validator, _ -> new InternalApplicationException(message));
    }

    public <T> Function<T, Mono<T>> makeSure(
        Predicate<T> validator,
        Function<T, ApplicationException> applicationExceptionBuilder) {
      return (T value) -> validator.test(value)
          ? Mono.just(value)
          : Mono.error(applicationExceptionBuilder.apply(value));
    }

    public <T> Function<T, Mono<T>> makeSureAsync(
        Function<T, Mono<Boolean>> asyncValidator,
        String message) {
      return makeSureAsync(asyncValidator, _ -> new ConflictException(message));
    }

    public <T> Function<T, Mono<T>> makeSureAsync(
        Function<T, Mono<Boolean>> asyncValidator,
        Function<T, ApplicationException> appExceptionBuilderFunction) {
      return (T value) -> Mono.justOrEmpty(value)
          .filterWhen(asyncValidator)
          .switchIfEmpty(Mono.error(appExceptionBuilderFunction.apply(value)));
    }

    public <I, O> Function<I, Mono<O>> liftEither(Function<I, Either<ApplicationException, O>> kleisliArrow) {
      return (I value) -> kleisliArrow
          .apply(value)
          .fold(Mono::error, Mono::justOrEmpty);
    }

    public <T> Mono<T> fromEither(Either<ApplicationException, T> valueOrException) {
      return valueOrException
          .fold(Mono::error, Mono::justOrEmpty);
    }

    public <E extends Throwable, T> Function<E, Mono<T>> exceptionWrapper(Function<E, String> messageBuilder) {
      return (E exception) -> (exception instanceof ApplicationException)
          ? Mono.<T>error(exception) // Propagate existing ApplicationException as is
          : Mono.<T>error(new InternalApplicationException(messageBuilder.apply(exception), exception));
    }

    public <E extends Throwable, T> Function<E, Mono<T>> exceptionWrapper(String message) {
      return exceptionWrapper(_ -> message);
    }

    public <E extends Throwable, T> Function<E, Mono<T>> exceptionWrapper() {
      return exceptionWrapper("Unexpected exception!");
    }
  }
}
