package org.commonprovenance.framework.store.common.utils;

import java.util.Objects;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.commonprovenance.framework.store.common.validation.ValidatableDTO;
import org.commonprovenance.framework.store.config.AppConfig;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.ConstraintException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

import io.vavr.Function1;
import io.vavr.control.Either;
import io.vavr.control.Try;

public interface EitherUtils {
  EitherHelper EITHER = EitherHelper.get();

  // Mono implementation
  class EitherHelper {
    private static class Holder {
      static EitherHelper instance = new EitherHelper(false);
    }

    private final boolean verboseMode;

    private EitherHelper(boolean verboseMode) {
      this.verboseMode = verboseMode;
    }

    /**
     * Initializes the singleton with the configured value. Should be called exactly
     * once during
     * application startup from {@link AppConfig}.
     */
    public static void initialize(boolean verboseMode) {
      Holder.instance = new EitherHelper(verboseMode);
    }

    static EitherHelper get() {
      return Holder.instance;
    }

    private <R> String defaultNullMessage(R value) {
      if (!this.verboseMode) {
        return "Input parameter can not be null.";
      }

      return "Input parameter can not be null. Caller="
          + callerLocation()
          + ", runtimeType="
          + ((value == null) ? "unknown" : value.getClass().getName());
    }

    private <R> String defaultMessage(String message) {
      if (!this.verboseMode) {
        return message;
      }

      return message + " Caller=" + callerLocation();
    }

    private String callerLocation() {
      return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
          .walk(frames -> frames
              .dropWhile(frame -> frame.getClassName().equals(EitherHelper.class.getName()))
              .findFirst()
              .map(frame -> frame.getClassName() + "#" + frame.getMethodName() + ":" + frame.getLineNumber())
              .orElse("unknown"));
    }

    public <R extends ValidatableDTO> Either<ApplicationException, R> validateDTO(R value) {
      Vector<String> result = value.validate();
      return result.isEmpty()
          ? Either.right(value)
          : Either.left(new ConstraintException(
              "Validation of class '" + value.getClass().getSimpleName() + "' faild with message: "
                  + result.stream().reduce("", (acc, i) -> acc.isEmpty() ? i : acc + ", " + i)));
    }

    public <R> Either<ApplicationException, R> makeSureNotNull(R value) {
      return this.<R>makeSureNotNullWithMessage(this.defaultNullMessage(value)).apply(value);
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

    // --

    public <I, R> Function<I, Either<ApplicationException, R>> liftEither(Function<I, R> liftFunction) {
      return this.<I, ApplicationException, R>liftEither(
          liftFunction,
          (Throwable throwable) -> new InternalApplicationException(this.defaultMessage(
              throwable.getClass().getSimpleName() + ": " + throwable.getMessage())));
    }

    public <I, R> Function<I, Either<ApplicationException, R>> liftEither(
        Function<I, R> liftFunction,
        String leftMessage) {
      return this.<I, ApplicationException, R>liftEither(
          liftFunction,
          _ -> new InternalApplicationException(leftMessage));
    }

    public <I, R> Function<I, Either<ApplicationException, R>> liftEither(
        Function<I, R> liftFunction,
        ApplicationException leftValue) {
      return this.<I, ApplicationException, R>liftEither(liftFunction, _ -> leftValue);
    }

    public <I, L, R> Function<I, Either<L, R>> liftEither(
        Function<I, R> liftFunction,
        Function<Throwable, L> errorMapper) {
      return Function1.<I, R>liftTry(liftFunction)
          .andThen((Try<R> resOrThrowable) -> resOrThrowable.toEither().mapLeft(errorMapper));
    }

    // --

    public <L, R> Function<R, Either<L, R>> makeSureBefore(
        Boolean onlyIf,
        Function<R, Either<L, R>> mapper) {
      return makeSureBefore(_ -> onlyIf, mapper);
    }

    public <L, R> Function<R, Either<L, R>> makeSureBefore(
        Predicate<R> predicate,
        Function<R, Either<L, R>> mapper) {
      return value -> predicate.test(value) ? mapper.apply(value) : Either.right(value);
    }

    // --

    /**
     * Applicative pattern: Combines two Either values using a combining function.
     * If both are Right, applies the combiner to their values.
     * If any is Left, returns the first Left encountered.
     *
     * Example use case: Combining multiple independent validations
     */
    public <L, A, B, R> Either<L, R> combine(
        Either<L, A> eitherA,
        Either<L, B> eitherB,
        BiFunction<A, B, R> combiner) {
      return eitherA.flatMap(a -> eitherB.map(b -> combiner.apply(a, b)));
    }

    public <L, R1, R2> Either<L, R1> combine(
        Either<L, R1> eitherA,
        Either<L, R2> eitherB,
        BiConsumer<R1, R2> consumer) {
      return eitherA.peek(a -> eitherB.peek(b -> consumer.accept(a, b)));
    }

    /**
     * Applicative pattern: Combines three Either values.
     */
    public <L, A, B, C, R> Either<L, R> combine3(
        Either<L, A> eitherA,
        Either<L, B> eitherB,
        Either<L, C> eitherC,
        Function3<A, B, C, R> combiner) {
      return eitherA.flatMap(a -> eitherB.flatMap(b -> eitherC.map(c -> combiner.apply(a, b, c))));
    }

    @FunctionalInterface
    public interface Function3<A, B, C, R> {
      R apply(A a, B b, C c);
    }

    // TODO: !!test this!!
    public <L, R1, R2, R3> Function<R1, Either<L, Function<R2, R3>>> applicative(
        Function<R1, Function<R2, R3>> combiner) {
      return (R1 value) -> Either.<L, R1>right(value).map(combiner);
    }
  }
}
