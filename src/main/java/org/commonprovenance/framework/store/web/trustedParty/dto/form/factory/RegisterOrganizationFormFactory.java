package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import java.util.List;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.common.composition.Monoid;
import org.commonprovenance.framework.store.common.dto.HasClientCertificate;
import org.commonprovenance.framework.store.common.dto.HasIdentifier;
import org.commonprovenance.framework.store.common.dto.HasIntermediateCertificates;
import org.commonprovenance.framework.store.common.dto.HasOrganizationId;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.RegisterOrganizationTPFormDTO;

import io.vavr.control.Either;

public class RegisterOrganizationFormFactory {
  private static <T extends HasIdentifier<T> & HasClientCertificate<T> & HasIntermediateCertificates<T>> UnaryOperator<RegisterOrganizationTPFormDTO> mapper(T data) {
    return Monoid.<RegisterOrganizationTPFormDTO> composeOperators(
        List.of(
            HasOrganizationId.addOrganizationId(data),
            HasClientCertificate.addClientCertificate(data),
            HasIntermediateCertificates.addIntermediateCertificates(data)));
  }

  public static Either<ApplicationException, RegisterOrganizationTPFormDTO> build(Organization organization) {
    return Either.<ApplicationException, RegisterOrganizationTPFormDTO> right(new RegisterOrganizationTPFormDTO())
        .map(RegisterOrganizationFormFactory.mapper(organization))
        .flatMap(EITHER::validateDTO);
  }

}
