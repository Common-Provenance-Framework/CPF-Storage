package org.commonprovenance.framework.store.controller.dto.response.factory;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.util.List;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.common.composition.MonoidComposition;
import org.commonprovenance.framework.store.common.dto.HasJwtToken;
import org.commonprovenance.framework.store.controller.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;

import io.vavr.control.Either;

public class TokenResponseFactory {
  private static <T extends HasJwtToken<T>> UnaryOperator<TokenResponseDTO> mapper(T data) {
    return (TokenResponseDTO response) -> MonoidComposition.compose(
        response,
        List.of(
            HasJwtToken.addJwt(data)));
  }

  public static <T extends HasJwtToken<T>> TokenResponseDTO build(T data) {
    return mapper(data).apply(new TokenResponseDTO());
  }

  public static <T extends HasJwtToken<T>> UnaryOperator<TokenResponseDTO> append(T data) {
    return (TokenResponseDTO response) -> mapper(data).apply(response);
  }

  public static Either<ApplicationException, TokenResponseDTO> build(Organization organization) {
    return Either.<ApplicationException, Organization> right(organization)
        .flatMap(EITHER.liftEitherOptional(
            Organization::getDocument,
            _ -> new InvalidValueException("Document is missing!")))
        .flatMap(EITHER.liftEitherOptional(
            Document::getToken,
            _ -> new InvalidValueException("Token is missing!")))
        .map(TokenResponseFactory::build);
  }
}
