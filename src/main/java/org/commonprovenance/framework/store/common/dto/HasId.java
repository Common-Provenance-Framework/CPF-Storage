package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

public interface HasId<T extends HasId<T>> {
  String getId();

  default T withId(String identifier) {
    throw new InternalApplicationException("withId is not supported for read-only type:" + this.getClass().getSimpleName());
  }

  static <T extends HasId<T>, F extends HasId<F>> UnaryOperator<T> addId(F from) {
    return (T to) -> Optional.ofNullable(from)
        .map(F::getId)
        .map(to::withId)
        .orElse(to);
  }

  static <T extends HasId<T>, F extends org.commonprovenance.framework.store.common.dtos.HasId> UnaryOperator<T> addId(F from) {
    return (T to) -> Optional.ofNullable(from)
        .map(F::id)
        .map(to::withId)
        .orElse(to);
  }

  static <T extends HasId<T>, F> UnaryOperator<T> addIdIfPresent(F from) {
    return (T to) -> Optional.ofNullable(from)
        .flatMap(HasId::getValue)
        .map(to::withId)
        .orElse(to);
  }

  private static <T> Optional<String> getValue(T form) {
    if (form instanceof HasId<?> has)
      return Optional.of(has.getId());

    if (form instanceof org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasName has)
      return Optional.of(has.getName());

    if (form instanceof org.commonprovenance.framework.store.common.dtos.HasId has)
      return Optional.of(has.id());

    return Optional.empty();
  }
}
