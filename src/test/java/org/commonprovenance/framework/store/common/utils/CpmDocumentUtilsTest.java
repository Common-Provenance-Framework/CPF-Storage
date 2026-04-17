package org.commonprovenance.framework.store.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.Element;
import org.openprovenance.prov.model.HasOther;
import org.openprovenance.prov.model.Other;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.vanilla.ProvFactory;

import cz.muni.fi.cpm.constants.CpmNamespaceConstants;
import cz.muni.fi.cpm.model.CpmDocument;
import cz.muni.fi.cpm.model.INode;
import io.vavr.control.Either;
import reactor.test.StepVerifier;

@DisplayName("CPM Document Utils Test")
class CpmDocumentUtilsTest {

  private static final String ERR_STATEMENT_NULL = "Statement can not be null!";
  private static final String ERR_REFERENCED_META_MISSING = "Statement does not have 'referencedMetaBundleId' attribute, or its value is null!";
  private static final String ERR_REFERENCED_META_WRONG_TYPE = "referencedMetaBundleId value is not instance of QualifiedName!";
  private static final String ERR_CPM_DOCUMENT_NULL = "CpmDocument can not be null!";
  private static final String ERR_MAIN_ACTIVITY_NULL = "MainActivity in CpmDocument can not be null!";

  private final ProvFactory provFactory = new ProvFactory();

  private QualifiedName cpmAttributeName(String localPart) {
    return provFactory.newQualifiedName(CpmNamespaceConstants.CPM_NS, localPart, CpmNamespaceConstants.CPM_PREFIX);
  }

  private Element elementWithReferencedMetaBundleId(Object value, QualifiedName type) {
    Element element = mock(Element.class, org.mockito.Mockito.withSettings().extraInterfaces(HasOther.class));
    HasOther hasOther = (HasOther) element;

    Other referencedMetaBundleId = provFactory.newOther(
        cpmAttributeName("referencedMetaBundleId"),
        value,
        type);
    when(hasOther.getOther()).thenReturn(List.of(referencedMetaBundleId));
    return element;
  }

  private Element elementWithReferencedBundleId(Object value, QualifiedName type) {
    Element element = mock(Element.class, org.mockito.Mockito.withSettings().extraInterfaces(HasOther.class));
    HasOther hasOther = (HasOther) element;

    Other referencedBundleId = provFactory.newOther(
        cpmAttributeName("referencedBundleId"),
        value,
        type);
    when(hasOther.getOther()).thenReturn(List.of(referencedBundleId));
    return element;
  }

  private <R> void assertLeft(
      String expectedMessage,
      Either<ApplicationException, R> result) {

    assertTrue(result.isLeft());
    Throwable exception = result.getLeft();
    assertInstanceOf(ApplicationException.class, exception);
    assertNotNull(exception);
    assertEquals(expectedMessage, exception.getMessage());
  }

  private <R> void assertRight(
      R expected,
      Either<ApplicationException, R> result) {

    assertTrue(result.isRight());
    R value = result.get();
    assertNotNull(value);
    assertSame(expected, value);
  }

  @Test
  @DisplayName("Functional getCpmReferencedMetaBundleId should return Either with exact Left side for null statement")
  void requireCpmReferencedMetaBundleId_shouldFailForNullStatement() {
    assertLeft(
        ERR_STATEMENT_NULL,
        CpmDocumentUtils.FUNCTIONAL.getCpmReferencedMetaBundleId(null));
  }

  @Test
  @DisplayName("Functional getCpmReferencedMetaBundleId should return Either with exact Left side when attribute is missing")
  void requireCpmReferencedMetaBundleId_shouldFailWhenAttributeMissing() {
    HasOther hasOther = mock(HasOther.class);
    when(hasOther.getOther()).thenReturn(List.of());

    assertLeft(
        ERR_REFERENCED_META_MISSING,
        CpmDocumentUtils.FUNCTIONAL.getCpmReferencedMetaBundleId(hasOther));
  }

  @Test
  @DisplayName("Functional getCpmReferencedMetaBundleId should return Either with exact Left side when attribute has wrong type")
  void requireCpmReferencedMetaBundleId_shouldFailWhenAttributeHasWrongType() {
    HasOther hasOther = (HasOther) elementWithReferencedMetaBundleId(
        "not-a-qualified-name",
        provFactory.getName().XSD_STRING);

    assertLeft(ERR_REFERENCED_META_WRONG_TYPE,
        CpmDocumentUtils.FUNCTIONAL.getCpmReferencedMetaBundleId(hasOther));
  }

  // --

  @Test
  @DisplayName("Functional getCpmReferencedMetaBundleId should return Either with qualified name in Right side when attribute is valid")
  void requireCpmReferencedMetaBundleId_shouldReturnQualifiedNameWhenAttributeIsValid() {
    QualifiedName expectedReference = provFactory.newQualifiedName(
        "https://example.org/bundles/",
        "meta-bundle-1",
        "ex");

    HasOther hasOther = (HasOther) elementWithReferencedMetaBundleId(
        expectedReference,
        provFactory.getName().PROV_QUALIFIED_NAME);

    assertRight(expectedReference, CpmDocumentUtils.FUNCTIONAL.getCpmReferencedMetaBundleId(hasOther));
  }

  // --

  @Test
  @DisplayName("Functional getCpmReferencedBundleId should return Either with qualified name in Right side when attribute is valid")
  void requireCpmReferencedBundleId_shouldReturnQualifiedNameWhenAttributeIsValid() {
    QualifiedName expectedReference = provFactory.newQualifiedName(
        "https://example.org/bundles/",
        "bundle-1",
        "ex");

    HasOther hasOther = (HasOther) elementWithReferencedBundleId(
        expectedReference,
        provFactory.getName().PROV_QUALIFIED_NAME);

    assertRight(expectedReference, CpmDocumentUtils.FUNCTIONAL.getCpmReferencedBundleId(hasOther));
  }

  // --

  @Test
  @DisplayName("Functional getMainActivityReferenceMetaBundleId should return Either with exact Left side for null document")
  void requireMainActivityReferenceMetaBundleId_shouldFailForNullDocument() {
    assertLeft(
        ERR_CPM_DOCUMENT_NULL,
        CpmDocumentUtils.FUNCTIONAL.getMainActivityReferenceMetaBundleId(null));
  }

  @Test
  @DisplayName("Functional getMainActivityReferenceMetaBundleId should return Either with exact Left side when main activity is null")
  void requireMainActivityReferenceMetaBundleId_shouldFailWhenMainActivityIsNull() {
    CpmDocument cpmDocument = mock(CpmDocument.class);
    when(cpmDocument.getMainActivity()).thenReturn(null);

    assertLeft(
        ERR_MAIN_ACTIVITY_NULL,
        CpmDocumentUtils.FUNCTIONAL.getMainActivityReferenceMetaBundleId(cpmDocument));
  }

  @Test
  @DisplayName("Functional getMainActivityReferenceMetaBundleId should return Either with referenced id from main activity in Right side")
  void requireMainActivityReferenceMetaBundleId_shouldReturnReferencedIdFromMainActivity() {
    QualifiedName expectedReference = provFactory.newQualifiedName("https://example.org/bundles/", "meta-bundle-2",
        "ex");
    Element activityElement = elementWithReferencedMetaBundleId(expectedReference,
        provFactory.getName().PROV_QUALIFIED_NAME);

    INode mainActivity = mock(INode.class);
    when(mainActivity.getAnyElement()).thenReturn(activityElement);

    CpmDocument cpmDocument = mock(CpmDocument.class);
    when(cpmDocument.getMainActivity()).thenReturn(mainActivity);

    assertRight(expectedReference, CpmDocumentUtils.FUNCTIONAL.getMainActivityReferenceMetaBundleId(cpmDocument));
  }

  @Test
  @DisplayName("Reactive getCpmReferencedMetaBundleId should propagate synchronous Either Left side value to reactive error channel")

  void getCpmReferencedMetaBundleId_shouldPropagateErrorToReactiveChannel() {
    StepVerifier.create(CpmDocumentUtils.REACTIVE.getCpmReferencedMetaBundleId(null))
        .expectErrorSatisfies((Throwable error) -> {
          assertInstanceOf(InternalApplicationException.class, error);
          assertEquals(ERR_STATEMENT_NULL, error.getMessage());
        })
        .verify();
  }

  @Test
  @DisplayName("Reactive getMainActivityReferenceMetaBundleId should emit referenced value from Either Right side")
  void getMainActivityReferenceMetaBundleId_shouldEmitReferencedId() {
    QualifiedName expectedReference = provFactory.newQualifiedName("https://example.org/bundles/", "meta-bundle-3",
        "ex");
    Element activityElement = elementWithReferencedMetaBundleId(expectedReference,
        provFactory.getName().PROV_QUALIFIED_NAME);

    INode mainActivity = mock(INode.class);
    when(mainActivity.getAnyElement()).thenReturn(activityElement);

    CpmDocument cpmDocument = mock(CpmDocument.class);
    when(cpmDocument.getMainActivity()).thenReturn(mainActivity);

    StepVerifier.create(CpmDocumentUtils.REACTIVE.getMainActivityReferenceMetaBundleId(cpmDocument))
        .expectNext(expectedReference)
        .verifyComplete();
  }
}
