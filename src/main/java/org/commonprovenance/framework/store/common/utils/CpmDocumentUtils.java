package org.commonprovenance.framework.store.common.utils;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.openprovenance.prov.model.HasOther;
import org.openprovenance.prov.model.QualifiedName;

import cz.muni.fi.cpm.constants.CpmAttribute;
import cz.muni.fi.cpm.model.CpmDocument;
import cz.muni.fi.cpm.model.CpmUtilities;
import cz.muni.fi.cpm.model.INode;
import io.vavr.control.Either;
import reactor.core.publisher.Mono;

public interface CpmDocumentUtils {
  CpmDocumentFunctionalUtils FUNCTIONAL = new CpmDocumentFunctionalUtils();
  CpmDocumentImperativeUtils IMPERATIVE = new CpmDocumentImperativeUtils();
  CpmDocumentReactiveUtils REACTIVE = new CpmDocumentReactiveUtils();

  class CpmDocumentFunctionalUtils {
    public Either<ApplicationException, QualifiedName> getCpmReferencedMetaBundleId(HasOther hasOther) {
      return EITHER.<HasOther>makeSureNotNullWithMessage("Statement can not be null!")
          .apply(hasOther)
          .map(statement -> CpmUtilities.getCpmAttributeValue(statement, CpmAttribute.REFERENCED_META_BUNDLE_ID))
          .flatMap(EITHER.makeSureNotNullWithMessage(
              "Statement does not have 'referencedMetaBundleId' attribute, or its value is null!"))
          .flatMap(EITHER.makeSure(
              QualifiedName.class::isInstance,
              "referencedMetaBundleId value is not instance of QualifiedName!"))
          .map(QualifiedName.class::cast);
    }

    public Either<ApplicationException, QualifiedName> getMainActivityReferenceMetaBundleId(
        CpmDocument cpmDocument) {
      return EITHER.<CpmDocument>makeSureNotNullWithMessage("CpmDocument can not be null!")
          .apply(cpmDocument)
          .map(CpmDocument::getMainActivity)
          .flatMap(EITHER.makeSureNotNullWithMessage("MainActivity in CpmDocument can not be null!"))
          .map(INode::getAnyElement)
          .flatMap(this::getCpmReferencedMetaBundleId);
    }

  }

  // ---

  class CpmDocumentReactiveUtils {
    public Mono<QualifiedName> getCpmReferencedMetaBundleId(HasOther hasOther) {
      return FUNCTIONAL.getCpmReferencedMetaBundleId(hasOther)
          .fold(Mono::error, Mono::justOrEmpty);
    }

    public Mono<QualifiedName> getMainActivityReferenceMetaBundleId(CpmDocument cpmDocument) {
      return FUNCTIONAL.getMainActivityReferenceMetaBundleId(cpmDocument)
          .fold(Mono::error, Mono::justOrEmpty);
    }

  }

  // ---

  class CpmDocumentImperativeUtils {
    public QualifiedName getCpmReferencedMetaBundleId(HasOther hasOther) throws ApplicationException {
      return FUNCTIONAL.getCpmReferencedMetaBundleId(hasOther)
          .getOrElseThrow(Function.identity());
    }

    public QualifiedName getMainActivityReferenceMetaBundleId(CpmDocument cpmDocument) throws ApplicationException {
      return FUNCTIONAL.getMainActivityReferenceMetaBundleId(cpmDocument)
          .getOrElseThrow(Function.identity());
    }

  }

}
