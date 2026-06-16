package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

public interface HasOrganizationIdentifierOptional<T extends HasOrganizationIdentifierOptional<T>> {

  Optional<String> getOrganizationIdentifier();

  T withOrganizationIdentifier(String organizationIdentifier);

  static <U extends HasOrganizationIdentifier<U>, T extends HasOrganizationIdentifierOptional<T>> UnaryOperator<U> addIdentifier(
      T from) {
    return (U to) -> Optional.ofNullable(from)
        .flatMap(T::getOrganizationIdentifier)
        .map(to::withOrganizationIdentifier)
        .orElse(to);
  }
}
