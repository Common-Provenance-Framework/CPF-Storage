package org.commonprovenance.framework.store.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j.DocumentNeo4jRepository;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.neo4j.client.DocumentNeo4jRepositoryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openprovenance.prov.vanilla.QualifiedName;

import cz.muni.fi.cpm.model.CpmDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@DisplayName("Neo4j Repository - DocumentNeo4jRepository Specification")
class DocumentRepositorySpec {

  @Mock
  private DocumentNeo4jRepositoryClient client;

  private DocumentNeo4jRepository repository;
  @Mock
  private CpmDocument cpmDocument;

  @Mock
  private Token token;

  private final String TEST_ID_1 = "e3cf8742-b595-47f4-8aae-a1e94b62a856";
  private final String BASE64_STRING_GRAPH_1 = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
  private final GraphFormat FORMAT_1 = GraphFormat.JSON;

  @BeforeEach
  void setUp() {
    repository = new DocumentNeo4jRepository(client);
  }

  @Test
  @DisplayName("Create - should call save method with exact parameters")
  void created_should_call_save_method_with_exact_paramters() {
    Document doucment = new Document(
        BASE64_STRING_GRAPH_1,
        FORMAT_1,
        cpmDocument,
        token);

    when(client.save(any())).thenAnswer(invocation -> {
      DocumentNode argumentEntity = invocation.getArgument(0);
      return Mono.just(argumentEntity);
    });

    when(cpmDocument.getBundleId()).thenAnswer(invocation -> {
      return new QualifiedName("http://localhost:8080/api/v1/organizations/6fb292aa-ee38-48ae-998f-079ad9d01e7c/documents/", TEST_ID_1, "storage");
    });

    StepVerifier.create(repository.save(doucment))
        .verifyComplete();

    ArgumentCaptor<DocumentNode> captor = ArgumentCaptor.forClass(DocumentNode.class);
    verify(
        client,
        times(1)
            .description("Repository save method should be invoked once"))
        .save(captor.capture());

    DocumentNode capturedEntity = captor.getValue();
    assertTrue(capturedEntity.getIdentifier().equals(TEST_ID_1)
        && capturedEntity.getGraph().equals(BASE64_STRING_GRAPH_1)
        && capturedEntity.getFormat().equals(FORMAT_1.toString()),
        "should be called with exact entity");
  }

  @Test
  @DisplayName("GetByIdentifier - should call findByIdentifier method with exact parameters")
  void getByIdentifier_should_call_findByIdentifier_method_with_exact_paramters() {
    when(client.getIdByIdentifier(anyString())).thenReturn(Flux.empty());

    StepVerifier.create(repository.findByIdentifier(TEST_ID_1))
        .expectNextCount(0)
        .verifyError();

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(
        client,
        times(1)
            .description("Neo4j client getIdByIdentifier method should be invoked once"))
        .getIdByIdentifier(captor.capture());

    assertEquals(TEST_ID_1, captor.getValue(), "Repository findById method should be invoked with exact argument");
  }
}
