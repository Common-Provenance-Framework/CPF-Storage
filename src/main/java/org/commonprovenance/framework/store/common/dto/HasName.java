package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

public interface HasName<T extends HasName<T>> {

  String getName();

  T withName(String name);

  static <U extends HasName<U>, F extends HasName<F>> UnaryOperator<U> addName(F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(F::getName)
        .map(to::withName)
        .orElse(to);
  }

  static <U extends HasName<U>, F extends org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasName> UnaryOperator<U> addName(F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(F::getName)
        .map(to::withName)
        .orElse(to);
  }

  static <U extends HasName<U>, F> UnaryOperator<U> addNameIfPresent(F from) {
    return (U to) -> Optional.ofNullable(from)
        .flatMap(HasName::getValue)
        .map(to::withName)
        .orElse(to);
  }

  private static <T> Optional<String> getValue(T form) {
    if (form instanceof HasName<?> has)
      return Optional.of(has.getName());

    if (form instanceof org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasName has)
      return Optional.of(has.getName());

    return Optional.empty();
  }
}
