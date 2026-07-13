package org.commonprovenance.framework.store.controller.dto.response.factory;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import org.commonprovenance.framework.store.controller.dto.response.DocumentResponseDTO;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Token;

import io.vavr.control.Either;

public class DocumentResponseFactory {

  public static DocumentResponseDTO build(String document, String jwt) {
    return new DocumentResponseDTO(document, jwt);
  }

  public static Either<ApplicationException, DocumentResponseDTO> buildSafe(Document document) {
    return Either.<ApplicationException, Document> right(document)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Can not build Document response, because document is null!")))
        .flatMap(EITHER.liftEitherOptional(
            Document::getToken,
            _ -> new InvalidValueException("Can not build Token response, because Token is missing in Document!")))
        .map(Token::getJwt)
        .map(jwt -> DocumentResponseFactory.build(
            document.getGraph(),
            jwt));
  }

}
