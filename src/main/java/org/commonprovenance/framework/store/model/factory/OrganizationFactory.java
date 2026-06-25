package org.commonprovenance.framework.store.model.factory;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import java.util.List;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.common.composition.Monoid;
import org.commonprovenance.framework.store.common.dto.HasClientCertificate;
import org.commonprovenance.framework.store.common.dto.HasDocumentOptional;
import org.commonprovenance.framework.store.common.dto.HasIdentifier;
import org.commonprovenance.framework.store.common.dto.HasIntermediateCertificates;
import org.commonprovenance.framework.store.common.dto.HasTrustedPartyOptional;
import org.commonprovenance.framework.store.controller.dto.form.OrganizationRegisterFormDTO;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.OrganizationNode;

import io.vavr.control.Either;

public class OrganizationFactory {

  private static <T extends HasIdentifier<T> & HasClientCertificate<T> & HasIntermediateCertificates<T>> UnaryOperator<Organization> mapper(T data) {
    return (Organization organization) -> Monoid.compose(
        organization,
        List.of(
            HasIdentifier.addIdentifier(data),
            HasClientCertificate.addClientCertificate(data),
            HasIntermediateCertificates.addIntermediateCertificates(data)));
  }

  private static <T> UnaryOperator<Organization> mapper(T data) {
    return (Organization organization) -> Monoid.compose(
        organization,
        List.of(
            HasIdentifier.addIdentifierIfPresent(data),
            HasClientCertificate.addClientCertificateIfPresent(data),
            HasIntermediateCertificates.addIntermediateCertificatesIfPresent(data)));
  }

  public static Organization build(OrganizationRegisterFormDTO form) {
    return OrganizationFactory.mapper(form).apply(new Organization());
  }

  public static Organization build(OrganizationNode form) {
    return OrganizationFactory.mapper(form).apply(new Organization());
  }

  public static Either<ApplicationException, Organization> buildWithRelations(OrganizationNode data) {
    return Either.<ApplicationException, Organization> right(new Organization())
        .map(OrganizationFactory.mapper(data))
        .flatMap(HasTrustedPartyOptional.addTrustedParty(data))
        .flatMap(HasDocumentOptional.addDocument(data))
        .flatMap(EITHER::validateDTO);
  }

  public static <T> Either<ApplicationException, Organization> buildUnsafe(T data) {
    return Either.<ApplicationException, Organization> right(new Organization())
        .map(OrganizationFactory.mapper(data))
        .flatMap(EITHER::validateDTO);
  }

}
