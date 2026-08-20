package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import java.time.LocalDateTime;

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
  public static IssueTokenFormDTO build(Organization organization, Document document, GraphType graphType, String signature) {
    return new IssueTokenFormDTO(
        organization.getIdentifier(),
        document.getGraph(),
        document.getFormat(),
        signature,
        graphType,
        LocalDateTime.now().toString());
  }

  public static Either<ApplicationException, IssueTokenFormDTO> build(MetaDocument metaDocument) {
    return EITHER.<String, String, IssueTokenFormDTO> combine(
        metaDocument.getOrganizationIdentifier(),
        metaDocument.getB64Graph(),
        (organizationIdentifier, b64Graph) -> new IssueTokenFormDTO(
            organizationIdentifier,
            b64Graph,
            GraphFormat.JSON,
            null,
            GraphType.META,
            LocalDateTime.now().toString()));
  }

  public static Either<ApplicationException, IssueTokenFormDTO> buildSafe(Organization organization, String signature) {
    return Either.<ApplicationException, Organization> right(organization)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Can not build IssueToken form, because organization is null!")))
        .flatMap(EITHER.liftEitherOptional(
            Organization::getDocument,
            _ -> new InvalidValueException("Can not build IssueToken form, because Document is empty!")))
        .map(document -> IssueTokenFormFactory.build(organization, document, GraphType.GRAPH, signature))
        .flatMap(EITHER::validateDTO);
  }

  public static Either<ApplicationException, IssueTokenFormDTO> buildSafe(Organization organization, GraphType graphType) {
    return Either.<ApplicationException, Organization> right(organization)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Can not build IssueToken form, because organization is null!")))
        .flatMap(EITHER.liftEitherOptional(
            Organization::getDocument,
            _ -> new InvalidValueException("Can not build IssueToken form, because Document is empty!")))
        .map(document -> IssueTokenFormFactory.build(organization, document, graphType, null))
        .flatMap(EITHER::validateDTO);
  }

  public static Either<ApplicationException, IssueTokenFormDTO> buildSafe(MetaDocument metaDocument) {
    return Either.<ApplicationException, MetaDocument> right(metaDocument)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Can not build IssueToken form, because metaDocument is null!")))
        .flatMap(IssueTokenFormFactory::build)
        .flatMap(EITHER::validateDTO);
  }
}
