package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.model.GraphType;

public interface HasGraphType<T extends HasGraphType<T>> {

  String getGraphType();

  T withGraphType(GraphType graphType);

  public static <T extends HasGraphType<T>> UnaryOperator<T> setTypeAsGraph() {
    return (T to) -> to.withGraphType(GraphType.GRAPH);
  }

  public static <T extends HasGraphType<T>> UnaryOperator<T> addGraphType(GraphType graphType) {
    return (T to) -> Optional.ofNullable(graphType)
        .map(to::withGraphType)
        .orElse(to);
  }

  public static <T extends HasDocumentType<T>, F extends HasDocumentType<F>> UnaryOperator<T> addGraphType(F from) {
    return (T to) -> Optional.ofNullable(from)
        .map(F::getDocumentType)
        .map(to::withDocumentType)
        .orElse(to);
  }
}
