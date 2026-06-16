package org.commonprovenance.framework.store.common.dto;

import java.util.Optional;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.model.TrustedParty;

public interface HasTrustedParty<T extends HasTrustedParty<T>> {
  TrustedParty getTrustedParty();

  T withTrustedParty(TrustedParty trustedParty);

  static <T extends HasTrustedParty<T>, F extends HasTrustedParty<F>> UnaryOperator<T> addTrustedParty(F from) {
    return (T to) -> Optional.ofNullable(from)
        .map(F::getTrustedParty)
        .map(to::withTrustedParty)
        .orElse(to);
  }

  static <T extends HasTrustedParty<T>, F extends HasTrustedPartyOptional<F>> UnaryOperator<T> addTrustedParty(F from) {
    return (T to) -> Optional.ofNullable(from)
        .flatMap(F::getTrustedParty)
        .map(to::withTrustedParty)
        .orElse(to);
  }

}
