package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

public interface HasUrl<T extends HasUrl<T>> {

  String getUrl();

  default T withUrl(String url) {
    throw new InternalApplicationException("withUrl is not supported for read-only type:" + this.getClass().getSimpleName());
  }

  static <U extends HasUrl<U>, F extends HasUrl<F>> UnaryOperator<U> addUrl(F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(F::getUrl)
        .map(to::withUrl)
        .orElse(to);
  }

  static <U extends HasUrl<U>, F extends HasUrlOptional<F>> UnaryOperator<U> addUrl(F from) {
    return (U to) -> Optional.ofNullable(from)
        .flatMap(F::getUrl)
        .map(to::withUrl)
        .orElse(to);
  }

  static <U extends HasUrl<U>, F> UnaryOperator<U> addUrlIfPresent(F from) {
    return (U to) -> Optional.ofNullable(from)
        .flatMap((F v) -> (v instanceof HasUrl<?> has)
            ? Optional.of(has)
            : Optional.empty())
        .map(HasUrl::getUrl)
        .map(to::withUrl)
        .orElse(to);
  }
}
