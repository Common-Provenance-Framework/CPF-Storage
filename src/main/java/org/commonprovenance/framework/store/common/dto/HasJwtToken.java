package org.commonprovenance.framework.store.common.dto;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import javax.xml.datatype.XMLGregorianCalendar;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.HashFunction;
import org.openprovenance.prov.vanilla.ProvUtilities;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.vavr.control.Either;

public interface HasJwtToken<T extends HasJwtToken<T>> {
  public static enum JwtPayloadItems {
    HASH_ALGORITHM("hash_alg"),
    DOCUMENT_DIGEST("doc_digest"),
    DOCUMENT_TIMESTAMP("doc_iat"),
    ORGANIZATION_ID("org_id");

    private final String label;

    JwtPayloadItems(String label) {
      this.label = label;
    }

    public String getLabel() {
      return this.label;
    }
  }

  public static enum JwtHeaderItems {
    TRUSTED_PARTY_URI("trustedPartyUri");

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

  default Either<ApplicationException, Date> getTokenTimestamp() {
    return this.parseJwt()
        .flatMap(EITHER.liftEitherChecked(
            SignedJWT::getJWTClaimsSet,
            _ -> throwable -> new InvalidValueException("Error while getting JWT claims!", throwable)))

        .map(JWTClaimsSet::getIssueTime)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("IssueTime is not specified in JWT Token payload!")));
  }

  default Either<ApplicationException, Long> getTokenTimestampAsLong() {
    return this.getTokenTimestamp()
        .map(Date::toInstant)
        .map(Instant::getEpochSecond);
  }

  default Either<ApplicationException, XMLGregorianCalendar> getTokenTimestampAsXMLGregorianCalendar() {
    return this.getTokenTimestamp()
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

  default Either<ApplicationException, HashFunction> getHashFunction() {
    return this.parseJwt()
        .flatMap(EITHER.liftEitherChecked(SignedJWT::getJWTClaimsSet))
        .flatMap(claims -> EITHER.liftEitherChecked(claims::getStringClaim).apply(JwtPayloadItems.HASH_ALGORITHM.getLabel()))
        .flatMap(EITHER.liftEitherOptional(
            HashFunction::from,
            hashFunction -> new InvalidValueException("Is not valid HashFunction: " + hashFunction)));
  }

  default Either<ApplicationException, String> getDocumentDigest() {
    return this.parseJwt()
        .flatMap(EITHER.liftEitherChecked(SignedJWT::getJWTClaimsSet))
        .flatMap(claims -> EITHER.liftEitherChecked(claims::getStringClaim).apply(JwtPayloadItems.DOCUMENT_DIGEST.getLabel()));
  }

  default Either<ApplicationException, String> getOrganizationId() {
    return this.parseJwt()
        .flatMap(EITHER.liftEitherChecked(SignedJWT::getJWTClaimsSet))
        .flatMap(claims -> EITHER.liftEitherChecked(claims::getStringClaim).apply(JwtPayloadItems.ORGANIZATION_ID.getLabel()));
  }

  default Either<ApplicationException, String> getBundleIdentifier() {
    return this.parseJwt()
        .flatMap(EITHER.liftEitherChecked(SignedJWT::getJWTClaimsSet))
        .map(JWTClaimsSet::getSubject)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Subject is not specified in JWT Token payload!")))
        .map(url -> url.replaceAll("/+$", ""))
        .map(url -> url.substring(url.lastIndexOf('/') + 1))
        .flatMap(EITHER.makeSure(Predicate.not(String::isBlank), "Bundle URL contains no identifier segment."));
  }

  default Either<ApplicationException, Map<String, Object>> getTokenGeneratorAttributes() {
    return parseJwt()
        .map(SignedJWT::getHeader)
        .flatMap(header -> EITHER.<String, List<String>, Map<String, Object>> combine(
            getTrustedPartyUri(),
            getCertChain(),
            (trustedPartyUri, certChain) -> Map.of(
                "trustedPartyUri", trustedPartyUri,
                "trustedPartyCertificate", certChain)));
  }

  default Either<ApplicationException, String> getTrustedPartyUri() {
    return parseJwt()
        .map(SignedJWT::getHeader)
        .map(header -> header.getCustomParam(JwtHeaderItems.TRUSTED_PARTY_URI.getLabel()))
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("TrustedParty uri is not specified in JWT Token header!")))
        .map(Object::toString);
  }

  default Either<ApplicationException, List<String>> getCertChain() {
    Function<String, String> base64ToPem = (String base64Der) -> {
      String body = base64Der.replaceAll("\\s+", "");
      String wrapped = body.replaceAll("(.{64})", "$1\n");
      if (!wrapped.endsWith("\n")) {
        wrapped += "\n";
      }
      return "-----BEGIN CERTIFICATE-----\n"
          + wrapped
          + "-----END CERTIFICATE-----\n";
    };

    return parseJwt()
        .map(SignedJWT::getHeader)
        .map(JWSHeader::getX509CertChain)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("TrustedParty X.509 certificate chain is not specified in JWT Token header!")))
        .map(chain -> chain.stream()
            .map(Base64::decode)
            .map(java.util.Base64.getEncoder()::encodeToString)
            .map(base64ToPem)
            .collect(Collectors.toList()));
  }

  default Either<ApplicationException, String> getTokenGeneratorIdentifier() {
    return parseJwt()
        .flatMap(EITHER.liftEitherChecked(SignedJWT::getJWTClaimsSet))
        .map(JWTClaimsSet::getIssuer)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Issuer is not specified in JWT Token payload!")));
  }

  private Either<ApplicationException, SignedJWT> parseJwt() {
    return Either.<ApplicationException, String> right(this.getJwt())
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("JWT Token can not be null!")))
        .flatMap(EITHER.makeSure(
            Predicate.not(String::isBlank),
            _ -> new InvalidValueException("JWT Token can not be blank String.")))
        .flatMap(EITHER.liftEitherChecked(
            SignedJWT::parse,
            jwt -> throwable -> new InvalidValueException("Error while parse JWT '" + jwt + "'!", throwable)));
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
