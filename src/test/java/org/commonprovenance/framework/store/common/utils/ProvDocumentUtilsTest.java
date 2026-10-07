package org.commonprovenance.framework.store.common.utils;

import static org.commonprovenance.framework.store.common.composition.Reactor.MONO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.List;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.HashFunction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Element;
import org.openprovenance.prov.model.HasOther;
import org.openprovenance.prov.model.Other;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.vanilla.ProvFactory;

import cz.muni.fi.cpm.constants.CpmAttribute;
import cz.muni.fi.cpm.constants.CpmNamespaceConstants;
import cz.muni.fi.cpm.model.CpmDocument;
import cz.muni.fi.cpm.model.INode;
import io.vavr.control.Either;
import reactor.test.StepVerifier;

@DisplayName("CPM Document Utils Test")
class ProvDocumentUtilsTest {

  private static final String ERR_STATEMENT_NULL = "Statement can not be null!";
  private static final String ERR_REFERENCED_META_MISSING = "Statement does not have 'referencedMetaBundleId' attribute, or its value is null!";
  private static final String ERR_REFERENCED_META_WRONG_TYPE = "referencedMetaBundleId value is not instance of QualifiedName!";
  private static final String ERR_HASH_ALG_WRONG_VALUE = "Unknown hash function: '...'";
  private static final String ERR_CPM_DOCUMENT_NULL = "CpmDocument can not be null!";
  private static final String ERR_MAIN_ACTIVITY_NULL = "MainActivity in CpmDocument can not be null!";

  private final ProvFactory provFactory = new ProvFactory();

  private QualifiedName cpmAttributeName(CpmAttribute cpmAttribute) {
    return provFactory.newQualifiedName(CpmNamespaceConstants.CPM_NS, cpmAttribute.toString(), CpmNamespaceConstants.CPM_PREFIX);
  }

  private Element elementWithReferencedMetaBundleId(Object value, QualifiedName type) {
    Element element = mock(Element.class, withSettings().extraInterfaces(HasOther.class));

    Other referencedMetaBundleId = provFactory.newOther(
        cpmAttributeName(CpmAttribute.REFERENCED_META_BUNDLE_ID),
        value,
        type);
    when(element.getOther()).thenReturn(List.of(referencedMetaBundleId));
    return element;
  }

  private Element elementWithReferencedBundleId(Object value, QualifiedName type) {
    Element element = mock(Element.class, withSettings().extraInterfaces(HasOther.class));

    Other referencedBundleId = provFactory.newOther(
        cpmAttributeName(CpmAttribute.REFERENCED_BUNDLE_ID),
        value,
        type);
    when(element.getOther()).thenReturn(List.of(referencedBundleId));
    return element;
  }

  private Element elementWithHashAlg(String value) {
    Element element = mock(Element.class, withSettings().extraInterfaces(HasOther.class));

    Other hashAlg = provFactory.newOther(
        cpmAttributeName(CpmAttribute.HASH_ALG),
        value,
        provFactory.getName().XSD_STRING);
    when(element.getOther()).thenReturn(List.of(hashAlg));
    return element;
  }

  private Element elementWithHashValue(String value) {
    Element element = mock(Element.class, withSettings().extraInterfaces(HasOther.class));

    Other hashValue = provFactory.newOther(
        cpmAttributeName(CpmAttribute.REFERENCED_BUNDLE_HASH_VALUE),
        value,
        provFactory.getName().XSD_STRING);
    when(element.getOther()).thenReturn(List.of(hashValue));
    return element;
  }

  private Element elementWithEmptyAttributes() {
    Element element = mock(Element.class, withSettings().extraInterfaces(HasOther.class));

    when(element.getOther()).thenReturn(List.of());
    return element;
  }

  private <R> void assertLeft(
      String expectedMessage,
      Either<ApplicationException, R> result) {

    assertFalse(result.isRight(), "Right side has not been expected!");
    assertTrue(result.isLeft(), "Left side has been expected!");
    Throwable exception = result.getLeft();
    assertNotNull(exception, "Left side can not be a NULL!");
    assertInstanceOf(ApplicationException.class, exception, "Exception has to be instance of ApplicationException!");
    assertEquals(expectedMessage, exception.getMessage(), "Exact error message has been expected!");
  }

  private <R> void assertRight(
      R expected,
      Either<ApplicationException, R> result) {

    assertFalse(result.isLeft(), "Left side has not been expected!");
    assertTrue(result.isRight(), "Right side has been expected!");
    R value = result.get();
    assertNotNull(value, "Right side can not be a NULL!");
    assertSame(expected, value, "Exact value in Right side has been expected!");
  }

  @Test
  @DisplayName("Functional getCpmReferencedMetaBundleId should return Either with exact Left side for null statement")
  void requireCpmReferencedMetaBundleId_shouldFailForNullStatement() {
    assertLeft(
        ERR_STATEMENT_NULL,
        ProvDocumentUtils.getCpmReferencedMetaBundleId(null));
  }

  @Test
  @DisplayName("Functional getCpmReferencedMetaBundleId should return Either with exact Left side when attribute is missing")
  void requireCpmReferencedMetaBundleId_shouldFailWhenAttributeMissing() {
    Element connector = elementWithEmptyAttributes();
    assertLeft(
        ERR_REFERENCED_META_MISSING,
        ProvDocumentUtils.getCpmReferencedMetaBundleId(connector));
  }

  @Test
  @DisplayName("Functional getCpmReferencedMetaBundleId should return Either with exact Left side when attribute has wrong type")
  void requireCpmReferencedMetaBundleId_shouldFailWhenAttributeHasWrongType() {
    Element connector = elementWithReferencedMetaBundleId(
        "not-a-qualified-name",
        provFactory.getName().XSD_STRING);

    assertLeft(ERR_REFERENCED_META_WRONG_TYPE,
        ProvDocumentUtils.getCpmReferencedMetaBundleId(connector));
  }

  @Test
  @DisplayName("Functional getCpmReferencedMetaBundleId should return Either with qualified name in Right side when attribute is valid")
  void requireCpmReferencedMetaBundleId_shouldReturnQualifiedNameWhenAttributeIsValid() {
    QualifiedName expectedReference = provFactory.newQualifiedName(
        "https://example.org/bundles/",
        "meta-bundle-1",
        "ex");

    Element connector = elementWithReferencedMetaBundleId(
        expectedReference,
        provFactory.getName().PROV_QUALIFIED_NAME);

    assertRight(expectedReference, ProvDocumentUtils.getCpmReferencedMetaBundleId(connector));
  }

  @Test
  @DisplayName("Functional getCpmReferencedBundleId should return Either with qualified name in Right side when attribute is valid")
  void requireCpmReferencedBundleId_shouldReturnQualifiedNameWhenAttributeIsValid() {
    QualifiedName expectedReference = provFactory.newQualifiedName(
        "https://example.org/bundles/",
        "bundle-1",
        "ex");

    Element connector = elementWithReferencedBundleId(
        expectedReference,
        provFactory.getName().PROV_QUALIFIED_NAME);

    assertRight(expectedReference, ProvDocumentUtils.getCpmReferencedBundleId(connector));
  }

  @Test
  @DisplayName("Functional getCpmHashAlg should return Either  with exact Left side when attribute has wrong value")
  void requireCpmHashAlg_shouldFailWhenAttributeHasWrongHashAlgValue() {
    Element connector = elementWithHashAlg("...");

    assertLeft(ERR_HASH_ALG_WRONG_VALUE, ProvDocumentUtils.getCpmHashAlg(connector));
  }

  @Test
  @DisplayName("Functional getCpmHashAlg should return Either with HashFunction in Right side when is valid")
  void requireCpmHashAlg_shouldReturnHashFunctionWhenValidHashAlg() {
    Element connector = elementWithHashAlg(HashFunction.SHA256.toString());

    assertRight(HashFunction.SHA256, ProvDocumentUtils.getCpmHashAlg(connector));
  }

  @Test
  @DisplayName("Functional getCpmHashAlgAsString should return Either with HashFunction as String in Right side when is valid")
  void requireCpmHashAlgAsString_shouldReturnStringWhenValidHashAlg() {
    Element connector = elementWithHashAlg(HashFunction.SHA256.toString());

    assertRight(HashFunction.SHA256.toString(), ProvDocumentUtils.getCpmHashAlgAsString(connector));
  }

  @Test
  @DisplayName("Functional getCpmReferencedBundleHashValue should return Either with hash (String) in Right side when is valid")
  void requireCpmReferencedBundleHashValue_shouldReturnHashWhenValidHashAlg() {
    String expected = "1cd742f82503ec53a8d884fda2aeafd5c708878ea5d43cf052273055cc55b4cb";
    Element connector = elementWithHashValue(expected);

    assertRight(expected, ProvDocumentUtils.getCpmReferencedBundleHashValue(connector));
  }

  @Test
  @DisplayName("Functional getMainActivityReferencedMetaBundleId should return Either with exact Left side when main activity is null")
  void requireMainActivityReferenceMetaBundleId_shouldFailWhenMainActivityIsNull() {
    CpmDocument cpmDocument = mock(CpmDocument.class);
    when(cpmDocument.getMainActivity()).thenReturn(null);

    Document document = mock(Document.class);
    when(document.getCpmDocument()).thenReturn(Either.right(cpmDocument));
    when(document.getMainActivity()).thenCallRealMethod();

    assertLeft(
        ERR_MAIN_ACTIVITY_NULL,
        document.getMainActivity()
            .flatMap(ProvDocumentUtils::getCpmReferencedMetaBundleId));
  }

  @Test
  @DisplayName("Functional getMainActivityReferencedMetaBundleId should return Either with referenced id from main activity in Right side")
  void requireMainActivityReferenceMetaBundleId_shouldReturnReferencedIdFromMainActivity() {
    QualifiedName expectedReference = provFactory.newQualifiedName(
        "https://example.org/bundles/",
        "meta-bundle-2",
        "ex");

    Activity activityElement = mock(Activity.class, withSettings().extraInterfaces(HasOther.class));

    Other referencedMetaBundleId = provFactory.newOther(
        cpmAttributeName(CpmAttribute.REFERENCED_META_BUNDLE_ID),
        expectedReference,
        provFactory.getName().PROV_QUALIFIED_NAME);
    when(activityElement.getOther()).thenReturn(List.of(referencedMetaBundleId));

    INode mainActivity = mock(INode.class);
    when(mainActivity.getAnyElement()).thenReturn(activityElement);

    CpmDocument cpmDocument = mock(CpmDocument.class);
    when(cpmDocument.getMainActivity()).thenReturn(mainActivity);

    Document document = mock(Document.class);
    when(document.getCpmDocument()).thenReturn(Either.right(cpmDocument));
    when(document.getMainActivity()).thenCallRealMethod();

    assertRight(
        expectedReference,
        document.getMainActivity()
            .flatMap(ProvDocumentUtils::getCpmReferencedMetaBundleId));
  }

  @Test
  @DisplayName("Reactive getCpmReferencedMetaBundleId should propagate synchronous Either Left side value to reactive error channel")

  void getCpmReferencedMetaBundleId_shouldPropagateErrorToReactiveChannel() {
    StepVerifier.create(MONO.fromEither(ProvDocumentUtils.getCpmReferencedMetaBundleId(null)))
        .expectErrorSatisfies((Throwable error) -> {
          assertInstanceOf(InternalApplicationException.class, error);
          assertEquals(ERR_STATEMENT_NULL, error.getMessage());
        })
        .verify();
  }

  @Test
  @DisplayName("Reactive getMainActivityReferencedMetaBundleId should emit referenced value from Either Right side")
  void getMainActivityReferenceMetaBundleId_shouldEmitReferencedId() {
    QualifiedName expectedReference = provFactory.newQualifiedName(
        "https://example.org/bundles/",
        "meta-bundle-3",
        "ex");
    Activity activityElement = mock(Activity.class, withSettings().extraInterfaces(HasOther.class));
    HasOther hasOther = (HasOther) activityElement;

    Other referencedMetaBundleId = provFactory.newOther(
        cpmAttributeName(CpmAttribute.REFERENCED_META_BUNDLE_ID),
        expectedReference,
        provFactory.getName().PROV_QUALIFIED_NAME);
    when(hasOther.getOther()).thenReturn(List.of(referencedMetaBundleId));

    INode mainActivity = mock(INode.class);
    when(mainActivity.getAnyElement()).thenReturn(activityElement);

    CpmDocument cpmDocument = mock(CpmDocument.class);
    when(cpmDocument.getMainActivity()).thenReturn(mainActivity);

    Document document = mock(Document.class);
    when(document.getCpmDocument()).thenReturn(Either.right(cpmDocument));
    when(document.getMainActivity()).thenCallRealMethod();

    StepVerifier.create(MONO.fromEither(document.getMainActivity()
        .flatMap(ProvDocumentUtils::getCpmReferencedMetaBundleId)))
        .expectNext(expectedReference)
        .verifyComplete();
  }
}
