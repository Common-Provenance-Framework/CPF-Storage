package org.commonprovenance.framework.store.controller.dto.response.factory;

import java.util.List;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.common.composition.Monoid;
import org.commonprovenance.framework.store.common.dto.HasClientCertificate;
import org.commonprovenance.framework.store.common.dto.HasIdentifier;
import org.commonprovenance.framework.store.common.dto.HasIntermediateCertificates;
import org.commonprovenance.framework.store.controller.dto.response.OrganizationResponseDTO;

public class OrganizationResponseFactory {
  private static <T extends HasIdentifier<T> & HasClientCertificate<T> & HasIntermediateCertificates<T>> UnaryOperator<OrganizationResponseDTO> mapper(T data) {
    return (OrganizationResponseDTO response) -> Monoid.compose(
        response,
        List.of(
            data.putIdentifiarToDTO(),
            data.putClientCertificatToDTO(),
            data.putIntermediateCertificatesToDTO()));
  }

  public static <T extends HasIdentifier<T> & HasClientCertificate<T> & HasIntermediateCertificates<T>> OrganizationResponseDTO build(T data) {
    return mapper(data).apply(new OrganizationResponseDTO());
  }

  public static <T extends HasIdentifier<T> & HasClientCertificate<T> & HasIntermediateCertificates<T>> UnaryOperator<OrganizationResponseDTO> append(T data) {
    return (OrganizationResponseDTO response) -> mapper(data).apply(response);
  }
}
