package org.commonprovenance.framework.store.common.dto;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.util.List;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.TokenNodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TokenNode;

import io.vavr.control.Either;

public interface HasTokenNodeList<T extends HasTokenNodeList<T>> {
  List<TokenNode> getTokens();

  T withToken(TokenNode token);

  static <T extends HasTokenNodeList<T>, F extends HasTokenOptional<F>> Function<T, Either<ApplicationException, T>> addToken(F from) {
    return (T to) -> EITHER.makeSureNotNull(from)
        .flatMap(EITHER.liftEitherOptional(F::getToken))
        .map(TokenNodeFactory::build)
        .map(to::withToken);
  }

  static <T extends HasTokenNodeList<T>, F extends HasTokenOptional<F>> Function<T, Either<ApplicationException, T>> addTokenFull(F from) {
    return (T to) -> EITHER.makeSureNotNull(from)
        .flatMap(EITHER.liftEitherOptional(F::getToken))
        .map(TokenNodeFactory::buildWithRelations)
        .map(to::withToken);
  }

}
