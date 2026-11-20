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

/**
 * Creates {@link IssueTokenFormDTO} request data from {@link Organization} instances,
 * {@link Document documents}, and {@link MetaDocument metadata documents}.
 */
public class IssueTokenFormFactory {
  /**
   * Builds token-issuance request data for a document.
   *
   * @param organization {@link Organization} associated with the document
   * @param document {@link Document} whose graph is submitted
   * @param graphType {@link GraphType} to submit
   * @param signature optional signature; absent signatures are represented as {@code null}
   * @param clock {@link Clock} used to obtain the request creation time
   * @return the token-issuance {@link IssueTokenFormDTO}
   */
  public static IssueTokenFormDTO build(Organization organization, Document document, GraphType graphType, Optional<String> signature, Clock clock) {
    return new IssueTokenFormDTO(
        organization.getIdentifier(),
        document.getGraph(),
        document.getFormat(),
        signature.orElse(null),
        graphType,
        Instant.now(clock).toString());
  }

  /**
   * Builds and validates a graph token request using the system UTC clock.
   *
   * @param organization {@link Organization} whose document graph is submitted
   * @param signature optional signature; may be {@code null}
   * @return a right containing the validated {@link IssueTokenFormDTO}, or a left containing the
   *     {@link ApplicationException}
   */
  public static Either<ApplicationException, IssueTokenFormDTO> buildSafe(Organization organization, String signature) {
    return IssueTokenFormFactory.buildSafe(organization, signature, Clock.systemUTC());
  }

  /**
   * Builds and validates a graph token request using the supplied clock.
   *
   * @param organization {@link Organization} whose document graph is submitted
   * @param signature optional signature; may be {@code null}
   * @param clock {@link Clock} used to obtain the request creation time
   * @return a right containing the validated {@link IssueTokenFormDTO}, or a left containing the
   *     {@link ApplicationException}
   */
  public static Either<ApplicationException, IssueTokenFormDTO> buildSafe(Organization organization, String signature, Clock clock) {
    return Either.<ApplicationException, Organization> right(organization)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Can not build IssueToken form, because organization is null!")))
        .flatMap(EITHER.liftEitherOptional(
            Organization::getDocument,
            _ -> new InvalidValueException("Can not build IssueToken form, because Document is empty!")))
        .map(document -> IssueTokenFormFactory.build(organization, document, GraphType.GRAPH, Optional.ofNullable(signature), clock))
        .flatMap(EITHER::validateDTO);
  }

  /**
   * Builds and validates a token request for the organization's document and the specified graph
   * type, using the system UTC clock.
   *
   * @param organization {@link Organization} whose document graph is submitted
   * @param graphType {@link GraphType} to submit
   * @return a right containing the validated {@link IssueTokenFormDTO}, or a left containing the
   *     {@link ApplicationException}
   */
  public static Either<ApplicationException, IssueTokenFormDTO> buildSafe(Organization organization, GraphType graphType) {
    return IssueTokenFormFactory.buildSafe(organization, graphType, Clock.systemUTC());
  }

  /**
   * Builds and validates a token request for the organization's document and the specified graph
   * type, using the supplied clock.
   *
   * @param organization {@link Organization} whose document graph is submitted
   * @param graphType {@link GraphType} to submit
   * @param clock {@link Clock} used to obtain the request creation time
   * @return a right containing the validated {@link IssueTokenFormDTO}, or a left containing the
   *     {@link ApplicationException}
   */
  public static Either<ApplicationException, IssueTokenFormDTO> buildSafe(Organization organization, GraphType graphType, Clock clock) {
    return Either.<ApplicationException, Organization> right(organization)
        .flatMap(EITHER.makeSureNotNull(_ -> new InvalidValueException("Can not build IssueToken form, because organization is null!")))
        .flatMap(EITHER.liftEitherOptional(
            Organization::getDocument,
            _ -> new InvalidValueException("Can not build IssueToken form, because Document is empty!")))
        .map(document -> IssueTokenFormFactory.build(organization, document, graphType, Optional.empty(), clock))
        .flatMap(EITHER::validateDTO);
  }

  /**
   * Builds and validates a metadata token request using the system UTC clock.
   *
   * @param metaDocument {@link MetaDocument} whose graph and organization identifier are submitted
   * @return a right containing the validated {@link IssueTokenFormDTO}, or a left containing the
   *     {@link ApplicationException}
   */
  public static Either<ApplicationException, IssueTokenFormDTO> buildSafe(MetaDocument metaDocument) {
    return IssueTokenFormFactory.buildSafe(metaDocument, Clock.systemUTC());
  }

  /**
   * Builds and validates a metadata token request using the supplied clock.
   *
   * @param metaDocument {@link MetaDocument} whose graph and organization identifier are submitted
   * @param clock {@link Clock} used to obtain the request creation time
   * @return a right containing the validated {@link IssueTokenFormDTO}, or a left containing the
   *     {@link ApplicationException}
   */
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
