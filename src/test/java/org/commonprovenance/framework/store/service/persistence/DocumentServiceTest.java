package org.commonprovenance.framework.store.service.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.DocumentRepository;
import org.commonprovenance.framework.store.service.persistence.finalizedProvComponent.impl.DocumentServiceImpl;
import org.commonprovenance.framework.store.service.web.store.StoreWebService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openprovenance.prov.vanilla.QualifiedName;

import cz.muni.fi.cpm.model.CpmDocument;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("Service - DocumentServiceImpl UnitTest")
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {
  @Mock
  private Token token;

  @Mock
  private CpmDocument cpmDocument;

  final private String UUID_STR_1 = "e3cf8742-b595-47f4-8aae-a1e94b62a856";
  final private String TEST_ORG_ID_1 = "6ee9d79b-0615-4cb1-b0f3-2303d10c8cff";
  final private String BASE64_STRING_GRAPH_1 = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
  final private Format FORMAT_1 = Format.JSON;

  private Document DOCUMENT_1;

  final private String UUID_STR_2 = "dc3b1fc8-d01e-4405-8cf8-94320a11ba4c";
  final private String BASE64_STRING_GRAPH_2 = "AAAASAAAAGUAAABsAAAAbAAAAG8AAAAgAAAAVwAAAG8AAAByAAAAbAAAAGQAAAAh";
  final private Format FORMAT_2 = Format.JSON;
  private Document DOCUMENT_2;

  private class DocumentRepositoryStub implements DocumentRepository {

    @Override
    public Mono<Void> save(Document document) {
      return document == null
          ? Mono.error(new InternalApplicationException("Illegal argument!",
              new IllegalArgumentException("Document can not be 'null'!")))
          : Mono.empty();
    }

    @Override
    public Mono<Document> findByIdentifier(String identifier) {

      if (identifier == null)
        return Mono.error(new InternalApplicationException(
            "DocumentNeo4jRepository - Error while reading document",
            new IllegalArgumentException(
                "Identifier can not be 'null'!")));

      switch (identifier) {
        case UUID_STR_1:
          return Mono.just(DOCUMENT_1);
        case UUID_STR_2:
          return Mono.just(DOCUMENT_2);
        default:
          return Mono.empty();
      }
    }

    @Override
    public Mono<Boolean> existsByIdentifier(String identifier) {
      if (identifier == null)
        return Mono.error(new InternalApplicationException(
            "DocumentNeo4jRepository - Error while reading document",
            new IllegalArgumentException(
                "Identifier can not be 'null'!")));

      switch (identifier) {
        case UUID_STR_1:
          return Mono.just(true);
        case UUID_STR_2:
          return Mono.just(true);
        default:
          return Mono.just(false);
      }
    }

    @Override
    public Mono<String> getOrganizationIdentifierByIdentifier(String identifier) {
      return Mono.just(TEST_ORG_ID_1);
    }
  }

  @Mock
  private StoreWebService storeWebService;

  private final DocumentRepository documentRepository;

  private DocumentServiceImpl documentService;

  public DocumentServiceTest() {
    this.documentRepository = new DocumentRepositoryStub();
  }

  @BeforeEach
  void setUp() {
    documentService = new DocumentServiceImpl(documentRepository, storeWebService);
    DOCUMENT_1 = new Document(BASE64_STRING_GRAPH_1, FORMAT_1, cpmDocument, token);
    DOCUMENT_2 = new Document(BASE64_STRING_GRAPH_2, FORMAT_2);
  }

  @Test
  @DisplayName("HappyPath - storeDocument - should return new Document which has been stored.")
  void storeDocument_return_new_document() {

    StepVerifier.create(documentService.storeDocument(DOCUMENT_1))
        .verifyComplete();
  }

  @Test
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
  @DisplayName("HappyPath - getDocumentById - should return Mono with exact document from repository.")
  void getDocumentByIdentifier_should_return_mono_with_exact_document() {
    when(cpmDocument.getBundleId()).thenAnswer(invocation -> {
      return new QualifiedName(
          "http://localhost:8080/api/v1/organizations/6fb292aa-ee38-48ae-998f-079ad9d01e7c/documents/",
          UUID_STR_1,
          "storage");
    });
    StepVerifier.create(documentService.getDocumentByIdentifier(UUID_STR_1))
        .assertNext(doc -> {
          assertEquals(UUID_STR_1, doc.getIdentifier().get(),
              "should have exact id");
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("HappyPath - getDocumentByIdentifier - should return empty Mono.")
  void getDocumentByIdentifier_should_return_empty_mono() {

    StepVerifier.create(documentService.getDocumentByIdentifier(UUID.randomUUID().toString()))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  @DisplayName("ErrorPath - getDocumentByIdentifier - should propagate exception from repository, if any.")
  void getDocumentByIdentifier_propagete_exception_from_repository() {

    StepVerifier.create(documentService.getDocumentByIdentifier(null))
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
}
