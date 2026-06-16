package org.commonprovenance.framework.store.controller.dto.response.factory;

import java.util.List;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.common.composition.MonoidComposition;
import org.commonprovenance.framework.store.common.dto.HasGraph;
import org.commonprovenance.framework.store.common.dto.HasTokenOptional;
import org.commonprovenance.framework.store.common.dto.HasTokenResponse;
import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;

public class DocumentResponseFactory {

  private static <T extends HasGraph<T> & HasTokenOptional<T>> UnaryOperator<DocumentResponseDTO> mapper(T data) {
    return (DocumentResponseDTO response) -> MonoidComposition.compose(
        response,
        List.of(
            HasGraph.addGraph(data),
            HasTokenResponse.addToken(data)));
  }

  public static <T extends HasGraph<T> & HasTokenOptional<T>> DocumentResponseDTO build(T data) {
    return mapper(data).apply(new DocumentResponseDTO());
  }

  public static <T extends HasGraph<T> & HasTokenOptional<T>> UnaryOperator<DocumentResponseDTO> append(T data) {
    return (DocumentResponseDTO response) -> mapper(data).apply(response);
  }
}
