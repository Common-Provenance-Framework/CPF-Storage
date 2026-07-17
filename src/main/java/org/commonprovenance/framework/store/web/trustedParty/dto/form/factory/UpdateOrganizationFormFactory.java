package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.UpdateOrganizationFormDTO;

import io.vavr.control.Either;

public class UpdateOrganizationFormFactory {

  public static UpdateOrganizationFormDTO build(Organization organization) {
    return new UpdateOrganizationFormDTO(
        organization.getClientCertificate(),
        organization.getIntermediateCertificates());
  }

  public static Either<ApplicationException, UpdateOrganizationFormDTO> buildSafe(Organization organization) {
    return Either.<ApplicationException, Organization> right(organization)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Can not build UpdateOrganization form, because organization is null!")))
        .map(UpdateOrganizationFormFactory::build)
        .flatMap(EITHER::validateDTO);
  }
}
