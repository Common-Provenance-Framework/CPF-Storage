package org.commonprovenance.framework.store.model.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.List;

import org.commonprovenance.framework.store.common.utils.ProvDocumentUtils;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.model.Document;
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

@DisplayName("CPM Document Utils Test")
class DocumentUtilsTest {

  private static final String ERR_MAIN_ACTIVITY_NULL = "MainActivity in CpmDocument can not be null!";

  private final ProvFactory provFactory = new ProvFactory();

  private QualifiedName cpmAttributeName(CpmAttribute cpmAttribute) {
    return provFactory.newQualifiedName(CpmNamespaceConstants.CPM_NS, cpmAttribute.toString(), CpmNamespaceConstants.CPM_PREFIX);
  }

  private Element activityWithReferencedMetaBundleId(Object value) {
    Element activityElement = mock(Activity.class, withSettings().extraInterfaces(HasOther.class));

    Other referencedMetaBundleId = provFactory.newOther(
        cpmAttributeName(CpmAttribute.REFERENCED_META_BUNDLE_ID),
        value,
        provFactory.getName().PROV_QUALIFIED_NAME);
    when(activityElement.getOther()).thenReturn(List.of(referencedMetaBundleId));

    return activityElement;
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
  @DisplayName("getMainActivityReferencedMetaBundleId should return Either with exact Left side when main activity is null")
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
  @DisplayName("getMainActivityReferencedMetaBundleId should return Either with referenced id from main activity in Right side")
  void requireMainActivityReferenceMetaBundleId_shouldReturnReferencedIdFromMainActivity() {
    QualifiedName expectedReference = provFactory.newQualifiedName(
        "https://example.org/bundles/",
        "meta-bundle-2",
        "ex");

    Element activityElement = activityWithReferencedMetaBundleId(expectedReference);

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

}
