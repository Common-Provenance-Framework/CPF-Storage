package org.commonprovenance.framework.store.common.utils;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.xml.datatype.XMLGregorianCalendar;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.openprovenance.prov.vanilla.ProvUtilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vavr.control.Either;

/**
 * Utility class for parsing JWT tokens.
 */
public class JwtUtils {
  public static enum JwtParts {
    HEADER(0),
    PAYLOAD(1),
    SIGNATURE(2);

    private final Integer index;

    JwtParts(Integer index) {
      this.index = index;
    }

    public Integer getIndex() {
      return this.index;
    }
  }

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

  private static final ObjectMapper mapper = new ObjectMapper();

  private static Either<ApplicationException, String> getJwtPart(String jwt, JwtParts part) {
    return Either.<ApplicationException, String> right(jwt)
        .flatMap(EITHER::makeSureNotNull)
        .flatMap(EITHER.makeSure(Predicate.not(String::isBlank), "JWT token can not be blank String."))
        .flatMap(EITHER.makeSure(
            token -> token.split("\\.").length >= 2, // Split JWT into parts: header.payload.signature
            "Not valid JWT token."))
        .map(token -> token.split("\\.")[part.getIndex()]);

  }

  private static Either<ApplicationException, JsonNode> toJsonNode(String jwtPart) {
    return Either.<ApplicationException, String> right(jwtPart)
        .flatMap(Base64Utils::decodeBase64UrlToString)
        .flatMap(payload -> {
          try {
            return Either.<ApplicationException, JsonNode> right(mapper.readTree(payload));
          } catch (Throwable throwable) {
            return Either.left(
                new InternalApplicationException("Can not read JWT payload: " + throwable.getMessage(), throwable));
          }
        });
  }

  private static Either<ApplicationException, JsonNode> getHeader(String jwt) {
    return JwtUtils.getJwtPart(jwt, JwtParts.HEADER)
        .flatMap(JwtUtils::toJsonNode);
  }

  private static Either<ApplicationException, JsonNode> getPaylod(String jwt) {
    return JwtUtils.getJwtPart(jwt, JwtParts.PAYLOAD)
        .flatMap(JwtUtils::toJsonNode);
  }

  private static Either<ApplicationException, String> getSignature(String jwt) {
    return JwtUtils.getJwtPart(jwt, JwtParts.PAYLOAD);
  }

  private static Function<JsonNode, Either<ApplicationException, Long>> getItemAsLong(JwtPayloadItems item) {
    return (JsonNode payload) -> Either.<ApplicationException, JsonNode> right(payload)
        .flatMap(EITHER.liftEither(p -> p.get(item.getLabel())))
        .flatMap(EITHER::makeSureNotNull)
        .flatMap(EITHER.makeSure(JsonNode::isNumber, "'" + item.getLabel() + "' value has to be numbert!"))
        .map(JsonNode::asLong);
  }

  private static Function<JsonNode, Either<ApplicationException, String>> getItemAsText(JwtHeaderItems item) {
    return (JsonNode header) -> Either.<ApplicationException, JsonNode> right(header)
        .flatMap(EITHER.liftEither(h -> h.get(item.getLabel())))
        .flatMap(EITHER::makeSureNotNull)
        .flatMap(EITHER.makeSure(JsonNode::isTextual, "'" + item.getLabel() + "' value has to be text!"))
        .map(JsonNode::asText)
        .flatMap(EITHER.makeSure(Predicate.not(String::isBlank),
            "'" + item.getLabel() + "' value can not be blank String!"));
  }

  private static Function<JsonNode, Either<ApplicationException, String>> getItemAsText(JwtPayloadItems item) {
    return (JsonNode header) -> Either.<ApplicationException, JsonNode> right(header)
        .flatMap(EITHER.liftEither(h -> h.get(item.getLabel())))
        .flatMap(EITHER::makeSureNotNull)
        .flatMap(EITHER.makeSure(JsonNode::isTextual, "'" + item.getLabel() + "' value has to be text!"))
        .map(JsonNode::asText)
        .flatMap(EITHER.makeSure(Predicate.not(String::isBlank),
            "'" + item.getLabel() + "' value can not be blank String!"));
  }

  /**
   * Extracts the tokenTimestamp from a JWT payload. JWT format: header.payload.signature The payload is base64url encoded JSON containing claims.
   *
   * @param jwt the JWT string
   * @return the tokenTimestamp as Long, or null if not found or invalid
   */
  public static Either<ApplicationException, Long> extractTokenTimestamp(String jwt) {
    return JwtUtils.getPaylod(jwt)
        .flatMap(JwtUtils.getItemAsLong(JwtPayloadItems.TOKEN_TIMESTAMP));
  }

  public static Either<ApplicationException, XMLGregorianCalendar> extractTokenCreation(String jwt) {
    return JwtUtils.extractTokenTimestamp(jwt)
        .flatMap(EITHER::makeSureNotNull)
        .flatMap(EITHER.liftEitherChecked(timestamp -> Instant.ofEpochSecond(timestamp.longValue())))
        .flatMap(EITHER.liftEitherChecked(Date::from))
        .flatMap(EITHER.liftEither(ProvUtilities::toXMLGregorianCalendar));
  }

  public static Either<ApplicationException, String> extractTokenCreationString(String jwt) {
    return JwtUtils.extractTokenCreation(jwt)
        .map(XMLGregorianCalendar::toString);
  }

  public static Either<ApplicationException, Map<String, Object>> extractTokenGeneratorAttributes(String jwt) {
    return JwtUtils.getHeader(jwt)
        .flatMap((JsonNode header) -> EITHER.<String, String, Map<String, Object>> combine(
            Either.<ApplicationException, JsonNode> right(header)
                .flatMap(JwtUtils.getItemAsText(JwtHeaderItems.TRUSTED_PARTY_URI)),
            Either.<ApplicationException, JsonNode> right(header)
                .flatMap(JwtUtils.getItemAsText(JwtHeaderItems.TRUSTED_PARTY_CERTIFICATE)),
            (uri, cert) -> Map.<String, Object> of(
                JwtHeaderItems.TRUSTED_PARTY_URI.getLabel(), uri,
                JwtHeaderItems.TRUSTED_PARTY_CERTIFICATE.getLabel(), cert)));
  }

  public static Either<ApplicationException, String> extractTokenGeneratorIdentifier(String jwt) {
    return JwtUtils.getPaylod(jwt)
        .flatMap(JwtUtils.getItemAsText(JwtPayloadItems.AUTHORITY_ID));
  }

  public static Either<ApplicationException, String> extractBundleIdentifier(String jwt) {
    return JwtUtils.getHeader(jwt)
        .flatMap(JwtUtils.getItemAsText(JwtPayloadItems.BUNDLE))
        .map(url -> url.replaceAll("/+$", ""))
        .map(url -> url.substring(url.lastIndexOf('/') + 1))
        .flatMap(EITHER.makeSure(Predicate.not(String::isBlank), "Bundle URL contains no identifier segment."));
  }
}
