package org.commonprovenance.framework.store.model.utils;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.model.Organization;

import io.vavr.control.Either;

public final class OrganizationUtils {
  public static Either<ApplicationException, Void> validateTrustedParty(Organization organization) {
    return Either.<ApplicationException, Organization> right(organization)
        .flatMap(EITHER.liftEitherOptional(Organization::getTrustedParty))
        .flatMap(EITHER.flatTap(TrustedPartyUtils::validate))
        .mapToVoid();
  }
}
