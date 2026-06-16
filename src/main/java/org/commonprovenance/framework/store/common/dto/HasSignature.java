package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

public interface HasSignature<T extends HasSignature<T>> {

  String getSignature();

  default T withSignature(String signature) {
    throw new InternalApplicationException("withSignature is not supported for read-only type:" + this.getClass().getSimpleName());
  }

  static <U extends HasSignature<U>, T extends HasSignature<T>> UnaryOperator<U> addSignature(T data) {
    return (U to) -> Optional.ofNullable(data)
        .map(T::getSignature)
        .flatMap(Optional::ofNullable)
        .map(to::withSignature)
        .orElse(to);
  }

  static <U extends HasSignature<U>, T extends HasSignature<T>> UnaryOperator<U> addSignature(Optional<T> maybeData) {
    return (U to) -> maybeData
        .map(T::getSignature)
        .flatMap(Optional::ofNullable)
        .map(to::withSignature)
        .orElse(to);
  }
}
