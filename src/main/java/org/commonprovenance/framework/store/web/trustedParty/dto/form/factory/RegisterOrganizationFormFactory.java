package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.RegisterOrganizationFormDTO;

import io.vavr.control.Either;

public class RegisterOrganizationFormFactory {
  public static RegisterOrganizationFormDTO build(Organization organization) {
    return new RegisterOrganizationFormDTO(
        organization.getIdentifier(),
        organization.getClientCertificate(),
        organization.getIntermediateCertificates());
  }

  public static Either<ApplicationException, RegisterOrganizationFormDTO> buildSafe(Organization organization) {
    return Either.<ApplicationException, Organization> right(organization)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Can not build RegisterOrganization form, because organization is null!")))
        .map(RegisterOrganizationFormFactory::build)
        .flatMap(EITHER::validateDTO);
  }

}
