package org.commonprovenance.framework.storage.service.impl;

import org.commonprovenance.framework.storage.model.Document;
import org.commonprovenance.framework.storage.model.Format;
import org.commonprovenance.framework.storage.persistence.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Service - DocumentServiceImpl")
class DocumentServiceImplSpec {

  @Mock
  private DocumentRepository documentRepository;

  private DocumentServiceImpl documentService;

  private final UUID IDENTIFIER_1 = UUID.fromString("e3cf8742-b595-47f4-8aae-a1e94b62a856");
  private final String BASE64_STRING_GRAPH_1 = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
  private final Format FORMAT_1 = Format.JSON;

  private final UUID IDENTIFIER_2 = UUID.fromString("dc3b1fc8-d01e-4405-8cf8-94320a11ba4c");
  private final String BASE64_STRING_GRAPH_2 = "AAAASAAAAGUAAABsAAAAbAAAAG8AAAAgAAAAVwAAAG8AAAByAAAAbAAAAGQAAAAh";
  private final Format FORMAT_2 = Format.JSON;

  @BeforeEach
  void setUp() {
    documentService = new DocumentServiceImpl(documentRepository);
  }

  @Test
  @DisplayName("storeDocument - should call create on repository once with exact Document.")

  void storeDocument_should_call_create_on_repository() {
    Document document = new Document(IDENTIFIER_1, BASE64_STRING_GRAPH_1, FORMAT_1);

    when(documentRepository.create(any(Document.class))).thenAnswer(invocation -> {
      Document documnent = invocation.getArgument(0);
      return Mono.just(documnent);
    });

    StepVerifier.create(documentService.storeDocument(document))
        .expectNextCount(1)
        .verifyComplete();

    ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
    verify(
        documentRepository,
        times(1)
            .description("Repository create method should be invoked once"))
        .create(captor.capture());

    Document capturedEntity = captor.getValue();
    assertTrue(capturedEntity.getIdentifier().equals(IDENTIFIER_1)
        && capturedEntity.getGraph().equals(BASE64_STRING_GRAPH_1)
        && capturedEntity.getFormat().equals(FORMAT_1),
        "should be called with exact Document");
  }

  @Test
  @DisplayName("getAllDocuments - should call getAll on repository once")
  void getAllDocuments_shouldReturnAllDocuments() {
    Document doc1 = new Document(IDENTIFIER_1, BASE64_STRING_GRAPH_1, FORMAT_1);
    Document doc2 = new Document(IDENTIFIER_2, BASE64_STRING_GRAPH_2, FORMAT_2);

    when(documentRepository.getAll()).thenReturn(Flux.just(doc1, doc2));

    Flux<Document> result = documentService.getAllDocuments();

    StepVerifier.create(result)
        .expectNextCount(2)
        .verifyComplete();

    verify(
        documentRepository,
        times(1)
            .description("Repository getAll method should be invoked once"))
        .getAll();
  }

  @Test
  void getDocumentById_shouldReturnDocument() {
    Document document = new Document(IDENTIFIER_1, BASE64_STRING_GRAPH_1, FORMAT_1);
    when(documentRepository.getById(IDENTIFIER_1)).thenReturn(Mono.just(document));

    StepVerifier.create(documentService.getDocumentById(IDENTIFIER_1))
        .expectNext(document)
        .verifyComplete();

    ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
    verify(
        documentRepository,
        times(1)
            .description("Repository getAll method should be invoked once"))
        .getById(captor.capture());

    UUID capturedId = captor.getValue();
    assertTrue(capturedId.equals(IDENTIFIER_1),
        "should be called with exact id");
  }

  @Test
  void deleteDocumentById_shouldDeleteDocument() {
    when(documentRepository.deleteById(IDENTIFIER_1)).thenReturn(Mono.empty().then());

    StepVerifier.create(documentService.deleteDocumentById(IDENTIFIER_1))
        .verifyComplete();

    ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
    verify(
        documentRepository,
        times(1)
            .description("Repository deleteById method should be invoked once"))
        .deleteById(captor.capture());

    UUID capturedId = captor.getValue();
    assertTrue(capturedId.equals(IDENTIFIER_1),
        "should be called with exact id");
  }
}