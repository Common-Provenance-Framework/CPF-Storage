package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.VerifySignatureFormDTO;

import io.vavr.control.Either;

public class VerifySignatureFormFactory {
  public static Function<Organization, Either<ApplicationException, VerifySignatureFormDTO>> build(String signature) {
    return (Organization organization) -> Either.<ApplicationException, Organization> right(organization)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Can not build VerifySignature form, because organization is null!")))
        .flatMap(EITHER.liftEitherOptional(
            Organization::getDocument,
            _ -> new InvalidValueException("Can not build VerifySignature form, because Document is empty!")))
        .map(document -> new VerifySignatureFormDTO(organization.getIdentifier(), document.getGraph(), signature))
        .flatMap(EITHER::validateDTO);

  }
}
