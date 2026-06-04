package org.commonprovenance.framework.store.model.factory;

import java.util.List;
import java.util.function.Function;

import org.commonprovenance.framework.store.common.composition.MonoidComposition;
import org.commonprovenance.framework.store.common.dto.HasJwtToken;
import org.commonprovenance.framework.store.common.dto.HasTrustedPartyOptional;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TokenNode;

import io.vavr.control.Either;

public class TokenFactory {

  private static <T extends HasJwtToken<T>> Function<Token, Either<ApplicationException, Token>> mapper(T data) {
    return (Token token) -> Either.<ApplicationException, Token> right(MonoidComposition.compose(
        token,
        List.of(HasJwtToken.addJwt(data))))
        .flatMap(HasJwtToken::loadCreatedOn);
  }

  private static <T> Function<Token, Either<ApplicationException, Token>> mapper(T data) {
    return (Token token) -> Either.<ApplicationException, Token> right(MonoidComposition.compose(
        token,
        List.of(HasJwtToken.addJwtIfPresent(data))))
        .flatMap(HasJwtToken::loadCreatedOn);
  }

  public static Either<ApplicationException, Token> build(TokenNode data) {
    return Either.<ApplicationException, Token> right(new Token())
        .flatMap(TokenFactory.mapper(data));
  }

  public static Either<ApplicationException, Token> buildWithRelations(TokenNode value) {
    return build(value)
        .flatMap(HasTrustedPartyOptional.addTrustedParty(value));
  }

  public static <T extends HasJwtToken<T>> Either<ApplicationException, Token> build(T data) {
    return Either.<ApplicationException, Token> right(new Token())
        .flatMap(TokenFactory.mapper(data));
  }

}
