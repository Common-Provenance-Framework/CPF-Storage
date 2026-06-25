package org.commonprovenance.framework.store.common.dto;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import java.util.List;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.QualifiedName;

import cz.muni.fi.cpm.model.CpmDocument;
import cz.muni.fi.cpm.model.INode;
import io.vavr.control.Either;

public interface HasCpmDocument<T extends HasCpmDocument<T>> {
  Either<ApplicationException, CpmDocument> getCpmDocument();

  default Either<ApplicationException, String> getIdentifier() {
    return getCpmDocument()
        .map(CpmDocument::getBundleId)
        .map(QualifiedName::getLocalPart);
  }

  default Either<ApplicationException, Document> getProvDocument() {
    return getCpmDocument()
        .map(CpmDocument::toDocument);
  }

  default Either<ApplicationException, List<Entity>> getSpecForwardConnectors() {
    return getCpmDocument()
        .map(CpmDocument::getSpecForwardConnectors)
        .map(EITHER.traverse(INode::getAnyElement))
        .flatMap(EITHER.traverseEither(EITHER.makeSure(
            Entity.class::isInstance,
            InvalidValueException::new,
            element -> "Invalid connector. Statement with id '" + element.getId().toString() + "' is not entity!")))
        .map(EITHER.traverse(Entity.class::cast));
  }

  default Either<ApplicationException, List<Entity>> getForwardConnectors() {
    return getCpmDocument()
        .map(CpmDocument::getForwardConnectors)
        .map(EITHER.traverse(INode::getAnyElement))
        .flatMap(EITHER.traverseEither(EITHER.makeSure(
            Entity.class::isInstance,
            InvalidValueException::new,
            element -> "Invalid connector. Statement with id '" + element.getId().toString() + "' is not entity!")))
        .map(EITHER.traverse(Entity.class::cast));
  }

  default Either<ApplicationException, List<Entity>> getBackwardConnectors() {
    return getCpmDocument()
        .map(CpmDocument::getBackwardConnectors)
        .map(EITHER.traverse(INode::getAnyElement))
        .flatMap(EITHER.traverseEither(EITHER.makeSure(
            Entity.class::isInstance,
            InvalidValueException::new,
            element -> "Invalid connector. Statement with id '" + element.getId().toString() + "' is not entity!")))
        .map(EITHER.traverse(Entity.class::cast));
  }

  default Either<ApplicationException, Activity> getMainActivity() {
    return getCpmDocument()
        .map(CpmDocument::getMainActivity)
        .flatMap(EITHER.makeSureNotNullWithMessage("MainActivity in CpmDocument can not be null!"))
        .map(INode::getAnyElement)
        .flatMap(EITHER.makeSure(
            Activity.class::isInstance,
            InvalidValueException::new,
            element -> "Invalid element. Statement with id '" + element.getId().toString() + "' is not activity!"))
        .map(Activity.class::cast);
  }

}
