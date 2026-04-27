package org.commonprovenance.framework.store.model.utils;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.util.List;

import org.commonprovenance.framework.store.common.utils.ProvDocumentUtils;
import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.exceptions.factory.ApplicationExceptionFactory;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Organization;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.HasOther;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.Statement;
import org.openprovenance.prov.model.interop.Formats;

import cz.muni.fi.cpm.constants.CpmAttribute;
import cz.muni.fi.cpm.constants.CpmType;
import cz.muni.fi.cpm.model.CpmDocument;
import cz.muni.fi.cpm.model.CpmUtilities;
import cz.muni.fi.cpm.model.INode;
import io.vavr.Function1;
import io.vavr.control.Either;

public final class DocumentUtils {
  public static Organization buildOrganization(Document document) {
    return (new Organization())
        .withIdentifier(document.getOrganizationIdentifier());
  }

  public static Function1<HasOther, Either<ApplicationException, QualifiedName>> getCpmAttributeValue(CpmAttribute attribute) {
    return (HasOther hasOther) -> Either.<ApplicationException, HasOther> right(hasOther).flatMap(EITHER.<HasOther> makeSureNotNullWithMessage("Statement can not be null!"))
        .map(statement -> CpmUtilities.getCpmAttributeValue(statement, attribute))
        .flatMap(EITHER.makeSureNotNullWithMessage("Statement does not have '" + attribute.toString() + "' attribute, or its value is null!"))
        .flatMap(EITHER.makeSure(
            QualifiedName.class::isInstance,
            attribute.toString() + " value is not instance of QualifiedName!"))
        .map(QualifiedName.class::cast);
  }

  public static Function1<CpmDocument, Either<ApplicationException, String>> serialize(Formats.ProvFormat format) {
    return cpmDocument -> Either.<ApplicationException, CpmDocument> right(cpmDocument)
        .map(CpmDocument::toDocument)
        .flatMap(ProvDocumentUtils.serialize(format));
  }

  public static Boolean isValidBackwardConnector(HasOther connector) {
    return (connector instanceof Entity entity)
        && CpmUtilities.hasCpmType((Statement) entity, CpmType.BACKWARD_CONNECTOR)
        && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_BUNDLE_ID)
        && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_META_BUNDLE_ID)
        // TODO: referencedBundleSpecV
        // TODO: referencedMetaBundleSpecV
        && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_BUNDLE_HASH_VALUE)
        && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.HASH_ALG);
  }

  public static Boolean isValidSpecForwardConnector(HasOther connector) {
    return (connector instanceof Entity entity)
        && CpmUtilities.hasCpmType(entity, CpmType.SPEC_FORWARD_CONNECTOR)
        && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_BUNDLE_ID)
        && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_META_BUNDLE_ID)
        // TODO: referencedBundleSpecV
        // TODO: referencedMetaBundleSpecV
        && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.REFERENCED_BUNDLE_HASH_VALUE)
        && CpmUtilities.containsCpmAttribute(entity, CpmAttribute.HASH_ALG);
  }

  public static Boolean isValidForwardConnector(HasOther connector) {
    return (connector instanceof Entity entity)
        && CpmUtilities.hasCpmType(entity, CpmType.FORWARD_CONNECTOR)
        && entity.getOther().isEmpty();

  }

  public static Either<ApplicationException, QualifiedName> getCpmReferencedMetaBundleId(HasOther hasOther) {
    return Either.<ApplicationException, HasOther> right(hasOther)
        .flatMap(DocumentUtils.getCpmAttributeValue(CpmAttribute.REFERENCED_META_BUNDLE_ID));
  }

  public static Either<ApplicationException, QualifiedName> getCpmReferencedBundleId(HasOther hasOther) {
    return Either.<ApplicationException, HasOther> right(hasOther)
        .flatMap(DocumentUtils.getCpmAttributeValue(CpmAttribute.REFERENCED_BUNDLE_ID));
  }

  public static Either<ApplicationException, QualifiedName> getMainActivityReferenceMetaBundleId(Document document) {
    return DocumentUtils.getCpmDocument(document)
        .flatMap(DocumentUtils::getMainActivityReferenceMetaBundleId);
  }

  public static Either<ApplicationException, QualifiedName> getMainActivityReferenceMetaBundleId(CpmDocument cpmDocument) {
    return Either.<ApplicationException, CpmDocument> right(cpmDocument)
        .flatMap(EITHER.<CpmDocument> makeSureNotNullWithMessage("CpmDocument can not be null!"))
        .map(CpmDocument::getMainActivity)
        .flatMap(EITHER.makeSureNotNullWithMessage("MainActivity in CpmDocument can not be null!"))
        .map(INode::getAnyElement)
        .flatMap(DocumentUtils::getCpmReferencedMetaBundleId);
  }

  public static Either<ApplicationException, QualifiedName> getConnectorReferenceMetaBundleId(CpmDocument cpmDocument) {
    return Either.<ApplicationException, CpmDocument> right(cpmDocument)
        .flatMap(EITHER.<CpmDocument> makeSureNotNullWithMessage("CpmDocument can not be null!"))
        .map(CpmDocument::getMainActivity)
        .flatMap(EITHER.makeSureNotNullWithMessage("MainActivity in CpmDocument can not be null!"))
        .map(INode::getAnyElement)
        .flatMap(DocumentUtils::getCpmReferencedMetaBundleId);
  }

  public static Either<ApplicationException, List<Entity>> getBackwardConnectors(CpmDocument cpmDocument) {
    return Either.<ApplicationException, CpmDocument> right(cpmDocument)
        .flatMap(EITHER::makeSureNotNull)
        .map(CpmDocument::getBackwardConnectors)
        .map(EITHER.traverse(INode::getAnyElement))
        .flatMap(EITHER.traverseEither(EITHER.makeSure(
            Entity.class::isInstance,
            InvalidValueException::new,
            element -> "Invalid connector. Statement with id '" + element.getId().toString() + "' is not entity!")))
        .map(EITHER.traverse(Entity.class::cast));

  }

  public static Either<ApplicationException, Void> checkBackwardConnetorsAttrs(Document document) {
    return Either.<ApplicationException, Document> right(document)
        .flatMap(DocumentUtils::getCpmDocument)
        .flatMap(DocumentUtils::getBackwardConnectors)
        .flatMap(EITHER.traverseEither(EITHER.<Entity> makeSure(
            DocumentUtils::isValidBackwardConnector,
            InvalidValueException::new,
            element -> "Entity '" + element.getId() + "' is not valid specialized forward connector")))
        .mapToVoid();
  }

  public static Either<ApplicationException, List<Entity>> getForwardConnectors(CpmDocument cpmDocument) {
    return Either.<ApplicationException, CpmDocument> right(cpmDocument)
        .flatMap(EITHER::makeSureNotNull)
        .map(CpmDocument::getForwardConnectors)
        .map(EITHER.traverse(INode::getAnyElement))
        .flatMap(EITHER.traverseEither(EITHER.makeSure(
            Entity.class::isInstance,
            InvalidValueException::new,
            element -> "Invalid connector. Statement with id '" + element.getId().toString() + "' is not entity!")))
        .map(EITHER.traverse(Entity.class::cast));
  }

  public static Either<ApplicationException, Void> checkForwardConnetorsAttrs(Document document) {
    return Either.<ApplicationException, Document> right(document)
        .flatMap(DocumentUtils::getCpmDocument)
        .flatMap(DocumentUtils::getForwardConnectors)
        .flatMap(EITHER.traverseEither(EITHER.<Entity> makeSure(
            DocumentUtils::isValidForwardConnector,
            InvalidValueException::new,
            element -> "Entity '" + element.getId() + "' is not valid forward connector")))
        .mapToVoid();
  }

  public static Either<ApplicationException, List<Entity>> getSpecForwardConnectors(CpmDocument cpmDocument) {
    return Either.<ApplicationException, CpmDocument> right(cpmDocument)
        .flatMap(EITHER::makeSureNotNull)
        .map(CpmDocument::getSpecForwardConnectors)
        .map(EITHER.traverse(INode::getAnyElement))
        .flatMap(EITHER.traverseEither(EITHER.makeSure(
            Entity.class::isInstance,
            InvalidValueException::new,
            element -> "Invalid connector. Statement with id '" + element.getId().toString() + "' is not entity!")))
        .map(EITHER.traverse(Entity.class::cast));
  }

  public static Either<ApplicationException, Void> checkSpecForwardConnetorsAttrs(Document document) {
    return Either.<ApplicationException, Document> right(document)
        .flatMap(DocumentUtils::getCpmDocument)
        .flatMap(DocumentUtils::getSpecForwardConnectors)
        .flatMap(EITHER.traverseEither(EITHER.<Entity> makeSure(
            DocumentUtils::isValidSpecForwardConnector,
            InvalidValueException::new,
            element -> "Entity '" + element.getId() + "' is not valid specialized forward connector")))
        .mapToVoid();
  }

  public static Function1<Document, Either<ApplicationException, Void>> checkBundleId(AppConfiguration configuration) {
    return (Document document) -> Either.<ApplicationException, Document> right(document)
        .flatMap(DocumentUtils::getCpmDocument)
        .map(CpmDocument::getBundleId)
        .map(QualifiedName::getNamespaceURI)
        .flatMap(EITHER.makeSureNotNull(uri -> new InternalApplicationException("The bundle namespace uri '" + uri + "' does not resolve into known storage!!")))
        .flatMap(EITHER.makeSure(
            uri -> uri.equals(configuration.getFqdn() + "documents/"),
            InvalidValueException::new,
            uri -> "The bundle namespace uri '" + uri + "' does not resolve into known storage!!"))
        .mapToVoid();

  }

  public static Either<ApplicationException, CpmDocument> getCpmDocument(Document document) {
    return Either.<ApplicationException, Document> right(document)
        .map(Document::getCpmDocument)
        .flatMap(EITHER::liftEither)
        .mapLeft(ApplicationExceptionFactory.build(
            InvalidValueException::new,
            "CpmDocument is not present!"));
  }

  public static Either<ApplicationException, Document> setDocumentIdentifier(Document document) {
    return Either.<ApplicationException, Document> right(document)
        .flatMap(DocumentUtils::getCpmDocument)
        .map(CpmDocument::getBundleId)
        .map(QualifiedName::getLocalPart)
        .map(document::withIdentifier);
  }

}
