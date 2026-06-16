package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

public interface HasIsDefault<T extends HasIsDefault<T>> {

  Boolean getIsDefault();

  T withIsDefault(Boolean isDefault);

  static <U extends HasIsDefault<U>, F extends HasIsDefault<F>> UnaryOperator<U> addIsDefault(F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(F::getIsDefault)
        .map(to::withIsDefault)
        .orElse(to);
  }

  static <U extends HasIsDefault<U>, F extends org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasIsDefault> UnaryOperator<U> addIsDefault(
      F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(F::getIsDefault)
        .map(to::withIsDefault)
        .orElse(to);
  }

  static <U extends HasIsDefault<U>, F> UnaryOperator<U> addIsDefaultIfPresent(F from) {
    return (U to) -> Optional.ofNullable(from)
        .flatMap(HasIsDefault::getValue)
        .map(to::withIsDefault)
        .orElse(to);
  }

  private static <T> Optional<Boolean> getValue(T form) {
    if (form instanceof HasIsDefault<?> has)
      return Optional.of(has.getIsDefault());

    if (form instanceof org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasIsDefault has)
      return Optional.of(has.getIsDefault());

    return Optional.empty();
  }
}
