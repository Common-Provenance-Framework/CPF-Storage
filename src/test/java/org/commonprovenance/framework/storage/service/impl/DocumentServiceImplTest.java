package org.commonprovenance.framework.storage.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.commonprovenance.framework.storage.exceptions.InternalApplicationException;
import org.commonprovenance.framework.storage.model.Document;
import org.commonprovenance.framework.storage.model.Format;
import org.commonprovenance.framework.storage.persistence.DocumentRepository;
import org.commonprovenance.framework.storage.persistence.dummy.DocumentDummyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import reactor.test.StepVerifier;

import java.util.UUID;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Service - DocumentServiceImpl")
class DocumentServiceImplTest {

  private final DocumentRepository documentRepository;

  private DocumentServiceImpl documentService;

  private final UUID IDENTIFIER_1 = UUID.fromString("e3cf8742-b595-47f4-8aae-a1e94b62a856");
  private final String BASE64_STRING_GRAPH_1 = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
  private final Format FORMAT_1 = Format.JSON;

  @Autowired
  public DocumentServiceImplTest() {
    this.documentRepository = new DocumentDummyRepository();
  }

  @BeforeEach
  void setUp() {
    documentService = new DocumentServiceImpl(documentRepository);
  }

  @Test
  @Order(1)
  @DisplayName("HappyPath - storeDocument - should return new Document which has been stored.")
  void storeDocument_return_new_document() {
    Document document = new Document(IDENTIFIER_1, BASE64_STRING_GRAPH_1, FORMAT_1);

    StepVerifier.create(documentService.storeDocument(document))
        .assertNext(doc -> {
          assertEquals(IDENTIFIER_1.toString(), doc.getIdentifier().toString(), "should have exact identifier");
          assertEquals(BASE64_STRING_GRAPH_1, doc.getGraph(), "should have exact graph");
          assertEquals(FORMAT_1.toString(), doc.getFormat().toString(), "should have exact format");
        })
        .verifyComplete();
  }

  @Test
  @Order(2)
  @DisplayName("ErrorPath - storeDocument - should propagate exception from repository, if any.")
  void storeDocument_propagete_exception_from_repository() {
    StepVerifier.create(documentService.storeDocument(null))
        .verifyErrorSatisfies(err -> {
          assertInstanceOf(InternalApplicationException.class,
              err,
              "should be InternalApplicationException - Exception");
          assertEquals(
              "Illegal argument!",
              err.getMessage(),
              "should have exact error message");

          assertInstanceOf(
              IllegalArgumentException.class,
              err.getCause(),
              "should be NullPointerException - Exception cause");
          assertEquals(
              "Document can not be 'null'!",
              err.getCause().getMessage(),
              "should have exact error message");
        });
  }

  @Test
  @Order(3)
  @DisplayName("HappyPath - getAllDocuments - should return all Flux with all documents from repository.")
  void getAllDocuments_should_return_all_documents() {

    StepVerifier.create(documentService.getAllDocuments())
        .assertNext(doc -> {
          assertEquals(IDENTIFIER_1.toString(), doc.getIdentifier().toString());
        })
        .verifyComplete();
  }

  @Test
  @Order(4)
  @DisplayName("HappyPath - getDocumentById - should return Mono with exact document from repository.")
  void getDocumentById_should_return_mono_with_exact_document() {

    StepVerifier.create(documentService.getDocumentById(IDENTIFIER_1))
        .assertNext(doc -> {
          assertEquals(IDENTIFIER_1.toString(), doc.getIdentifier().toString());
        })
        .verifyComplete();
  }

  @Test
  @Order(5)
  @DisplayName("HappyPath - getDocumentById - should return empty Mono.")
  void getDocumentById_should_return_empty_mono() {

    StepVerifier.create(documentService.getDocumentById(UUID.randomUUID()))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  @Order(6)
  @DisplayName("ErrorPath - getDocumentById - should propagate exception from repository, if any.")
  void getDocumentById_propagete_exception_from_repository() {

    StepVerifier.create(documentService.getDocumentById(null))
        .verifyErrorSatisfies(err -> {
          assertInstanceOf(InternalApplicationException.class,
              err,
              "should be InternalApplicationException - Exception");
          assertEquals(
              "DocumentNeo4jRepository - Error while reading document",
              err.getMessage(),
              "should have exact error message");

          assertInstanceOf(
              IllegalArgumentException.class,
              err.getCause(),
              "should be NullPointerException - Exception cause");
          assertEquals(
              "Identifier can not be 'null'!",
              err.getCause().getMessage(),
              "should have exact error message");
        });
  }

  @Test
  @Order(7)
  @DisplayName("HappyPath - deleteById - should delete document from repository and return empty Mono.")
  void deleteDocumentById_should_delete_document() {

    StepVerifier.create(documentService.getAllDocuments())
      .expectNextCount(1)
        .verifyComplete();

    StepVerifier.create(documentService.deleteDocumentById(IDENTIFIER_1))
        .verifyComplete();

    StepVerifier.create(documentService.getAllDocuments())
      .expectNextCount(0)
        .verifyComplete();
  }
}