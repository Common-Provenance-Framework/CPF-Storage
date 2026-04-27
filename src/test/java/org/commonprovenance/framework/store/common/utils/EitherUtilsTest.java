package org.commonprovenance.framework.store.common.utils;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.vavr.Function1;
import io.vavr.control.Either;

@DisplayName("EitherUtils - traverseEither Test")
public class EitherUtilsTest {

  private final Function1<String, Either<ApplicationException, Integer>> parseIntOrLeft = value -> {
    try {
      return Either.right(Integer.parseInt(value));
    } catch (NumberFormatException e) {
      return Either.left(new InternalApplicationException("Not a number: " +
          value));
    }
  };

  private void handleLeft(ApplicationException e) {
    fail("Left side was not expected: " + e.getMessage());
  }

  private <T> void handleRight(T v) {
    fail("Right side was not expected");
  }

  @Test
  @DisplayName("should return Right(List) when all elements succeed")
  public void shouldReturnRightList_whenAllElementsSucceed() {
    List<String> inputs = List.of("1", "2", "3");

    EITHER.traverseEither(parseIntOrLeft)
        .apply(inputs)
        .peek(result -> assertEquals(List.of(1, 2, 3), result))
        .peekLeft(this::handleLeft);
  }

  @Test
  @DisplayName("should return Right(empty list) for empty input")
  public void shouldReturnRightEmptyList_whenInputIsEmpty() {
    List<String> inputs = List.of();

    EITHER.traverseEither(parseIntOrLeft)
        .apply(inputs)
        .peek(result -> assertTrue(result.isEmpty()))
        .peekLeft(this::handleLeft);
  }

  @Test
  @DisplayName("should return Left when first element fails")
  public void shouldReturnLeft_whenFirstElementFails() {
    List<String> inputs = List.of("not-a-number", "2", "3");

    EITHER.traverseEither(parseIntOrLeft)
        .apply(inputs)
        .peek(this::handleRight)
        .peekLeft(error -> assertTrue(error.getMessage().contains("not-a-number")));
  }

  @Test
  @DisplayName("should return Left when last element fails")
  public void shouldReturnLeft_whenLastElementFails() {
    List<String> inputs = List.of("1", "2", "not-a-number");

    EITHER.traverseEither(parseIntOrLeft)
        .apply(inputs)
        .peek(this::handleRight)
        .peekLeft(error -> assertTrue(error.getMessage().contains("not-a-number")));
  }

  @Test
  @DisplayName("should return first Left when multiple elements fail")
  public void shouldReturnFirstLeft_whenMultipleElementsFail() {
    List<String> inputs = List.of("1", "bad-a", "bad-b");

    EITHER.traverseEither(parseIntOrLeft)
        .apply(inputs)
        .peek(this::handleRight)
        .peekLeft(error -> assertTrue(error.getMessage().contains("bad-a")));
  }

  @Test
  @DisplayName("should preserve order of results")
  public void shouldPreserveOrder() {
    List<String> inputs = List.of("10", "20", "30", "40");

    EITHER.traverseEither(parseIntOrLeft)
        .apply(inputs)
        .peek(result -> {
          assertEquals(4, result.size());
          assertEquals(10, result.get(0));
          assertEquals(20, result.get(1));
          assertEquals(30, result.get(2));
          assertEquals(40, result.get(3));
        })
        .peekLeft(this::handleLeft);
  }

  @Test
  @DisplayName("should return Right(single-element list) for single successful element")
  public void shouldReturnRightSingleElementList_whenSingleElementSucceeds() {
    List<String> inputs = List.of("42");

    EITHER.traverseEither(parseIntOrLeft)
        .apply(inputs)
        .peek(result -> assertEquals(List.of(42), result))
        .peekLeft(this::handleLeft);
  }
}
