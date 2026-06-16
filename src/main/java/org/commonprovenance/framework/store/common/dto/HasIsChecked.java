package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

public interface HasIsChecked<T extends HasIsChecked<T>> {

  Boolean getIsChecked();

  T withIsChecked(Boolean isChecked);

  static <U extends HasIsChecked<U>, F extends HasIsChecked<F>> UnaryOperator<U> addIsChecked(F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(F::getIsChecked)
        .map(to::withIsChecked)
        .orElse(to);
  }

  static <U extends HasIsChecked<U>, F extends org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasIsChecked> UnaryOperator<U> addIsChecked(
      F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(F::getIsChecked)
        .map(to::withIsChecked)
        .orElse(to);
  }

  static <U extends HasIsChecked<U>, F> UnaryOperator<U> addIsCheckedIfPresent(F from) {
    return (U to) -> Optional.ofNullable(from)
        .flatMap(HasIsChecked::getValue)
        .map(to::withIsChecked)
        .orElse(to);
  }

  private static <T> Optional<Boolean> getValue(T form) {
    if (form instanceof HasIsChecked<?> has)
      return Optional.of(has.getIsChecked());

    if (form instanceof org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasIsChecked has)
      return Optional.of(has.getIsChecked());

    return Optional.empty();
  }
}
