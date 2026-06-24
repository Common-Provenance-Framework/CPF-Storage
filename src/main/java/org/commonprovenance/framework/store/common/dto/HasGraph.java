package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

public interface HasGraph<T extends HasGraph<T>> {

  String getGraph();

  default T withGraph(String graph) {
    throw new InternalApplicationException("withGraph is not supported for read-only type:" + this.getClass().getSimpleName());
  }

  default UnaryOperator<DocumentResponseDTO> putGraphToDTO() {
    return (DocumentResponseDTO to) -> Optional.ofNullable(getGraph())
        .map(to::withGraph)
        .orElse(to);
  }

  static <U extends HasGraph<U>, F extends HasGraph<F>> UnaryOperator<U> addGraph(F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(F::getGraph)
        .map(to::withGraph)
        .orElse(to);
  }

  static <U extends HasGraph<U>, F extends HasGraph<F>> UnaryOperator<U> addGraph(Optional<F> maybeFrom) {
    return (U to) -> maybeFrom
        .map(F::getGraph)
        .map(to::withGraph)
        .orElse(to);
  }

  static <U extends HasGraph<U>, F> UnaryOperator<U> addGraphIfPresent(F from) {
    return (U to) -> Optional.ofNullable(from)
        .flatMap(HasGraph::getValue)
        .map(to::withGraph)
        .orElse(to);
  }

  private static <T> Optional<String> getValue(T form) {
    if (form instanceof HasGraph<?> has)
      return Optional.of(has.getGraph());

    if (form instanceof org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasGraph has)
      return Optional.of(has.getGraph());

    return Optional.empty();
  }
}
