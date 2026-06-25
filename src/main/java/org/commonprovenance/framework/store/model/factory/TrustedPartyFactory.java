package org.commonprovenance.framework.store.model.factory;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.commonprovenance.framework.store.common.composition.Monoid;
import org.commonprovenance.framework.store.common.dto.HasClientCertificate;
import org.commonprovenance.framework.store.common.dto.HasIsChecked;
import org.commonprovenance.framework.store.common.dto.HasIsDefault;
import org.commonprovenance.framework.store.common.dto.HasIsValid;
import org.commonprovenance.framework.store.common.dto.HasName;
import org.commonprovenance.framework.store.common.dto.HasUrl;
import org.commonprovenance.framework.store.common.dto.HasUrlOptional;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;

import io.vavr.control.Either;

public class TrustedPartyFactory {
  private static <T extends HasName<T> & HasClientCertificate<T> & HasUrl<T> & HasIsChecked<T> & HasIsValid<T> & HasIsDefault<T>> UnaryOperator<TrustedParty> mapper(T data) {
    return (TrustedParty trustedParty) -> Monoid.compose(
        trustedParty,
        List.of(
            HasName.addName(data),
            HasClientCertificate.addClientCertificate(data),
            HasUrlOptional.addUrl(data),
            HasIsChecked.addIsChecked(data),
            HasIsValid.addIsValid(data),
            HasIsDefault.addIsDefault(data)));
  }

  private static <T> UnaryOperator<TrustedParty> mapper(T data) {
    return (TrustedParty trustedParty) -> Monoid.compose(
        trustedParty,
        List.of(
            HasName.addNameIfPresent(data),
            HasClientCertificate.addClientCertificateIfPresent(data),
            HasUrlOptional.addUrlIfPresent(data),
            HasIsChecked.addIsCheckedIfPresent(data),
            HasIsValid.addIsValidIfPresent(data),
            HasIsDefault.addIsDefaultIfPresent(data)));
  }

  public static TrustedParty build(TrustedPartyNode trustedPartyNode) {
    return mapper(trustedPartyNode).apply(new TrustedParty());
  }

  public static <T> Either<ApplicationException, TrustedParty> buildUnsafe(T data) {
    return Either.<ApplicationException, TrustedParty> right(new TrustedParty())
        .map(TrustedPartyFactory.mapper(data))
        .flatMap(EITHER::validateDTO);
  }

  public static <T> Function<T, Either<ApplicationException, TrustedParty>> buildUnsafe(String url, Boolean isDefault) {
    return (T value) -> buildUnsafe(value)
        .map((TrustedParty trustedParty) -> trustedParty.withUrl(url))
        .map((TrustedParty trustedParty) -> trustedParty.withIsDefault(isDefault))
        .flatMap(EITHER::validateDTO);
  }

}
