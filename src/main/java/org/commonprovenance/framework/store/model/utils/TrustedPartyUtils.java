package org.commonprovenance.framework.store.model.utils;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.ConstraintException;
import org.commonprovenance.framework.store.model.TrustedParty;

import io.vavr.control.Either;

public final class TrustedPartyUtils {

  public static Either<ApplicationException, TrustedParty> validate(TrustedParty trustedParty) {
    return Either.<ApplicationException, TrustedParty> right(trustedParty)
        .flatMap(EITHER.makeSure(
            TrustedParty::getIsChecked,
            ConstraintException::new,
            _ -> "TrustedParty is registered in Store, but has not been checked for its validity yet!"))
        .flatMap(EITHER.makeSure(
            TrustedParty::getIsValid,
            ConstraintException::new,
            _ -> "TrustedParty is registered in Store and checked, but has not been considered as vaid!"));
  }
}
