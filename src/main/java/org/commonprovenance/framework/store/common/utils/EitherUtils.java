package org.commonprovenance.framework.store.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.commonprovenance.framework.store.common.validation.ValidatableDTO;
import org.commonprovenance.framework.store.config.AppConfig;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.ConstraintException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

import io.vavr.CheckedFunction0;
import io.vavr.CheckedFunction1;
import io.vavr.CheckedFunction2;
import io.vavr.Function0;
import io.vavr.Function1;
import io.vavr.Function2;
import io.vavr.Function3;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
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
     * Initializes the singleton with the configured value. Should be called exactly once during application startup from {@link AppConfig}.
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
      return this.<R> makeSureNotNullWithMessage(this.defaultNullMessage(value)).apply(value);
    }

    public <R> Function1<R, Either<ApplicationException, R>> makeSureNotNullWithMessage(String message) {
      return this.<R> makeSure(Objects::nonNull, message);
    }

    public <R> Function1<R, Either<ApplicationException, R>> makeSureNotNull(Function1<R, ApplicationException> exceptionBuilder) {
      return this.<R> makeSure(Objects::nonNull, exceptionBuilder);
    }

    public <R> Function1<R, Either<ApplicationException, R>> makeSure(Predicate<R> validator, String message) {
      return this.<R> makeSure(validator, _ -> new InternalApplicationException(message));
    }

    public <R> Function1<R, Either<ApplicationException, R>> makeSure(
        Predicate<R> validator,
        Function1<R, ApplicationException> applicationExceptionBuilder) {
      return (R value) -> validator.test(value)
          ? Either.right(value)
          : Either.left(applicationExceptionBuilder.apply(value));
    }

    public <R> Function1<R, Either<ApplicationException, R>> makeSure(
        Predicate<R> validator,
        Function1<String, ApplicationException> factory,
        Function1<R, String> messageBuileder) {
      return (R value) -> validator.test(value)
          ? Either.right(value)
          : Either.left(factory.compose(messageBuileder).apply(value));
    }

    // --
    public <R> Either<ApplicationException, R> liftEither(Optional<R> maybe) {
      return maybe.isPresent()
          ? Either.<ApplicationException, R> right(maybe.get())
          : Either.<ApplicationException, R> left(new InternalApplicationException("Optional value is not present!"));
    }

    public <I, R> Function1<I, Either<ApplicationException, R>> liftEitherOptional(Function1<I, Optional<R>> maybe) {
      return (I input) -> maybe.apply(input)
          .map(Either::<ApplicationException, R> right)
          .orElse(Either.<ApplicationException, R> left(new InternalApplicationException("Optional value is not present!")));
    }

    public <R> Either<ApplicationException, R> liftEither(Function0<R> liftSupplier) {
      return Function0.<R> liftTry(liftSupplier)
          .andThen((Try<R> resOrThrowable) -> resOrThrowable.toEither()
              .mapLeft(throwable -> (ApplicationException) new InternalApplicationException(
                  this.defaultMessage(throwable.getClass().getSimpleName() + ": " + throwable.getMessage()))))
          .apply();
    }

    public <R> Either<ApplicationException, R> liftEitherChecked(CheckedFunction0<R> liftChecked) {
      return CheckedFunction0.<R> liftTry(liftChecked)
          .andThen((Try<R> resOrThrowable) -> resOrThrowable.toEither()
              .mapLeft(throwable -> (ApplicationException) new InternalApplicationException(
                  this.defaultMessage(throwable.getClass().getSimpleName() + ": " + throwable.getMessage()))))
          .apply();
    }

    public <I, R> Function1<I, Either<ApplicationException, R>> liftEither(Function1<I, R> liftFunction) {
      return this.<I, R> liftEither(
          liftFunction,
          (Throwable throwable) -> new InternalApplicationException(this.defaultMessage(
              throwable.getClass().getSimpleName() + ": " + throwable.getMessage())));
    }

    public <I, R> Function1<I, Either<ApplicationException, R>> liftEitherChecked(CheckedFunction1<I, R> liftChecked) {
      return CheckedFunction1.<I, R> liftTry(liftChecked)
          .andThen((Try<R> resOrThrowable) -> resOrThrowable.toEither()
              .mapLeft(throwable -> new InternalApplicationException(
                  this.defaultMessage(throwable.getClass().getSimpleName() + ": " + throwable.getMessage()))));
    }

    public <I1, I2, R> Function2<I1, I2, Either<ApplicationException, R>> liftEither(
        CheckedFunction2<I1, I2, R> liftChecked) {
      return CheckedFunction2.<I1, I2, R> liftTry(liftChecked)
          .andThen((Try<R> resOrThrowable) -> resOrThrowable.toEither()
              .mapLeft(throwable -> new InternalApplicationException(
                  this.defaultMessage(throwable.getClass().getSimpleName() + ": " + throwable.getMessage()))));
    }

    public <I, R> Function1<I, Either<ApplicationException, R>> liftEither(
        Function1<I, R> liftFunction,
        String leftMessage) {
      return this.<I, R> liftEither(
          liftFunction,
          _ -> (ApplicationException) new InternalApplicationException(leftMessage));
    }

    public <I, R> Function1<I, Either<ApplicationException, R>> liftEither(
        Function1<I, R> liftFunction,
        ApplicationException leftValue) {
      return this.<I, R> liftEither(liftFunction, _ -> leftValue);
    }

    public <I, R> Function1<I, Either<ApplicationException, R>> liftEither(
        Function1<I, R> liftFunction,
        Function1<Throwable, ApplicationException> errorMapper) {
      return Function1.<I, R> liftTry(liftFunction)
          .andThen((Try<R> resOrThrowable) -> resOrThrowable.toEither().mapLeft(errorMapper));
    }

    // --

    public <R> Function1<R, Either<ApplicationException, R>> makeSureBefore(
        boolean onlyIf,
        Function1<R, Either<ApplicationException, R>> mapper) {
      return makeSureBefore(_ -> onlyIf, mapper);
    }

    public <R> Function1<R, Either<ApplicationException, R>> makeSureBefore(
        Predicate<R> predicate,
        Function1<R, Either<ApplicationException, R>> mapper) {
      return value -> predicate.test(value) ? mapper.apply(value) : Either.right(value);
    }

    // --

    /**
     * Applicative pattern: Combines two Either values using a combining function. If both are Right, applies the combiner to their values. If any is Left, returns the first Left
     * encountered.
     *
     * Example use case: Combining multiple independent validations
     */
    public <A, B, R> Either<ApplicationException, R> combine(
        Either<ApplicationException, A> eitherA,
        Either<ApplicationException, B> eitherB,
        Function2<A, B, R> combiner) {
      return eitherA.flatMap(a -> eitherB.map(b -> combiner.apply(a, b)));
    }

    public <R1, R2> Either<ApplicationException, R1> combine(
        Either<ApplicationException, R1> eitherA,
        Either<ApplicationException, R2> eitherB,
        BiConsumer<R1, R2> consumer) {
      return eitherA.peek(a -> eitherB.peek(b -> consumer.accept(a, b)));
    }

    public <R1, R2> Either<ApplicationException, Tuple2<R1, R2>> zip(
        Either<ApplicationException, R1> eitherA,
        Either<ApplicationException, R2> eitherB) {
      return eitherA.flatMap(a -> eitherB.map(b -> Tuple.of(a, b)));
    }

    public <R1, R2, R3> Either<ApplicationException, Tuple3<R1, R2, R3>> zip(
        Either<ApplicationException, R1> eitherA,
        Either<ApplicationException, R2> eitherB,
        Either<ApplicationException, R3> eitherC) {
      return eitherA.flatMap(a -> eitherB.flatMap(b -> eitherC.map(c -> Tuple.of(a, b, c))));
    }

    /**
     * Applicative pattern: Combines three Either values.
     */
    public <A, B, C, R> Either<ApplicationException, R> combine(
        Either<ApplicationException, A> eitherA,
        Either<ApplicationException, B> eitherB,
        Either<ApplicationException, C> eitherC,
        Function3<A, B, C, R> combiner) {
      return eitherA.flatMap(a -> eitherB.flatMap(b -> eitherC.map(c -> combiner.apply(a, b, c))));
    }

    public <A, B, C, R> Either<ApplicationException, R> combineM(
        Either<ApplicationException, A> eitherA,
        Either<ApplicationException, B> eitherB,
        Either<ApplicationException, C> eitherC,
        Function3<A, B, C, Either<ApplicationException, R>> combiner) {
      return eitherA.flatMap(a -> eitherB.flatMap(b -> eitherC.flatMap(c -> combiner.apply(a, b, c))));
    }

    public <A, B, R> Either<ApplicationException, R> combineM(
        Either<ApplicationException, A> eitherA,
        Either<ApplicationException, B> eitherB,
        BiFunction<A, B, Either<ApplicationException, R>> combiner) {
      return eitherA.flatMap(a -> eitherB.flatMap(b -> combiner.apply(a, b)));
    }

    public <A, B, R> Either<ApplicationException, R> combineChecked(
        Either<ApplicationException, A> eitherA,
        Either<ApplicationException, B> eitherB,
        CheckedFunction2<A, B, R> combiner) {
      return eitherA.flatMap(a -> eitherB.flatMap(b -> {
        try {
          return Either.<ApplicationException, R> right(combiner.apply(a, b));
        } catch (Throwable throwable) {
          return Either.<ApplicationException, R> left(new InternalApplicationException(
              this.defaultMessage(throwable.getClass().getSimpleName() + ": " + throwable.getMessage())));
        }
      }));
    }

    // TODO: !!test this!!
    public <R1, R2, R3> Function1<R1, Either<ApplicationException, Function1<R2, R3>>> applicative(
        Function1<R1, Function1<R2, R3>> combiner) {
      return (R1 value) -> Either.<ApplicationException, R1> right(value).map(combiner);
    }

    public <I, O> Function1<List<I>, List<O>> traverse(Function1<I, O> mapFunction) {
      return (List<I> inputs) -> inputs.stream()
          .map(mapFunction)
          .collect(Collectors.toList());
    }

    public <I, R> Function1<List<I>, Either<ApplicationException, List<R>>> traverseEither(
        Function1<I, Either<ApplicationException, R>> kleisliArrow) {
      return (List<I> inputs) -> inputs.stream()
          .map(kleisliArrow)
          .reduce(
              Either.<ApplicationException, List<R>> right(List.of()),
              (Either<ApplicationException, List<R>> acc,
                  Either<ApplicationException, R> item) -> acc.flatMap(values -> item.map(value -> {
                    List<R> next = new ArrayList<>(values);
                    next.add(value);
                    return List.copyOf(next);
                  })),
              (Either<ApplicationException, List<R>> left,
                  Either<ApplicationException, List<R>> right) -> left.flatMap(valuesLeft -> right.map(valuesRight -> {
                    List<R> merged = new ArrayList<>(valuesLeft);
                    merged.addAll(valuesRight);
                    return List.copyOf(merged);
                  })));
    }

    public <A, B> Function1<A, Either<ApplicationException, A>> flatTap(
        Function1<A, Either<ApplicationException, B>> effect) {
      return a -> effect.apply(a).map(_ -> a);
    }
  }
}
