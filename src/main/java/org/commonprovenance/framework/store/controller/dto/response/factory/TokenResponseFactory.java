package org.commonprovenance.framework.store.controller.dto.response.factory;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import org.commonprovenance.framework.store.controller.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;

import io.vavr.control.Either;

public class TokenResponseFactory {
  public static TokenResponseDTO build(Token token) {
    return new TokenResponseDTO(
        token.getJwt());
  }

  public static Either<ApplicationException, TokenResponseDTO> buildSafe(Token token) {
    return Either.<ApplicationException, Token> right(token)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Can not build Token response, because token is null!")))
        .map(TokenResponseFactory::build);

  }

  public static Either<ApplicationException, TokenResponseDTO> buildFromDocument(Document document) {
    return Either.<ApplicationException, Document> right(document)
        .flatMap(EITHER.liftEitherOptional(
            Document::getToken,
            _ -> new InvalidValueException("Can not build Token response, because Token is missing in Document!")))
        .flatMap(TokenResponseFactory::buildSafe);
  }

  public static Either<ApplicationException, TokenResponseDTO> buildFromOrganization(Organization organization) {
    return Either.<ApplicationException, Organization> right(organization)
        .flatMap(EITHER.liftEitherOptional(
            Organization::getDocument,
            _ -> new InvalidValueException("Can not build Token response, because Document is missing in Organization!")))
        .flatMap(TokenResponseFactory::buildFromDocument);
  }
}
