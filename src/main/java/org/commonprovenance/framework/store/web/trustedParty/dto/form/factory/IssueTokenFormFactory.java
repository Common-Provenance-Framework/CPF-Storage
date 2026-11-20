package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.model.GraphType;
import org.commonprovenance.framework.store.model.MetaDocument;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.IssueTokenFormDTO;

import io.vavr.control.Either;

public class IssueTokenFormFactory {
  public static IssueTokenFormDTO build(Organization organization, Document document, GraphType graphType, Optional<String> signature, Clock clock) {
    return new IssueTokenFormDTO(
        organization.getIdentifier(),
        document.getGraph(),
        document.getFormat(),
        signature.orElse(null),
        graphType,
        Instant.now(clock).toString());
  }

  public static Either<ApplicationException, IssueTokenFormDTO> buildSafe(Organization organization, String signature) {
    return IssueTokenFormFactory.buildSafe(organization, signature, Clock.systemUTC());
  }

  public static Either<ApplicationException, IssueTokenFormDTO> buildSafe(Organization organization, String signature, Clock clock) {
    return Either.<ApplicationException, Organization> right(organization)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Can not build IssueToken form, because organization is null!")))
        .flatMap(EITHER.liftEitherOptional(
            Organization::getDocument,
            _ -> new InvalidValueException("Can not build IssueToken form, because Document is empty!")))
        .map(document -> IssueTokenFormFactory.build(organization, document, GraphType.GRAPH, Optional.ofNullable(signature), clock))
        .flatMap(EITHER::validateDTO);
  }

  public static Either<ApplicationException, IssueTokenFormDTO> buildSafe(Organization organization, GraphType graphType) {
    return IssueTokenFormFactory.buildSafe(organization, graphType, Clock.systemUTC());
  }

  public static Either<ApplicationException, IssueTokenFormDTO> buildSafe(Organization organization, GraphType graphType, Clock clock) {
    return Either.<ApplicationException, Organization> right(organization)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Can not build IssueToken form, because organization is null!")))
        .flatMap(EITHER.liftEitherOptional(
            Organization::getDocument,
            _ -> new InvalidValueException("Can not build IssueToken form, because Document is empty!")))
        .map(document -> IssueTokenFormFactory.build(organization, document, graphType, Optional.empty(), clock))
        .flatMap(EITHER::validateDTO);
  }

  public static Either<ApplicationException, IssueTokenFormDTO> buildSafe(MetaDocument metaDocument) {
    return IssueTokenFormFactory.buildSafe(metaDocument, Clock.systemUTC());
  }

  public static Either<ApplicationException, IssueTokenFormDTO> buildSafe(MetaDocument metaDocument, Clock clock) {
    return Either.<ApplicationException, MetaDocument> right(metaDocument)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Can not build IssueToken form, because metaDocument is null!")))
        .flatMap(meta -> EITHER.<String, String, IssueTokenFormDTO> combine(
            meta.getOrganizationIdentifier(),
            meta.getB64Graph(),
            (organizationIdentifier, b64Graph) -> new IssueTokenFormDTO(
                organizationIdentifier,
                b64Graph,
                GraphFormat.JSON,
                null,
                GraphType.META,
                Instant.now(clock).toString())))
        .flatMap(EITHER::validateDTO);
  }
}
