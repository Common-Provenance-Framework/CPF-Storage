package org.commonprovenance.framework.store.common.publisher;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PublisherHelper {
  MonoHelper MONO = new MonoHelper();
  FluxHelper FLUX = new FluxHelper();

  // Mono implementation
  class MonoHelper {
    public <T> Mono<T> makeSureNotNull(T value) {
      return this.<T>makeSureNotNullWithMessage("DTO can not be null.").apply(value);
    }

    public <T> Function<T, Mono<T>> makeSureNotNullWithMessage(String message) {
      return makeSure(Objects::nonNull, message);
    }

    public <T> Function<T, Mono<T>> makeSure(Predicate<T> validator, String message) {
      return (T value) -> validator.test(value)
          ? Mono.just(value)
          : Mono.error(new InternalApplicationException(message, new IllegalArgumentException()));
    }

    public <T> Function<T, Mono<T>> makeSure(Predicate<T> validator, Function<T, String> messageBuilder) {
      return (T value) -> validator.test(value)
          ? Mono.just(value)
          : Mono.error(new InternalApplicationException(messageBuilder.apply(value), new IllegalArgumentException()));
    }

    public <E extends Throwable, T> Function<E, Mono<T>> exceptionWrapper(Function<E, String> messageBuilder) {
      return (E exception) -> Mono
          .<T>error(new InternalApplicationException(messageBuilder.apply(exception), exception));
    }

    public <E extends Throwable, T> Function<E, Mono<T>> exceptionWrapper(String message) {
      return (E exception) -> Mono.<T>error(new InternalApplicationException(message, exception));
    }

    public <E extends Throwable, T> Function<E, Mono<T>> exceptionWrapper() {
      return (E exception) -> Mono.<T>error(new InternalApplicationException("Unexpected exception!", exception));
    }
  }

  // Flux implementation
  class FluxHelper {
    public <T> Function<T, Flux<T>> makeSureNotNullWithMessage(String message) {
      return makeSure(Objects::nonNull, message);
    }

    public <T> Function<T, Flux<T>> makeSure(Predicate<T> validator, String message) {
      return (T value) -> validator.test(value)
          ? Flux.just(value)
          : Flux.error(new InternalApplicationException(message, new IllegalArgumentException()));
    }

    public <T> Function<T, Flux<T>> makeSure(Predicate<T> validator, Function<T, String> messageBuilder) {
      return (T value) -> validator.test(value)
          ? Flux.just(value)
          : Flux.error(new InternalApplicationException(messageBuilder.apply(value), new IllegalArgumentException()));
    }

    public <E extends Throwable, T> Function<E, Flux<T>> exceptionWrapper(Function<E, String> messageBuilder) {
      return (E exception) -> Flux
          .<T>error(new InternalApplicationException(messageBuilder.apply(exception), exception));
    }

    public <E extends Throwable, T> Function<E, Flux<T>> exceptionWrapper(String message) {
      return (E exception) -> Flux.<T>error(new InternalApplicationException(message, exception));
    }

    public <E extends Throwable, T> Function<E, Flux<T>> exceptionWrapper() {
      return (E exception) -> Flux.<T>error(new InternalApplicationException("Unexpected exception!", exception));
    }
  }
}
