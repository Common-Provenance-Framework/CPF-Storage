package org.commonprovenance.framework.store.controller.dto.response.factory;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import org.commonprovenance.framework.store.controller.dto.response.OrganizationResponseDTO;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Organization;

import io.vavr.control.Either;

public class OrganizationResponseFactory {
  public static OrganizationResponseDTO build(Organization organization) {
    return new OrganizationResponseDTO(
        organization.getIdentifier(),
        organization.getClientCertificate(),
        organization.getIntermediateCertificates());
  }

  public static Either<ApplicationException, OrganizationResponseDTO> buildSafe(Organization organization) {
    return Either.<ApplicationException, Organization> right(organization)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Can not build Organization response, because organization is null!")))
        .map(OrganizationResponseFactory::build);

  }
}
