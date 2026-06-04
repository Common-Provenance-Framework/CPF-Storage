package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

public interface HasIsValid<T extends HasIsValid<T>> {

  Boolean getIsValid();

  T withIsValid(Boolean isValid);

  static <U extends HasIsValid<U>, F extends HasIsValid<F>> UnaryOperator<U> addIsValid(F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(F::getIsValid)
        .map(to::withIsValid)
        .orElse(to);
  }

  static <U extends HasIsValid<U>, F extends org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasIsValid> UnaryOperator<U> addIsValid(F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(F::getIsValid)
        .map(to::withIsValid)
        .orElse(to);
  }

  static <U extends HasIsValid<U>, F> UnaryOperator<U> addIsValidIfPresent(F from) {
    return (U to) -> Optional.ofNullable(from)
        .flatMap(HasIsValid::getValue)
        .map(to::withIsValid)
        .orElse(to);
  }

  private static <T> Optional<Boolean> getValue(T form) {
    if (form instanceof HasIsValid<?> has)
      return Optional.of(has.getIsValid());

    if (form instanceof org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasIsValid has)
      return Optional.of(has.getIsValid());

    return Optional.empty();
  }
}
