package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

public interface HasClientCertificate<T extends HasClientCertificate<T>> {

  String getClientCertificate();

  default T withClientCertificate(String clientCertificate) {
    throw new InternalApplicationException("withClientCertificate is not supported for read-only type:" + this.getClass().getSimpleName());
  }

  static <U extends HasClientCertificate<U>, F extends HasClientCertificate<F>> UnaryOperator<U> addClientCertificate(F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(F::getClientCertificate)
        .map(to::withClientCertificate)
        .orElse(to);
  }

  static <U extends HasClientCertificate<U>, F extends org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasClientCertificate> UnaryOperator<U> addClientCertificate(
      F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(F::getClientCertificate)
        .map(to::withClientCertificate)
        .orElse(to);
  }

  static <U extends HasClientCertificate<U>, F> UnaryOperator<U> addClientCertificateIfPresent(F from) {
    return (U to) -> Optional.ofNullable(from)
        .flatMap(HasClientCertificate::getValue)
        .map(to::withClientCertificate)
        .orElse(to);
  }

  private static <T> Optional<String> getValue(T form) {
    if (form instanceof HasClientCertificate<?> has)
      return Optional.of(has.getClientCertificate());

    if (form instanceof org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasClientCertificate has)
      return Optional.of(has.getClientCertificate());

    if (form instanceof org.commonprovenance.framework.store.common.dtos.HasClientCertificate has)
      return Optional.of(has.clientCertificate());

    return Optional.empty();
  }
}
