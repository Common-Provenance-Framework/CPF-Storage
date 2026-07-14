package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.model.GraphFormat;

public interface HasFormatSerialized<T extends HasFormatSerialized<T>> {
  String getDocumentFormat();

  T withDocumentFormat(GraphFormat documentFormat);

  static <T extends HasFormatSerialized<T>, F extends HasFormat<F>> UnaryOperator<T> addFormat(F from) {
    return (T to) -> Optional.ofNullable(from)
        .map(F::getFormat)
        .map(to::withDocumentFormat)
        .orElse(to);
  }

  static <T extends HasFormatSerialized<T>, F extends HasFormat<F>> UnaryOperator<T> addFormat(Optional<F> maybeForm) {
    return (T to) -> maybeForm
        .map(F::getFormat)
        .map(to::withDocumentFormat)
        .orElse(to);
  }
}
