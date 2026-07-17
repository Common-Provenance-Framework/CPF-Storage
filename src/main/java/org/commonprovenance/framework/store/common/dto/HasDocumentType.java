package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.model.GraphType;

// same as GraphType
public interface HasDocumentType<T extends HasDocumentType<T>> {

  String getDocumentType();

  T withDocumentType(String type);

  public static <T extends HasDocumentType<T>> UnaryOperator<T> setTypeAsGraph() {
    return (T to) -> to.withDocumentType(GraphType.GRAPH.toString());
  }

  public static <T extends HasDocumentType<T>> UnaryOperator<T> addType(GraphType graphType) {
    return (T to) -> Optional.ofNullable(graphType.toString())
        .map(to::withDocumentType)
        .orElse(to);
  }

  public static <T extends HasDocumentType<T>, F extends HasDocumentType<F>> UnaryOperator<T> addGraphType(F from) {
    return (T to) -> Optional.ofNullable(from)
        .map(F::getDocumentType)
        .map(to::withDocumentType)
        .orElse(to);
  }

  public static <T extends HasDocumentType<T>, F extends HasGraphType<F>> UnaryOperator<T> addGraphType(F from) {
    return (T to) -> Optional.ofNullable(from)
        .map(F::getGraphType)
        .map(to::withDocumentType)
        .orElse(to);
  }

}
