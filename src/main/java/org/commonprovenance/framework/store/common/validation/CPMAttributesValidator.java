package org.commonprovenance.framework.store.common.validation;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import java.util.function.Function;

import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.HasOther;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.Statement;

import cz.muni.fi.cpm.constants.CpmAttribute;
import cz.muni.fi.cpm.constants.CpmType;
import cz.muni.fi.cpm.model.CpmDocument;
import cz.muni.fi.cpm.model.CpmUtilities;
import io.vavr.Function1;
import io.vavr.control.Either;

public final class CPMAttributesValidator {

  private static Boolean isValidBackwardConnector(HasOther connector) {
    return (connector instanceof Entity entity)
        && CpmUtilities.hasCpmType((Statement) entity, CpmType.BACKWARD_CONNECTOR)
        && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_BUNDLE_ID)
        && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_META_BUNDLE_ID)
        // TODO: referencedBundleSpecV
        // TODO: referencedMetaBundleSpecV
        && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_BUNDLE_HASH_VALUE)
        && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.HASH_ALG);
  }

  private static Boolean isValidSpecForwardConnector(HasOther connector) {
    return (connector instanceof Entity entity)
        && CpmUtilities.hasCpmType(entity, CpmType.SPEC_FORWARD_CONNECTOR)
        && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_BUNDLE_ID)
        && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_META_BUNDLE_ID)
        // TODO: referencedBundleSpecV
        // TODO: referencedMetaBundleSpecV
        && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_BUNDLE_HASH_VALUE)
        && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.HASH_ALG);
  }

  private static Boolean isValidForwardConnector(HasOther connector) {
    return (connector instanceof Entity entity)
        && CpmUtilities.hasCpmType(entity, CpmType.FORWARD_CONNECTOR)
        && entity.getOther().isEmpty();

  }

  private static Function1<Organization, Either<ApplicationException, Void>> checkBundleId(AppConfiguration configuration) {
    return (Organization organization) -> Either.<ApplicationException, Organization> right(organization)
        .flatMap(EITHER.liftEitherOptional(Organization::getDocument))
        .flatMap(Document::getCpmDocument)
        .map(CpmDocument::getBundleId)
        .map(QualifiedName::getNamespaceURI)
        .flatMap(EITHER.makeSureNotNull(uri -> new InternalApplicationException("The bundle namespace uri '" + uri + "' does not resolve into known storage!!")))
        .flatMap(EITHER.makeSure(
            uri -> uri.equals(configuration.getFqdn() + "organizations/" + organization.getIdentifier() + "/documents/"),
            InvalidValueException::new,
            uri -> "The bundle namespace uri '" + uri + "' does not resolve into known storage!!"))
        .mapToVoid();

  }

  private static Either<ApplicationException, Void> checkBackwardConnetorsAttrs(Document document) {
    return Either.<ApplicationException, Document> right(document)
        .flatMap(Document::getBackwardConnectors)
        .flatMap(EITHER.traverseEither(EITHER.<Entity> makeSure(
            CPMAttributesValidator::isValidBackwardConnector,
            InvalidValueException::new,
            element -> "Entity '" + element.getId() + "' is not valid specialized forward connector")))
        .mapToVoid();
  }

  private static Either<ApplicationException, Void> checkForwardConnetorsAttrs(Document document) {
    return Either.<ApplicationException, Document> right(document)
        .flatMap(Document::getForwardConnectors)
        .flatMap(EITHER.traverseEither(EITHER.<Entity> makeSure(
            CPMAttributesValidator::isValidForwardConnector,
            InvalidValueException::new,
            element -> "Entity '" + element.getId() + "' is not valid forward connector")))
        .mapToVoid();
  }

  private static Either<ApplicationException, Void> checkSpecForwardConnetorsAttrs(Document document) {
    return Either.<ApplicationException, Document> right(document)
        .flatMap(Document::getSpecForwardConnectors)
        .flatMap(EITHER.traverseEither(EITHER.<Entity> makeSure(
            CPMAttributesValidator::isValidSpecForwardConnector,
            InvalidValueException::new,
            element -> "Entity '" + element.getId() + "' is not valid specialized forward connector")))
        .mapToVoid();
  }

  private static Either<ApplicationException, Void> checkDocument(Organization organization) {
    return Either.<ApplicationException, Organization> right(organization)
        .flatMap(EITHER.liftEitherOptional(
            Organization::getDocument,
            _ -> new InvalidValueException("Document has not been loaded yet!")))
        .flatMap(Document::getCpmDocument)
        .mapToVoid();
  }

  public static Function<Organization, Either<ApplicationException, Void>> validate(AppConfiguration configuration) {
    return (organization) -> Either.<ApplicationException, Organization> right(organization)
        .flatMap(EITHER.flatPeek(CPMAttributesValidator::checkDocument))
        .flatMap(EITHER.flatPeek(CPMAttributesValidator.checkBundleId(configuration)))
        .flatMap(EITHER.liftEitherOptional(Organization::getDocument))
        .flatMap(EITHER.flatPeek(CPMAttributesValidator::checkSpecForwardConnetorsAttrs))
        .flatMap(EITHER.flatPeek(CPMAttributesValidator::checkBackwardConnetorsAttrs))
        .flatMap(EITHER.flatPeek(CPMAttributesValidator::checkForwardConnetorsAttrs))
        .mapToVoid();
  }
}
