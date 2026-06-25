package org.commonprovenance.framework.store.common.dto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.controller.dto.response.OrganizationResponseDTO;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

public interface HasIntermediateCertificates<T extends HasIntermediateCertificates<T>> {

  List<String> getIntermediateCertificates();

  default T withIntermediateCertificates(List<String> intermediateCertificates) {
    throw new InternalApplicationException("withIntermediateCertificates is not supported for read-only type:" + this.getClass().getSimpleName());
  }

  default UnaryOperator<OrganizationResponseDTO> putIntermediateCertificatesToDTO() {
    return (OrganizationResponseDTO to) -> Optional.ofNullable(getIntermediateCertificates())
        .map(to::withIntermediateCertificates)
        .orElse(to);
  }

  static <U extends HasIntermediateCertificates<U>, F extends HasIntermediateCertificates<F>> UnaryOperator<U> addIntermediateCertificates(F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(F::getIntermediateCertificates)
        .map(to::withIntermediateCertificates)
        .orElse(to);
  }

  static <U extends HasIntermediateCertificates<U>, F> UnaryOperator<U> addIntermediateCertificatesIfPresent(F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(HasIntermediateCertificates::getValue)
        .map(to::withIntermediateCertificates)
        .orElse(to);
  }

  private static <T> List<String> getValue(T form) {
    if (form instanceof HasIntermediateCertificates<?> has)
      return has.getIntermediateCertificates();

    if (form instanceof org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasIntermediateCertificates has)
      return has.getIntermediateCertificates();

    return Collections.emptyList();
  }
}
