package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

public interface HasUrlOptional<T extends HasUrlOptional<T>> {

  Optional<String> getUrl();

  default T withUrl(String url) {
    throw new InternalApplicationException("withUrl is not supported for read-only type:" + this.getClass().getSimpleName());
  }

  static <U extends HasUrlOptional<U>, F extends HasUrlOptional<F>> UnaryOperator<U> addUrl(F from) {
    return (U to) -> Optional.ofNullable(from)
        .flatMap(F::getUrl)
        .map(to::withUrl)
        .orElse(to);
  }

  static <U extends HasUrlOptional<U>, F extends HasUrl<F>> UnaryOperator<U> addUrl(F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(F::getUrl)
        .map(to::withUrl)
        .orElse(to);
  }

  static <U extends HasUrlOptional<U>, F extends org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasUrl> UnaryOperator<U> addUrl(F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(F::getUrl)
        .map(to::withUrl)
        .orElse(to);
  }

  static <U extends HasUrlOptional<U>, F> UnaryOperator<U> addUrlIfPresent(F from) {
    return (U to) -> Optional.ofNullable(from)
        .flatMap(HasUrlOptional::getValue)
        .map(to::withUrl)
        .orElse(to);
  }

  private static <T> Optional<String> getValue(T form) {
    if (form instanceof HasUrl<?> has)
      return Optional.of(has.getUrl());

    if (form instanceof HasUrlOptional<?> maybeHas)
      return maybeHas.getUrl();

    if (form instanceof org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasUrl has)
      return Optional.of(has.getUrl());

    return Optional.empty();
  }
}
