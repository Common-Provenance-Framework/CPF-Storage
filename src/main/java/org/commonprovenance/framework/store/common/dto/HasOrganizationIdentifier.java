package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

public interface HasOrganizationIdentifier<T extends HasOrganizationIdentifier<T>> {

  String getOrganizationIdentifier();

  T withOrganizationIdentifier(String organizationIdentifier);

  static <U extends HasOrganizationIdentifier<U>, T extends HasIdentifier<T>> UnaryOperator<U> addIdentifier(T from) {
    return (U to) -> Optional.ofNullable(from)
        .map(T::getIdentifier)
        .flatMap(Optional::ofNullable)
        .map(to::withOrganizationIdentifier)
        .orElse(to);
  }
}
