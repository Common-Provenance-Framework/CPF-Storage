package org.commonprovenance.framework.store.common.dto;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.util.List;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
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

}
