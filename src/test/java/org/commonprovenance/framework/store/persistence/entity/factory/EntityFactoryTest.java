package org.commonprovenance.framework.store.persistence.entity.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory.DocumentNodeFactory;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openprovenance.prov.vanilla.QualifiedName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import cz.muni.fi.cpm.model.CpmDocument;

@ExtendWith(MockitoExtension.class)
@DisplayName("Neo4j Repository - EntityMapper")
class EntityFactoryTest {

  @Mock
  private CpmDocument cpmDocument;

  @Mock
  private Token token;

  @Test
  @DisplayName("HappyPath - should return Mono with DocumentEntity")
  void should_map_Document_to_DocumentEntity() {
    String testId = "6ee9d79b-0615-4cb1-b0f3-2303d10c8cff";
    String base64StringGraph = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
    GraphFormat format = GraphFormat.JSON;
    when(cpmDocument.getBundleId()).thenAnswer(invocation -> {
      return new QualifiedName("http://localhost:8080/api/v1/organizations/6fb292aa-ee38-48ae-998f-079ad9d01e7c/documents/", testId, "storage");
    });
    Document document = new Document(
        base64StringGraph,
        format,
        cpmDocument,
        token);

    DocumentNode node = DocumentNodeFactory.build(document);
    assertEquals(testId, node.getIdentifier());
    assertEquals(base64StringGraph, node.getGraph());
    assertEquals(format, GraphFormat.from(node.getFormat()).get());
  }
}
