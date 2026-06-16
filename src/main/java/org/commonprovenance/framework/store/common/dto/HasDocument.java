package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.model.Document;

public interface HasDocument<T extends HasDocument<T>> {
  Document getDocument();

  T withDocument(Document document);

  static <U extends HasDocument<U>, F extends HasDocument<F>> UnaryOperator<U> addGraph(F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(F::getDocument)
        .map(to::withDocument)
        .orElse(to);
  }

  static <U extends HasDocument<U>, F extends HasDocumentOptional<F>> UnaryOperator<U> addGraph(F from) {
    return (U to) -> Optional.ofNullable(from)
        .flatMap(F::getDocument)
        .map(to::withDocument)
        .orElse(to);
  }
}
