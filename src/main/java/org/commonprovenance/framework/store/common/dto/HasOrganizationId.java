package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

public interface HasOrganizationId<T extends HasOrganizationId<T>> {

  String getOrganizationId();

  T withOrganizationId(String organizationIdentifier);

  static <U extends HasOrganizationId<U>, T extends HasIdentifier<T>> UnaryOperator<U> addIdentifier(T from) {
    return (U to) -> Optional.ofNullable(from)
        .map(T::getIdentifier)
        .flatMap(Optional::ofNullable)
        .map(to::withOrganizationId)
        .orElse(to);
  }

  static <U extends HasOrganizationId<U>, T extends HasOrganizationIdentifier<T>> UnaryOperator<U> addIdentifier(
      T from) {
    return (U to) -> Optional.ofNullable(from)
        .map(T::getOrganizationIdentifier)
        .map(to::withOrganizationId)
        .orElse(to);
  }

  static <U extends HasOrganizationId<U>, T extends HasOptionalOrganizationIdentifier<T>> UnaryOperator<U> addIdentifier(
      T from) {
    return (U to) -> Optional.ofNullable(from)
        .flatMap(T::getOrganizationIdentifier)
        .map(to::withOrganizationId)
        .orElse(to);
  }
}
