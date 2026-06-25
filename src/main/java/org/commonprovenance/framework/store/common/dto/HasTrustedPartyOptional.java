package org.commonprovenance.framework.store.common.dto;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.model.factory.TrustedPartyFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasTrustedPartyNodes;

import io.vavr.control.Either;

public interface HasTrustedPartyOptional<T extends HasTrustedPartyOptional<T>> {
  Optional<TrustedParty> getTrustedParty();

  T withTrustedParty(TrustedParty trustedParty);

  static <T extends HasTrustedPartyOptional<T>, F extends HasTrustedPartyOptional<F>> UnaryOperator<T> addTrustedParty(F from) {
    return (T to) -> Optional.ofNullable(from)
        .flatMap(F::getTrustedParty)
        .map(to::withTrustedParty)
        .orElse(to);
  }

  static <T extends HasTrustedPartyOptional<T>, F extends HasTrustedParty<F>> UnaryOperator<T> addTrustedParty(F from) {
    return (T to) -> Optional.ofNullable(from)
        .map(F::getTrustedParty)
        .map(to::withTrustedParty)
        .orElse(to);
  }

  static <T extends HasTrustedPartyOptional<T>, F extends HasTrustedPartyNodes> Function<T, Either<ApplicationException, T>> addTrustedParty(F from) {
    return (T to) -> Either.<ApplicationException, F> right(from)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Form Object can not be null!")))
        .map(F::getTrustedParties)
        .flatMap(EITHER.<List<TrustedPartyNode>> makeSure(
            trustedParties -> trustedParties.size() <= 1,
            trustedParties -> new InvalidValueException("Max one TrustedParty expected, got " + trustedParties.size() + "!")))
        .flatMap(trustedParties -> trustedParties.size() == 0
            ? Either.<ApplicationException, T> right(to)
            : Either.<ApplicationException, TrustedPartyNode> right(trustedParties.getFirst())
                .map(TrustedPartyFactory::build)
                .map(to::withTrustedParty));
  }

  static <T extends HasTrustedPartyOptional<T>, F extends HasTrustedPartyNodes> Function<T, Either<ApplicationException, T>> addTrustedPartyStrict(F from) {
    return (T to) -> Either.<ApplicationException, F> right(from)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Form Object can not be null!")))
        .map(F::getTrustedParties)
        .flatMap(EITHER.<List<TrustedPartyNode>> makeSure(
            trustedParties -> trustedParties.size() == 1,
            trustedParties -> new InvalidValueException("Exactly one TrustedParty expected, got " + trustedParties.size() + "!")))
        .map(List::getFirst)
        .map(TrustedPartyFactory::build)
        .map(to::withTrustedParty);
  }

}
