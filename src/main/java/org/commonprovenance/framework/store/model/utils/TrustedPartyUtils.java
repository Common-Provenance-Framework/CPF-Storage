package org.commonprovenance.framework.store.model.utils;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.ConstraintException;
import org.commonprovenance.framework.store.model.TrustedParty;

import io.vavr.control.Either;

public final class TrustedPartyUtils {

  public static Either<ApplicationException, Void> isChecked(TrustedParty trustedParty) {
    return Either.<ApplicationException, TrustedParty> right(trustedParty)
        .flatMap(EITHER.makeSure(
            TrustedParty::getIsChecked,
            ConstraintException::new,
            tp -> "Trusted party has not been checked for its validity yet!"))
        .mapToVoid();
  }

  public static Either<ApplicationException, Void> isValid(TrustedParty trustedParty) {
    return Either.<ApplicationException, TrustedParty> right(trustedParty)
        .flatMap(EITHER.makeSure(
            TrustedParty::getIsValid,
            ConstraintException::new,
            _ -> "Trusted party has been checked, but has not been considered as vaid!"))
        .mapToVoid();
  }

  public static Either<ApplicationException, Void> validate(TrustedParty trustedParty) {
    return Either.<ApplicationException, TrustedParty> right(trustedParty)
        .flatMap(EITHER::makeSureNotNull)
        .flatMap(EITHER.flatTap(TrustedPartyUtils::isChecked))
        .flatMap(EITHER.flatTap(TrustedPartyUtils::isValid))
        .mapToVoid();
  }
}
