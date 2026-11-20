package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.RegisterOrganizationFormDTO;

import io.vavr.control.Either;

/**
 * Creates {@link RegisterOrganizationFormDTO} registration request data from an
 * {@link Organization}.
 */
public class RegisterOrganizationFormFactory {
  /**
   * Builds registration request data from the {@link Organization organization}'s identifier and
   * certificates.
   *
   * @param organization {@link Organization} to register
   * @return the corresponding {@link RegisterOrganizationFormDTO}
   */
  public static RegisterOrganizationFormDTO build(Organization organization) {
    return new RegisterOrganizationFormDTO(
        organization.getIdentifier(),
        organization.getClientCertificate(),
        organization.getIntermediateCertificates());
  }

  /**
   * Builds and validates registration request data, representing failures as an
   * {@link ApplicationException}.
   *
   * @param organization {@link Organization} to register
   * @return a right containing the validated {@link RegisterOrganizationFormDTO}, or a left
   *     containing the {@link ApplicationException}
   */
  public static Either<ApplicationException, RegisterOrganizationFormDTO> buildSafe(Organization organization) {
    return Either.<ApplicationException, Organization> right(organization)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Can not build RegisterOrganization form, because organization is null!")))
        .map(RegisterOrganizationFormFactory::build)
        .flatMap(EITHER::validateDTO);
  }
}
