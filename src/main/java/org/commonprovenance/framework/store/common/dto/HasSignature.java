package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

public interface HasSignature<T extends HasSignature<T>> {

  String getSignature();

  T withSignature(String signature);

  static <U extends HasSignature<U>, T extends HasSignature<T>> UnaryOperator<U> addSignature(T from) {
    return (U to) -> Optional
        .ofNullable(from)
        .map(T::getSignature)
        .flatMap(Optional::ofNullable)
        .map(to::withSignature)
        .orElse(to);
  }
}
