package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

public interface HasOrganizationId<T extends HasOrganizationId<T>> {
  String getOrganizationId();

  T withOrganizationId(String organizationId);

  static <T extends HasOrganizationId<T>, F extends HasOrganizationId<F>> UnaryOperator<T> addOrganizationId(F from) {
    return (T to) -> Optional.ofNullable(from)
        .map(F::getOrganizationId)
        .map(to::withOrganizationId)
        .orElse(to);
  }

  static <T extends HasOrganizationId<T>, F extends HasIdentifier<F>> UnaryOperator<T> addOrganizationId(F from) {
    return (T to) -> Optional.ofNullable(from)
        .map(F::getIdentifier)
        .map(to::withOrganizationId)
        .orElse(to);
  }

  static <T extends HasOrganizationId<T>, F> UnaryOperator<T> addOrganizationIdIfPresent(F from) {
    return (T to) -> Optional.ofNullable(from)
        .flatMap((F v) -> (v instanceof HasOrganizationId<?> has)
            ? Optional.of(has).map(HasOrganizationId::getOrganizationId)
            : Optional.empty())
        .map(to::withOrganizationId)
        .orElse(to);
  }

}
