package org.commonprovenance.framework.store.persistence.neo4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.commonprovenance.framework.store.persistence.neo4j.entities.DocumentEntity;
import org.commonprovenance.framework.store.persistence.neo4j.repository.IDocumentNeo4jRepository;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("Neo4j Repository - DocumentNeo4jRepository Specification")
class DocumentNeo4jRepositorySpec {

  @Mock
  private IDocumentNeo4jRepository documentRepository;

  private DocumentNeo4jRepository repository;

  private final String TEST_ID_1 = "e3cf8742-b595-47f4-8aae-a1e94b62a856";
  private final String BASE64_STRING_GRAPH_1 = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
  private final String FORMAT_1 = "JSON";

  @BeforeEach
  void setUp() {
    repository = new DocumentNeo4jRepository(documentRepository);
  }

  @Test
  @DisplayName("Create - should call save method with exact parameters")
  void created_should_call_save_method_with_exact_paramters() {
    Document doucment = new Document(
        UUID.fromString(TEST_ID_1),
        BASE64_STRING_GRAPH_1,
        Format.from(FORMAT_1).get());

    when(documentRepository.save(any())).thenAnswer(invocation -> {
      DocumentEntity argumentEntity = invocation.getArgument(0);
      return Mono.just(argumentEntity);
    });

    StepVerifier.create(repository.create(doucment))
        .expectNextCount(1)
        .verifyComplete();

    ArgumentCaptor<DocumentEntity> captor = ArgumentCaptor.forClass(DocumentEntity.class);
    verify(
        documentRepository,
        times(1)
            .description("Repository save method should be invoked once"))
        .save(captor.capture());

    DocumentEntity capturedEntity = captor.getValue();
    assertTrue(capturedEntity.getIdentifier().equals(TEST_ID_1)
        && capturedEntity.getGraph().equals(BASE64_STRING_GRAPH_1)
        && capturedEntity.getFormat().equals(FORMAT_1),
        "should be called with exact entity");
  }

  @Test
  @DisplayName("GetAll - should call findAll method")
  void getAll_should_call_findAll_method_with_exact_paramters() {
    when(documentRepository.findAll()).thenReturn(Flux.empty());

    StepVerifier.create(repository.getAll())
        .expectNextCount(0)
        .verifyComplete();

    verify(
        documentRepository,
        times(1)
            .description("Repository findAll method should be invoked once"))
        .findAll();
  }

  @Test
  @DisplayName("GetById - should call findById method with exact parameters")
  void getById_should_call_findById_method_with_exact_paramters() {
    when(documentRepository.findById(anyString())).thenReturn(Mono.empty());

    StepVerifier.create(repository.getById(UUID.fromString(TEST_ID_1)))
        .expectNextCount(0)
        .verifyComplete();

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(
        documentRepository,
        times(1)
            .description("Repository findById method should be invoked once"))
        .findById(captor.capture());

    assertEquals(TEST_ID_1, captor.getValue(), "Repository findById method should be invoked with exact argument");
  }

  @Test
  @DisplayName("DeleteById - should call deleteById method with exact parameters")
  void deleteById_should_call_deleteById_method_with_exact_paramters_when_getbyid() {
    when(documentRepository.deleteById(anyString())).thenReturn(Mono.empty().then());

    StepVerifier.create(repository.deleteById(UUID.fromString(TEST_ID_1)))
        .expectNextCount(0)
        .verifyComplete();

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(
        documentRepository,
        times(1)
            .description("Repository deleteById method should be invoked once"))
        .deleteById(captor.capture());

    assertEquals(TEST_ID_1, captor.getValue(),
        "Repository deleteById method should be invoked with exact argument");
  }
}