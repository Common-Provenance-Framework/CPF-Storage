package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.UpdateOrganizationFormDTO;

import io.vavr.control.Either;

/**
 * Creates {@link UpdateOrganizationFormDTO} update request data from an {@link Organization}.
 */
public class UpdateOrganizationFormFactory {

  /**
   * Builds update request data from the {@link Organization organization's} identifier and
   * certificates.
   *
   * @param organization {@link Organization} whose certificates should be updated
   * @return the corresponding {@link UpdateOrganizationFormDTO}
   */
  public static UpdateOrganizationFormDTO build(Organization organization) {
    return new UpdateOrganizationFormDTO(
        organization.getIdentifier(),
        organization.getClientCertificate(),
        organization.getIntermediateCertificates());
  }

  /**
   * Builds and validates update request data, representing failures as an
   * {@link ApplicationException}.
   *
   * @param organization {@link Organization} whose certificates should be updated
   * @return a right containing the validated {@link UpdateOrganizationFormDTO}, or a left
   *     containing the {@link ApplicationException}
   */
  public static Either<ApplicationException, UpdateOrganizationFormDTO> buildSafe(Organization organization) {
    return Either.<ApplicationException, Organization> right(organization)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Can not build UpdateOrganization form, because organization is null!")))
        .map(UpdateOrganizationFormFactory::build)
        .flatMap(EITHER::validateDTO);
  }
}
