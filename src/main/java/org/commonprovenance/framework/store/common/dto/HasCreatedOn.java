package org.commonprovenance.framework.store.common.dto;

import java.time.Instant;
import java.util.Optional;
import java.util.function.UnaryOperator;

public interface HasCreatedOn<T extends HasCreatedOn<T>> {
  Long getCreatedOn();

  T withCreatedOn(Long createdOn);

  public static <U extends HasCreatedOn<U>> UnaryOperator<U> setCurrentTimeSecond() {
    return (U to) -> to.withCreatedOn(Instant.now().getEpochSecond());
  }

  static <U extends HasCreatedOn<U>, F extends HasCreatedOn<F>> UnaryOperator<U> addCreatedOn(F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(F::getCreatedOn)
        .map(to::withCreatedOn)
        .orElse(to);
  }
}
