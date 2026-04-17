package org.commonprovenance.framework.store.common.utils;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.util.function.Function;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.openprovenance.prov.model.HasOther;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.interop.Formats;

import cz.muni.fi.cpm.constants.CpmAttribute;
import cz.muni.fi.cpm.model.CpmDocument;
import cz.muni.fi.cpm.model.CpmUtilities;
import cz.muni.fi.cpm.model.INode;
import io.vavr.control.Either;
import reactor.core.publisher.Mono;

public interface CpmDocumentUtils {
  CpmDocumentFunctionalUtils FUNCTIONAL = new CpmDocumentFunctionalUtils();
  CpmDocumentReactiveUtils REACTIVE = new CpmDocumentReactiveUtils();
  CpmDocumentImperativeUtils IMPERATIVE = new CpmDocumentImperativeUtils();

  class CpmDocumentFunctionalUtils {
    private Function<HasOther, Either<ApplicationException, QualifiedName>> getCpmAttributeValue(
        CpmAttribute attribute) {
      return (HasOther hasOther) -> Either.<ApplicationException, HasOther>right(hasOther)
          .flatMap(EITHER.<HasOther>makeSureNotNullWithMessage("Statement can not be null!"))
          .map(statement -> CpmUtilities.getCpmAttributeValue(statement, attribute))
          .flatMap(EITHER.makeSureNotNullWithMessage(
              "Statement does not have '" + attribute.toString() + "' attribute, or its value is null!"))
          .flatMap(EITHER.makeSure(
              QualifiedName.class::isInstance,
              attribute.toString() + " value is not instance of QualifiedName!"))
          .map(QualifiedName.class::cast);
    }

    public Function<CpmDocument, Either<ApplicationException, String>> serialize(Formats.ProvFormat format) {
      return cpmDocument -> Either.<ApplicationException, CpmDocument>right(cpmDocument)
          .map(CpmDocument::toDocument)
          .flatMap(ProvDocumentUtils.FUNCTIONAL.serialize(format));
    }

    public Either<ApplicationException, QualifiedName> getCpmReferencedMetaBundleId(HasOther hasOther) {
      return Either.<ApplicationException, HasOther>right(hasOther)
          .flatMap(this.getCpmAttributeValue(CpmAttribute.REFERENCED_META_BUNDLE_ID));
    }

    public Either<ApplicationException, QualifiedName> getCpmReferencedBundleId(HasOther hasOther) {
      return Either.<ApplicationException, HasOther>right(hasOther)
          .flatMap(this.getCpmAttributeValue(CpmAttribute.REFERENCED_BUNDLE_ID));
    }

    public Either<ApplicationException, QualifiedName> getMainActivityReferenceMetaBundleId(CpmDocument cpmDocument) {
      return Either.<ApplicationException, CpmDocument>right(cpmDocument)
          .flatMap(EITHER.<CpmDocument>makeSureNotNullWithMessage("CpmDocument can not be null!"))
          .map(CpmDocument::getMainActivity)
          .flatMap(EITHER.makeSureNotNullWithMessage("MainActivity in CpmDocument can not be null!"))
          .map(INode::getAnyElement)
          .flatMap(this::getCpmReferencedMetaBundleId);
    }

  }

  // ---

  class CpmDocumentReactiveUtils {

    public Function<CpmDocument, Mono<String>> serialize(Formats.ProvFormat format) {
      return (CpmDocument cpmDocument) -> Either.<ApplicationException, CpmDocument>right(cpmDocument)
          .flatMap(FUNCTIONAL.serialize(format))
          .fold(Mono::error, Mono::justOrEmpty);
    }

    public Mono<QualifiedName> getCpmReferencedMetaBundleId(HasOther hasOther) {
      return FUNCTIONAL.getCpmReferencedMetaBundleId(hasOther)
          .fold(Mono::error, Mono::justOrEmpty);
    }

    public Mono<QualifiedName> getCpmReferencedBundleId(HasOther hasOther) {
      return FUNCTIONAL.getCpmReferencedBundleId(hasOther)
          .fold(Mono::error, Mono::justOrEmpty);
    }

    public Mono<QualifiedName> getMainActivityReferenceMetaBundleId(CpmDocument cpmDocument) {
      return FUNCTIONAL.getMainActivityReferenceMetaBundleId(cpmDocument)
          .fold(Mono::error, Mono::justOrEmpty);
    }

  }

  // ---

  class CpmDocumentImperativeUtils {
    public Function<CpmDocument, String> serialize(Formats.ProvFormat format) throws ApplicationException {
      return (CpmDocument cpmDocument) -> Either.<ApplicationException, CpmDocument>right(cpmDocument)
          .flatMap(FUNCTIONAL.serialize(format))
          .getOrElseThrow(Function.identity());
    }

    public QualifiedName getCpmReferencedMetaBundleId(HasOther hasOther) throws ApplicationException {
      return FUNCTIONAL.getCpmReferencedMetaBundleId(hasOther)
          .getOrElseThrow(Function.identity());
    }

    public QualifiedName getCpmReferencedBundleId(HasOther hasOther) throws ApplicationException {
      return FUNCTIONAL.getCpmReferencedBundleId(hasOther)
          .getOrElseThrow(Function.identity());
    }

    public QualifiedName getMainActivityReferenceMetaBundleId(CpmDocument cpmDocument) throws ApplicationException {
      return FUNCTIONAL.getMainActivityReferenceMetaBundleId(cpmDocument)
          .getOrElseThrow(Function.identity());
    }

  }

}
