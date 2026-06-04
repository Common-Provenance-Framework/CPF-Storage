package org.commonprovenance.framework.store.common.dto;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.util.List;
import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.TrustedPartyNodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;

import io.vavr.control.Either;

public interface HasTrustedPartyNodeList<T extends HasTrustedPartyNodeList<T>> {
  List<TrustedPartyNode> getTrustedParties();

  T withTrustedParty(TrustedPartyNode trustedPartyNode);

  static <T extends HasTrustedPartyNodeList<T>, F extends HasTrustedPartyOptional<F>> Function<T, Either<ApplicationException, T>> addTrustedParty(F from) {
    return (T to) -> EITHER.makeSureNotNull(from)
        .flatMap(EITHER.liftEitherOptional(F::getTrustedParty))
        .map(TrustedPartyNodeFactory::build)
        .map(to::withTrustedParty);
  }

  static <T extends HasTrustedPartyNodeList<T>, F extends HasTrustedParty<F>> Function<T, Either<ApplicationException, T>> addTrustedParty(F from) {
    return (T to) -> EITHER.makeSureNotNull(from)
        .map(F::getTrustedParty)
        .map(TrustedPartyNodeFactory::build)
        .map(to::withTrustedParty);
  }

}
