package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.VerifySignatureFormDTO;

import io.vavr.control.Either;

/**
 * Creates {@link VerifySignatureFormDTO} signature-verification request data from an
 * {@link Organization} and its {@link Document}.
 */
public class VerifySignatureFormFactory {
  /**
   * Builds {@link VerifySignatureFormDTO} request data from the organization, document, and
   * signature.
   *
   * @param organization {@link Organization} associated with the document
   * @param document {@link Document} whose graph signature is being verified
   * @param signature signature to verify
   * @return the corresponding {@link VerifySignatureFormDTO}
   */
  public static VerifySignatureFormDTO build(Organization organization, Document document, String signature) {
    return new VerifySignatureFormDTO(
        organization.getIdentifier(),
        document.getGraph(),
        signature);
  }

  /**
   * Creates a function that builds and validates {@link VerifySignatureFormDTO} request data for
   * an organization.
   *
   * @param signature signature to verify
   * @return a function whose result is a right containing the validated
   *     {@link VerifySignatureFormDTO}, or a left containing the {@link ApplicationException}
   */
  public static Function<Organization, Either<ApplicationException, VerifySignatureFormDTO>> buildSafe(String signature) {
    return (Organization organization) -> Either.<ApplicationException, Organization> right(organization)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Can not build VerifySignature form, because organization is null!")))
        .flatMap(EITHER.liftEitherOptional(
            Organization::getDocument,
            _ -> new InvalidValueException("Can not build VerifySignature form, because Document is empty!")))
        .map(document -> VerifySignatureFormFactory.build(organization, document, signature))
        .flatMap(EITHER::validateDTO);

  }
}
