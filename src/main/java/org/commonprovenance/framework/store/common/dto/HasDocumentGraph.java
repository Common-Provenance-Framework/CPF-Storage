package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

public interface HasDocumentGraph<T extends HasDocumentGraph<T>> {

  String getDocument();

  T withDocument(String graph);

  static <U extends HasDocumentGraph<U>, F extends HasDocumentGraph<F>> UnaryOperator<U> addDocument(F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(F::getDocument)
        .map(to::withDocument)
        .orElse(to);
  }

  static <U extends HasDocumentGraph<U>, F extends HasGraph<F>> UnaryOperator<U> addDocument(Optional<F> maybeFrom) {
    return (U to) -> maybeFrom
        .map(F::getGraph)
        .map(to::withDocument)
        .orElse(to);
  }

  static <U extends HasDocumentGraph<U>, F> UnaryOperator<U> addDocumentIfPresent(F from) {
    return (U to) -> Optional.ofNullable(from)
        .flatMap(HasDocumentGraph::getValue)
        .map(to::withDocument)
        .orElse(to);
  }

  private static <T> Optional<String> getValue(T form) {
    if (form instanceof HasDocumentGraph<?> has)
      return Optional.of(has.getDocument());

    if (form instanceof HasGraph has)
      return Optional.of(has.getGraph());

    return Optional.empty();
  }
}
