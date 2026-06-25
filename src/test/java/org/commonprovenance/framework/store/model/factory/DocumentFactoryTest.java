package org.commonprovenance.framework.store.model.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.UUID;

import org.commonprovenance.framework.store.controller.dto.form.DocumentFormDTO;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.ConstraintException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Domain Model - ModelFactory")
public class DocumentFactoryTest {
  // persistence
  @Test
  @DisplayName("HappyPath - should return Either with Document in Right side")
  public void should_map_DocumentEntity_to_Document() {
    String testId = UUID.randomUUID().toString();
    String base64StringGraph = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
    Format format = Format.JSON;

    DocumentNode entity = new DocumentNode(
        testId,
        base64StringGraph,
        format.toString());

    DocumentFactory.build(entity)
        .peek((Document document) -> assertEquals(Format.JSON, document.getFormat()))
        .peek((Document document) -> assertEquals(base64StringGraph, document.getGraph()))
        .peekLeft(exception -> fail("Left side has not been expected! Got: [" + exception.getClass().getSimpleName() + "]: " + exception.getMessage()));
  }

  @Test
  @DisplayName("ErrorPath - should return Either with InternalApplicationException in Left side, if format is not valid Format string")
  void should_fail_format_InternalApplicationException() {
    UUID testId = UUID.randomUUID();
    String base64StringGraph = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
    String format = "unknown";

    DocumentNode entity = new DocumentNode(
        testId.toString(),
        base64StringGraph,
        format);

    DocumentFactory.build(entity)
        .peek((Document document) -> fail("Right side has not been expected! Got: [" + document.getIdentifier().getOrElse("unknown")))
        .peekLeft(exception -> assertInstanceOf(ApplicationException.class, exception))
        .peekLeft(exception -> assertInstanceOf(ConstraintException.class, exception))
        .peekLeft(exception -> assertNull(exception.getCause()))
        .peekLeft(exception -> assertEquals(
            "Validation of class 'Document' faild with message: Field with name 'format' can not be null!",
            exception.getMessage()));
  }

  @Test
  @DisplayName("ErrorPath - should return Mono with InternalApplicationException, if any Exception")
  void should_fail_Exception_InternalApplicationException() {
    DocumentFactory.build((DocumentNode) null)
        .peek((Document document) -> fail("Right side has not been expected! Got: [" + document.getIdentifier().getOrElse("unknown")))
        .peekLeft(exception -> assertInstanceOf(ApplicationException.class, exception))
        .peekLeft(exception -> assertInstanceOf(ConstraintException.class, exception))
        .peekLeft(exception -> assertNull(exception.getCause()))
        .peekLeft(exception -> assertEquals(
            "Validation of class 'Document' faild with message: Field with name 'graph' can not be null!, Field with name 'format' can not be null!",
            exception.getMessage()));
  }

  @Test
  @DisplayName("HappyPath - should return Mono with Document")
  void should_map_DocumentFormDTO_to_Document() {
    String signature = "..";
    String base64StringGraph = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
    Format format = Format.JSON;

    DocumentFormDTO formular = new DocumentFormDTO(base64StringGraph, format, signature);

    Document document = DocumentFactory.build(formular);
    assertNotNull(document);
    assertEquals(Format.JSON, document.getFormat(), "should have exact format");
    assertEquals(base64StringGraph, document.getGraph(), "should have exact graph");
  }

}
