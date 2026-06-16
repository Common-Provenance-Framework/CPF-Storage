package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.model.GraphType;

// same as GraphType
public interface HasType<T extends HasType<T>> {

  String getType();

  T withType(String type);

  public static <T extends HasType<T>> UnaryOperator<T> setTypeAsGraph() {
    return (T to) -> to.withType(GraphType.GRAPH.toString());
  }

  public static <T extends HasType<T>> UnaryOperator<T> addType(GraphType graphType) {
    return (T to) -> Optional.ofNullable(graphType.toString())
        .map(to::withType)
        .orElse(to);
  }

  public static <T extends HasType<T>, F extends HasType<F>> UnaryOperator<T> addGraphType(F from) {
    return (T to) -> Optional.ofNullable(from)
        .map(F::getType)
        .map(to::withType)
        .orElse(to);
  }

  public static <T extends HasType<T>, F extends HasGraphType<F>> UnaryOperator<T> addGraphType(F from) {
    return (T to) -> Optional.ofNullable(from)
        .map(F::getGraphType)
        .map(to::withType)
        .orElse(to);
  }

}
