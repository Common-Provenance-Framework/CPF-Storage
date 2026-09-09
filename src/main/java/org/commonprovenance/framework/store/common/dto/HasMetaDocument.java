package org.commonprovenance.framework.store.common.dto;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.commonprovenance.framework.store.common.dto.HasJwtToken.JwtPayloadItems;
import org.commonprovenance.framework.store.common.utils.Base64Utils;
import org.commonprovenance.framework.store.common.utils.ProvDocumentUtils;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.openprovenance.prov.model.Agent;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.TypedValue;
import org.openprovenance.prov.model.interop.Formats.ProvFormat;
import org.openprovenance.prov.vanilla.LangString;

import com.nimbusds.jwt.SignedJWT;

import io.vavr.control.Either;

public interface HasMetaDocument<T extends HasMetaDocument<T>> {

  Document getDocument();

  T withDocument(Document docuemnt);

  default Either<ApplicationException, String> getGraph() {
    return Either.<ApplicationException, Document> right(getDocument())
        .flatMap(ProvDocumentUtils.serialize(ProvFormat.JSON));
  }

  default Either<ApplicationException, String> getB64Graph() {
    return getGraph()
        .flatMap(Base64Utils::encodeFromString);
  }

  default TrustedParty getTrustedParty(String defaultTPUrl) {
    Bundle bundle = (Bundle) getDocument().getStatementOrBundle().getFirst();

    return bundle.getStatement().stream()
        .filter(Agent.class::isInstance)
        .map(Agent.class::cast)
        .filter(agent -> ((QualifiedName) agent.getType().getFirst().getValue()).getLocalPart().equals("TrustedParty"))
        .map(generator -> {
          String name = generator.getId().getLocalPart();

          String cert = generator.getOther().stream()
              .filter(o -> o.getElementName().getLocalPart().equals("trustedPartyCertificate"))
              .map(this::getAttrValueString)
              .collect(Collectors.toList())
              .getFirst();

          String uri = generator.getOther().stream()
              .filter(o -> o.getElementName().getLocalPart().equals("trustedPartyUri"))
              .map(this::getAttrValueString)
              .collect(Collectors.toList())
              .getFirst();

          return new TrustedParty(name, cert, uri, defaultTPUrl.contains(uri));
        })

        .collect(Collectors.toList())
        .getFirst();
  }

  private String getAttrValueString(TypedValue value) {
    Object v = value.getValue();
    if (v instanceof LangString ls)
      return ls.getValue();

    if (v instanceof String s)
      return s;

    return ((QualifiedName) v).getLocalPart();
  }

  default String getJwtToken() {
    Bundle bundle = (Bundle) getDocument().getStatementOrBundle().getFirst();
    return bundle.getStatement().stream()
        .filter(Entity.class::isInstance)
        .map(Entity.class::cast)
        .filter(entity -> ((QualifiedName) entity.getType().getFirst().getValue()).getLocalPart().equals("Token"))
        .map(token -> {
          return token.getOther().stream()
              .filter(o -> o.getElementName().getLocalPart().equals("jwt"))
              .map(this::getAttrValueString)
              .collect(Collectors.toList())
              .getFirst();
        })
        .collect(Collectors.toList())
        .getFirst();
  }

  default Either<ApplicationException, String> getOrganizationIdentifier() {
    return Either.<ApplicationException, String> right(getJwtToken())
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("JWT Token can not be null!")))
        .flatMap(EITHER.makeSure(
            Predicate.not(String::isBlank),
            _ -> new InvalidValueException("JWT Token can not be blank String.")))
        .flatMap(EITHER.liftEitherChecked(
            SignedJWT::parse,
            jwt -> throwable -> new InvalidValueException("Error while parse JWT '" + jwt + "'!", throwable)))
        .flatMap(EITHER.liftEitherChecked(SignedJWT::getJWTClaimsSet))
        .flatMap(claims -> EITHER.liftEitherChecked(claims::getStringClaim).apply(JwtPayloadItems.ORGANIZATION_ID.getLabel()));
  }

  static <U extends HasMetaDocument<U>, F extends HasMetaDocument<F>> UnaryOperator<U> addDocument(F from) {
    return (U to) -> Optional.ofNullable(from)
        .map(F::getDocument)
        .map(to::withDocument)
        .orElse(to);
  }

  static <U extends HasMetaDocument<U>, F> UnaryOperator<U> addDocumentIfPresent(F from) {
    return (U to) -> Optional.ofNullable(from)
        .flatMap(HasMetaDocument::getValue)
        .map(to::withDocument)
        .orElse(to);
  }

  private static <T> Optional<Document> getValue(T form) {
    if (form instanceof HasMetaDocument<?> has)
      return Optional.of(has.getDocument());

    return Optional.empty();
  }
}
