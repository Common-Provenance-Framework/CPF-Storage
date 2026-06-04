package org.commonprovenance.framework.store.common.dto;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.factory.TokenFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TokenNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasTokenNodes;

import io.vavr.control.Either;

public interface HasTokenOptional<T extends HasTokenOptional<T>> {
  Optional<Token> getToken();

  T withToken(Token token);

  static <T extends HasTokenOptional<T>, F extends HasTokenOptional<F>> UnaryOperator<T> addToken(F from) {
    return (T to) -> Optional.ofNullable(from)
        .flatMap(F::getToken)
        .map(to::withToken)
        .orElse(to);
  }

  static <T extends HasTokenOptional<T>, F extends HasTokenNodes> Function<T, Either<ApplicationException, T>> addToken(F from) {
    return (T to) -> Either.<ApplicationException, F> right(from)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Form Object can not be null!")))
        .map(F::getTokens)
        .flatMap(EITHER.<List<TokenNode>> makeSure(
            tokens -> tokens.size() == 1,
            tokens -> new InvalidValueException("Exactly one Token expected, got " + tokens.size() + "!")))
        .map(List::getFirst)
        .flatMap(TokenFactory::build)
        .map(to::withToken);
  }
}
