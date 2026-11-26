package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;
import java.util.Optional;

import org.commonprovenance.framework.store.model.GraphFormat;
import org.junit.jupiter.api.Test;

class DocumentNodeTest {
  private final TokenNode TOKEN = new TokenNode("jwt");
  private final DocumentNode DOCUMENT = new DocumentNode("identifier", "graph", "format").withToken(TOKEN);

  @Test
  void withIdentifierPreserveValuesAndRelationships() {
    String newIdentifier = "new_identifier";
    DocumentNode node = DOCUMENT.withIdentifier(newIdentifier);

    assertEquals(newIdentifier, node.identifier());
    assertEquals(DOCUMENT.graph(), node.graph());
    assertEquals(DOCUMENT.format(), node.format());
    assertEquals(DOCUMENT.tokens(), node.tokens());
  }

  @Test
  void withGraphPreserveValuesAndRelationships() {
    String newGraph = "new_grap";
    DocumentNode node = DOCUMENT.withGraph(newGraph);

    assertEquals(DOCUMENT.identifier(), node.identifier());
    assertEquals(newGraph, node.graph());
    assertEquals(DOCUMENT.format(), node.format());
    assertEquals(DOCUMENT.tokens(), node.tokens());
  }

  @Test
  void withFormatPreserveValuesAndRelationships() {
    GraphFormat newFormat = GraphFormat.JSON;
    DocumentNode node = DOCUMENT.withFormat(newFormat);

    assertEquals(DOCUMENT.identifier(), node.identifier());
    assertEquals(DOCUMENT.graph(), node.graph());
    assertEquals(newFormat.toString(), node.format());
    assertEquals(DOCUMENT.tokens(), node.tokens());
  }

  @Test
  void withTokenPreserveValuesAndRelationships() {
    TokenNode newToken = new TokenNode("new_jwt");
    DocumentNode node = DOCUMENT.withToken(Optional.of(newToken));

    assertEquals(DOCUMENT.identifier(), node.identifier());
    assertEquals(DOCUMENT.graph(), node.graph());
    assertEquals(DOCUMENT.format(), node.format());
    assertIterableEquals(List.of(TOKEN, newToken), node.tokens());
  }

  @Test
  void tokensReturnsTokens() {
    assertIterableEquals(List.of(TOKEN), DOCUMENT.tokens());
  }

  @Test
  void toStringExactString() {
    String expected = "DocumentNode ["
        + "id=null, "
        + "identifier=identifier, "
        + "graph=graph, "
        + "format=format, "
        + "hasToken=[HasToken ["
        + "token=TokenNode ["
        + "id=null, "
        + "jwt=jwt, "
        + "wasIssuedBy=[]"
        + "]]]]";
    assertEquals(expected, DOCUMENT.toString());
  }

}
