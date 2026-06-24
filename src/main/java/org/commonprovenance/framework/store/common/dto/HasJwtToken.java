package org.commonprovenance.framework.store.common.dto;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.xml.datatype.XMLGregorianCalendar;

import org.commonprovenance.framework.store.controller.dto.response.TokenResponseDTO;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.openprovenance.prov.vanilla.ProvUtilities;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.vavr.control.Either;

public interface HasJwtToken<T extends HasJwtToken<T>> {
  public static enum JwtPayloadItems {
    TOKEN_TIMESTAMP("tokenTimestamp"),
    AUTHORITY_ID("authorityId"),
    BUNDLE("bundle");

    private final String label;

    JwtPayloadItems(String label) {
      this.label = label;
    }

    public String getLabel() {
      return this.label;
    }
  }

  public static enum JwtHeaderItems {
    TRUSTED_PARTY_URI("trustedPartyUri"),
    TRUSTED_PARTY_CERTIFICATE("trustedPartyCertificate");

    private final String label;

    JwtHeaderItems(String label) {
      this.label = label;
    }

    public String getLabel() {
      return this.label;
    }
  }

  String getJwt();

  default T withJwt(String jwtToken) {
    throw new InternalApplicationException("withJwt is not supported for read-only type:" + this.getClass().getSimpleName());
  }

  default T withCreatedOn(Long createdOn) {
    throw new InternalApplicationException("withJwt is not supported for read-only type:" + this.getClass().getSimpleName());
  }

  default Either<ApplicationException, Long> getTokenTimestampAsLong() {
    return getJwtClaims()
        .flatMap(EITHER.makeSure(
            jwt -> jwt.getClaims().containsKey(JwtPayloadItems.TOKEN_TIMESTAMP.getLabel()),
            JwtPayloadItems.TOKEN_TIMESTAMP.getLabel() + ": claim is missing in JWT Token!"))
        .flatMap(EITHER.liftEitherChecked(x -> x.getLongClaim(JwtPayloadItems.TOKEN_TIMESTAMP.getLabel())));
  }

  default Either<ApplicationException, XMLGregorianCalendar> getTokenTimestampAsXMLGregorianCalendar() {
    return this.getTokenTimestampAsLong()
        .flatMap(EITHER::makeSureNotNull)
        .flatMap(EITHER.liftEitherChecked(timestamp -> Instant.ofEpochSecond(timestamp.longValue())))
        .flatMap(EITHER.liftEitherChecked(Date::from))
        .flatMap(EITHER.liftEither(ProvUtilities::toXMLGregorianCalendar));
  }

  default Either<ApplicationException, String> getTokenTimestampAsString() {
    return this.getTokenTimestampAsXMLGregorianCalendar()
        .map(XMLGregorianCalendar::toString);
  }

  default Either<ApplicationException, T> loadCreatedOn() {
    return this.getTokenTimestampAsLong()
        .map(createdOn -> this.withCreatedOn(createdOn)); // TODO: check this
  }

  default Either<ApplicationException, String> getBundleIdentifier() {
    return getJwtHeader()
        .map(header -> header.getCustomParam(JwtPayloadItems.BUNDLE.getLabel()))
        .flatMap(EITHER.makeSure(String.class::isInstance, "Bundle header claim has to be String"))
        .map(String.class::cast)
        .map(url -> url.replaceAll("/+$", ""))
        .map(url -> url.substring(url.lastIndexOf('/') + 1))
        .flatMap(EITHER.makeSure(Predicate.not(String::isBlank), "Bundle URL contains no identifier segment."));
  }

  default Either<ApplicationException, Map<String, Object>> getTokenGeneratorAttributes() {
    return getJwtHeader()
        .map(JWSHeader::getCustomParams)
        .map(Map::entrySet)
        .map(entries -> entries.stream()
            .filter(entry -> entry.getKey().equals(JwtHeaderItems.TRUSTED_PARTY_URI.getLabel())
                || entry.getKey().equals(JwtHeaderItems.TRUSTED_PARTY_CERTIFICATE.getLabel()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

  }

  default Either<ApplicationException, String> getTokenGeneratorIdentifier() {
    return getJwtClaims()
        .flatMap(EITHER.makeSure(
            jwt -> jwt.getClaims().containsKey(JwtPayloadItems.AUTHORITY_ID.getLabel()),
            JwtPayloadItems.TOKEN_TIMESTAMP.getLabel() + ": claim is missing in JWT Token!"))
        .flatMap(EITHER.liftEitherChecked(x -> x.getStringClaim(JwtPayloadItems.AUTHORITY_ID.getLabel())));

  }

  default UnaryOperator<TokenResponseDTO> putJwtToDTO() {
    return (TokenResponseDTO to) -> Optional.ofNullable(getJwt())
        .map(to::withJwt)
        .orElse(to);
  }

  private Either<ApplicationException, JWTClaimsSet> getJwtClaims() {
    return Either.<ApplicationException, String> right(this.getJwt())
        .flatMap(EITHER::makeSureNotNull)
        .flatMap(EITHER.makeSure(Predicate.not(String::isBlank), "JWT token can not be blank String."))
        .flatMap(EITHER.liftEitherChecked(SignedJWT::parse))
        .flatMap(EITHER.liftEitherChecked(SignedJWT::getJWTClaimsSet));
  }

  private Either<ApplicationException, JWSHeader> getJwtHeader() {
    return Either.<ApplicationException, String> right(this.getJwt())
        .flatMap(EITHER::makeSureNotNull)
        .flatMap(EITHER.makeSure(Predicate.not(String::isBlank), "JWT token can not be blank String."))
        .flatMap(EITHER.liftEitherChecked(SignedJWT::parse))
        .flatMap(EITHER.liftEitherChecked(SignedJWT::getHeader));
  }

  static <T extends HasJwtToken<T>, F extends HasJwtToken<F>> UnaryOperator<T> addJwt(F from) {
    return (T to) -> Optional.ofNullable(from)
        .map(F::getJwt)
        .map(to::withJwt)
        .orElse(to);
  }

  static <T extends HasJwtToken<T>, F> UnaryOperator<T> addJwtIfPresent(F from) {
    return (T to) -> Optional.ofNullable(from)
        .flatMap(HasJwtToken::getValue)
        .map(to::withJwt)
        .orElse(to);
  }

  private static <T> Optional<String> getValue(T form) {
    if (form instanceof HasJwtToken<?> has)
      return Optional.of(has.getJwt());

    if (form instanceof org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types.HasJwtToken has)
      return Optional.of(has.getJwt());

    return Optional.empty();
  }

}
