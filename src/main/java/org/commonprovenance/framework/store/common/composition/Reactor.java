package org.commonprovenance.framework.store.common.composition;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.commonprovenance.framework.store.config.AppConfig;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.ConflictException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

import io.vavr.Function1;
import io.vavr.Function3;
import io.vavr.control.Either;
import io.vavr.control.Try;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Reactor {
  ReactorComposition MONO = ReactorComposition.get();

  // Mono implementation
  class ReactorComposition {
    private static class Holder {
      static ReactorComposition instance = new ReactorComposition(false);
    }

    private final boolean verboseMode;

    private ReactorComposition(boolean verboseMode) {
      this.verboseMode = verboseMode;
    }

    /**
     * Initializes the singleton with the configured value. Should be called exactly once during application startup from {@link AppConfig}.
     */
    public static void initialize(boolean verboseMode) {
      Holder.instance = new ReactorComposition(verboseMode);
    }

    static ReactorComposition get() {
      return Holder.instance;
    }

    private <T> String defaultNullMessage(T value) {
      if (!this.verboseMode) {
        return "Input parameter can not be null.";
      }

      return "Input parameter can not be null. Caller="
          + callerLocation()
          + ", runtimeType="
          + ((value == null) ? "unknown" : value.getClass().getName());
    }

    private <T> String defaultMessage(String message) {
      if (!this.verboseMode) {
        return message;
      }

      return message + " Caller=" + callerLocation();
    }

    private String callerLocation() {
      return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
          .walk(frames -> frames
              .dropWhile(frame -> frame.getClassName().equals(ReactorComposition.class.getName()))
              .findFirst()
              .map(frame -> frame.getClassName() + "#" + frame.getMethodName() + ":" + frame.getLineNumber())
              .orElse("unknown"));
    }

    public <T> Mono<T> makeSureNotNull(T value) {
      return this.<T> makeSureNotNullWithMessage(this.defaultNullMessage(value)).apply(value);
    }

    public <T> Function<T, Mono<T>> makeSureNotNull(ApplicationException exception) {
      return this.<T> makeSure(Objects::nonNull, _ -> exception);
    }

    public <T> Function<T, Mono<T>> makeSureNotNullWithMessage(String message) {
      return this.<T> makeSure(Objects::nonNull, message);
    }

    public <T> Function<T, Mono<T>> makeSure(Predicate<T> validator, String message) {
      return this.<T> makeSure(validator, _ -> new InternalApplicationException(message));
    }

    public <T> Function<T, Mono<T>> makeSure(
        Predicate<T> validator,
        Function<T, ApplicationException> applicationExceptionBuilder) {
      return (T value) -> validator.test(value)
          ? Mono.just(value)
          : Mono.error(applicationExceptionBuilder.apply(value));
    }

    public <T, E extends ApplicationException> Function<T, Mono<T>> makeSure(
        Predicate<T> validator,
        Function<String, E> factory,
        Function<T, String> messageBuileder) {
      return (T value) -> validator.test(value)
          ? Mono.just(value)
          : Mono.error(factory.compose(messageBuileder).apply(value));
    }

    public <T> Function<T, Mono<T>> makeSureAsync(
        Function<T, Mono<Boolean>> asyncValidator,
        String message) {
      return makeSureAsync(asyncValidator, _ -> new ConflictException(message));
    }

    public <T> Function<T, Mono<T>> makeSureAsync(
        Function<T, Mono<Boolean>> asyncValidator,
        Function<T, ApplicationException> appExceptionBuilderFunction) {
      return makeSureAsync(asyncValidator, appExceptionBuilderFunction, true);
    }

    public <T> Function<T, Mono<T>> makeSureNotAsync(
        Function<T, Mono<Boolean>> asyncValidator,
        Function<T, ApplicationException> appExceptionBuilderFunction) {
      return makeSureAsync(asyncValidator, appExceptionBuilderFunction, false);
    }

    public <T, E extends ApplicationException> Function<T, Mono<T>> makeSureNotAsync(
        Function<T, Mono<Boolean>> asyncValidator,
        Function<String, E> factory,
        Function<T, String> messageBuileder) {
      return makeSureAsync(asyncValidator, factory, messageBuileder, false);
    }

    public <T> Function<T, Mono<T>> makeSureAsync(
        Function<T, Mono<Boolean>> asyncValidator,
        Function<T, ApplicationException> appExceptionBuilderFunction,
        Boolean positive) {
      return (T value) -> Mono.justOrEmpty(value)
          .flatMap(v -> asyncValidator.apply(v)
              .defaultIfEmpty(false)
              .flatMap(isValid -> isValid == positive
                  ? Mono.just(v)
                  : Mono.error(appExceptionBuilderFunction.apply(v))));
    }

    public <T, E extends ApplicationException> Function<T, Mono<T>> makeSureAsync(
        Function<T, Mono<Boolean>> asyncValidator,
        Function<String, E> factory,
        Function<T, String> messageBuileder) {
      return makeSureAsync(asyncValidator, factory, messageBuileder, true);
    }

    public <T, E extends ApplicationException> Function<T, Mono<T>> makeSureAsync(
        Function<T, Mono<Boolean>> asyncValidator,
        Function<String, E> factory,
        Function<T, String> messageBuileder,
        Boolean positive) {
      return (T value) -> Mono.justOrEmpty(value)
          .flatMap(v -> asyncValidator.apply(v)
              .defaultIfEmpty(false)
              .flatMap(isValid -> isValid == positive
                  ? Mono.just(v)
                  : Mono.error(factory.compose(messageBuileder).apply(v))));
    }

    public <I, O> Function<I, Mono<O>> liftEffectToMono(Function<I, Either<ApplicationException, O>> kleisliArrow) {
      return (I value) -> kleisliArrow
          .apply(value)
          .fold(Mono::error, Mono::justOrEmpty);
    }

    public <I, O> Function<I, Mono<O>> liftOptionalToMono(Function<I, Optional<O>> maybe, String message) {
      return (I value) -> maybe
          .apply(value)
          .map(Mono::justOrEmpty)
          .orElse(Mono.error(new InternalApplicationException(message)));
    }

    public <I, O> Function<I, Mono<O>> liftOptionalToMono(Function<I, Optional<O>> maybe) {
      return (I value) -> maybe
          .apply(value)
          .map(Mono::justOrEmpty)
          .orElse(Mono.empty());
    }

    public <I, O> Function<I, Mono<O>> liftOptionalToMono(Function<I, Optional<O>> maybe, Function<I, ApplicationException> exceptionBuilder) {
      return (I value) -> maybe
          .apply(value)
          .map(Mono::justOrEmpty)
          .orElse(Mono.error(exceptionBuilder.apply(value)));
    }

    public <I, O> Function<I, Flux<O>> liftEffectToFlux(Function<I, Either<ApplicationException, List<O>>> kleisliArrow) {
      return (I value) -> kleisliArrow
          .apply(value)
          .map(List::stream)
          .fold(Flux::error, Flux::fromStream);
    }

    public <I, O> Function<I, Mono<O>> liftPureToMono(Function<I, O> liftFunction) {
      return Function1.<I, O> liftTry(liftFunction)
          .andThen((Try<O> resOrThrowable) -> resOrThrowable.fold(Mono::error, Mono::justOrEmpty));
    }

    public <T> Mono<T> fromEither(Either<ApplicationException, T> valueOrException) {
      return valueOrException
          .fold(Mono::error, Mono::justOrEmpty);
    }

    public <T> Mono<Optional<T>> fromEitherOptional(Either<ApplicationException, Optional<T>> valueOrException) {
      return valueOrException
          .fold(Mono::error, Mono::just);
    }

    public <T> Mono<T> fromOptional(Optional<T> maybe) {
      return maybe.isPresent()
          ? Mono.<T> just(maybe.get())
          : Mono.<T> error(new InternalApplicationException("Optional value is not present!"));
    }

    public <E extends Throwable, T> Function<E, Mono<T>> exceptionWrapper(Function<E, String> messageBuilder) {
      return (E exception) -> (exception instanceof ApplicationException)
          ? Mono.<T> error(exception) // Propagate existing ApplicationException as is
          : Mono.<T> error(new InternalApplicationException(messageBuilder.apply(exception), exception));
    }

    public <E extends Throwable, T> Function<E, Mono<T>> exceptionWrapper(String message) {
      return exceptionWrapper(_ -> message);
    }

    public <E extends Throwable, T> Function<E, Mono<T>> exceptionWrapper() {
      return exceptionWrapper("Unexpected exception!");
    }

    public <I1, I2, O> Function<I2, Function<I1, Mono<O>>> flipped(Function<I1, Function<I2, Mono<O>>> function) {
      return (I2 v2) -> (I1 v1) -> function.apply(v1).apply(v2);
    }

    public <A, B, R> Mono<R> combine(
        Mono<A> monoA,
        Mono<B> monoB,
        BiFunction<A, B, R> combiner) {
      return monoA.flatMap(a -> monoB.map(b -> combiner.apply(a, b)));
    }

    public <A, B, R> Mono<R> combineM(
        Mono<A> monoA,
        Mono<B> monoB,
        BiFunction<A, B, Mono<R>> combinerM) {
      return monoA.flatMap(a -> monoB.flatMap(b -> combinerM.apply(a, b)));
    }

    public <A, B, C, R> Function1<C, Mono<R>> combineM(
        Mono<A> monoA,
        Mono<B> monoB,
        Function3<A, B, C, Mono<R>> combinerM) {
      return (C c) -> monoA.flatMap(a -> monoB.flatMap(b -> combinerM.apply(a, b, c)));
    }

    public <A, B, C, R> Mono<R> combineM(
        Mono<A> monoA,
        Mono<B> monoB,
        Mono<C> monoC,
        Function3<A, B, C, Mono<R>> combinerM) {
      return monoA.flatMap(a -> monoB.flatMap(b -> monoC.flatMap(c -> combinerM.apply(a, b, c))));
    }

    public <T> Function1<T, Mono<Void>> makeSureBefore(
        Predicate<T> predicate,
        Function1<T, Mono<Void>> mapper) {
      return value -> predicate.test(value) ? mapper.apply(value) : Mono.empty();
    }

    public <T> Function1<T, Mono<Void>> makeSureBefore(
        Function1<T, Mono<Boolean>> asyncPredicate,
        Function1<T, Mono<Void>> mapper) {
      return value -> asyncPredicate.apply(value)
          .flatMap(trueOrFalse -> trueOrFalse
              ? mapper.apply(value)
              : Mono.empty());
    }

    public <T> Function1<T, Mono<Void>> makeSureNotBefore(
        Function1<T, Mono<Boolean>> asyncPredicate,
        Function1<T, Mono<Void>> mapper) {
      return value -> asyncPredicate.apply(value)
          .flatMap(trueOrFalse -> trueOrFalse
              ? Mono.empty()
              : mapper.apply(value));
    }
  }

}
