package org.commonprovenance.framework.store.persistence.neo4j.entity.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.Format;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import reactor.test.StepVerifier;

@DisplayName("Neo4j Repository - EntityMapper")
class EntityFactoryTest {

  @Test
  @DisplayName("HappyPath - should return Mono with DocumentEntity")
  void should_map_Document_to_DocumentEntity() {
    String testId = "6ee9d79b-0615-4cb1-b0f3-2303d10c8cff";
    String base64StringGraph = "AAAAQQAAAGIAAAByAAAAYQAAAGsAAABhAAAAIAAAAEQAAABhAAAAYgAAAHIAAABhAAAALgAAAC4=";
    String format = "JSON";

    Document document = new Document(
        UUID.fromString(testId),
        base64StringGraph,
        Format.from(format).get());

    StepVerifier.create(EntityFactory.toEntity(document))
        .assertNext(entity -> {
          assertEquals(testId, entity.getId());
          assertEquals(base64StringGraph, entity.getGraph());
          assertEquals(format, entity.getFormat());
        })
        .verifyComplete();
  }
}