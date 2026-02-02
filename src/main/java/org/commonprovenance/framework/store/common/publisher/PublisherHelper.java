package org.commonprovenance.framework.store.common.publisher;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PublisherHelper {
  MonoHelper MONO = new MonoHelper();
  FluxHelper FLUX = new FluxHelper();

  // Mono implementation
  class MonoHelper {
    public <T> Mono<T> makeSureNotNull(T value) {
      return this.<T>makeSureNotNullWithMessage("Input parameter can not be null.").apply(value);
    }

    public <T> Function<T, Mono<T>> makeSureNotNullWithMessage(String message) {
      return this.<T>makeSure(Objects::nonNull, message);
    }

    public <T> Function<T, Mono<T>> makeSure(Predicate<T> validator, String message) {
      return this.<T>makeSure(validator, _ -> message);
    }

    public <T> Function<T, Mono<T>> makeSure(Predicate<T> validator, Function<T, String> messageBuilder) {
      return (T value) -> validator.test(value)
          ? Mono.just(value)
          : Mono.error(new InternalApplicationException(messageBuilder.apply(value), new IllegalArgumentException()));
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

  // Flux implementation
  class FluxHelper {
    public <T> Flux<T> makeSureNotNull(T value) {
      return this.<T>makeSureNotNullWithMessage("Input parameter can not be null.").apply(value);
    }

    public <T> Function<T, Flux<T>> makeSureNotNullWithMessage(String message) {
      return makeSure(Objects::nonNull, message);
    }

    public <T> Function<T, Flux<T>> makeSure(Predicate<T> validator, String message) {
      return makeSure(validator, _ -> message);
    }

    public <T> Function<T, Flux<T>> makeSure(Predicate<T> validator, Function<T, String> messageBuilder) {
      return (T value) -> validator.test(value)
          ? Flux.just(value)
          : Flux.error(new InternalApplicationException(messageBuilder.apply(value), new IllegalArgumentException()));
    }

    public <E extends Throwable, T> Function<E, Flux<T>> exceptionWrapper(Function<E, String> messageBuilder) {
      return (
          E exception) -> (exception instanceof ApplicationException)
              ? Flux.<T>error(exception) // Propagate existing ApplicationException as is
              : Flux.<T>error(new InternalApplicationException(messageBuilder.apply(exception), exception));
    }

    public <E extends Throwable, T> Function<E, Flux<T>> exceptionWrapper(String message) {
      return exceptionWrapper(_ -> message);
    }

    public <E extends Throwable, T> Function<E, Flux<T>> exceptionWrapper() {
      return exceptionWrapper("Unexpected exception!");
    }
  }
}
