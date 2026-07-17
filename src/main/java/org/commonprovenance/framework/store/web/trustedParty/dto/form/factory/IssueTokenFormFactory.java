package org.commonprovenance.framework.store.web.trustedParty.dto.form.factory;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.GraphType;
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
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
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
}
