package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import java.util.List;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.common.composition.Monoid;
import org.commonprovenance.framework.store.common.dto.HasClientCertificate;
import org.commonprovenance.framework.store.common.dto.HasIntermediateCertificates;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.UpdateOrganizationTPFormDTO;

import io.vavr.control.Either;

public class UpdateOrganizationFormFactory {
  private static <T extends HasClientCertificate<T> & HasIntermediateCertificates<T>> UnaryOperator<UpdateOrganizationTPFormDTO> mapper(T data) {
    return Monoid.<UpdateOrganizationTPFormDTO> composeOperators(
        List.of(
            HasClientCertificate.addClientCertificate(data),
            HasIntermediateCertificates.addIntermediateCertificates(data)));
  }

  public static Either<ApplicationException, UpdateOrganizationTPFormDTO> build(Organization organization) {
    return Either.<ApplicationException, UpdateOrganizationTPFormDTO> right(new UpdateOrganizationTPFormDTO())
        .map(UpdateOrganizationFormFactory.mapper(organization))
        .flatMap(EITHER::validateDTO);
  }
}
