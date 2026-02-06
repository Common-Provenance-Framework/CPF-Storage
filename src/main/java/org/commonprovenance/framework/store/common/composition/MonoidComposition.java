package org.commonprovenance.framework.store.common.composition;

import java.util.List;
import java.util.function.UnaryOperator;

public class MonoidComposition {

  /**
   * Monoid composition for any type T with endomorphism transformations.
   * Identity: UnaryOperator.identity()
   * Binary operation: (f, g) -> f.andThen(g)
   * Applies the composed operators to the initial value.
   */
  public static <T> T compose(
      T initial,
      List<UnaryOperator<T>> operators) {
    return composeOperators(operators).apply(initial);
  }

  /**
   * Monoid composition for operators without applying to a value.
   * Returns a composed operator that can be applied later.
   */
  public static <T> UnaryOperator<T> composeOperators(
      List<UnaryOperator<T>> operators) {
    return operators.stream()
        .reduce(UnaryOperator.identity(), (f, g) -> x -> f.andThen(g).apply(x));
  }
}
