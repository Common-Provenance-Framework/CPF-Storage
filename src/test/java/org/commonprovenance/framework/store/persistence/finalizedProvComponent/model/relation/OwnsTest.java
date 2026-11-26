package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.junit.jupiter.api.Test;

class OwnsTest {

  DocumentNode DOCUMENT = new DocumentNode("graph");
  Owns OWNS = new Owns(DOCUMENT);

  @Test
  void withDocumentPreserveValuesAndRelationships() {
    DocumentNode document = new DocumentNode("new_graph");
    Owns relation = OWNS.withDocument(document);

    assertEquals(OWNS.id(), relation.id());
    assertEquals(document, relation.document());
  }

  @Test
  void toStringExactString() {
    String expected = "Owns [document=DocumentNode [id=null, identifier=null, graph=graph, format=null, hasToken=[]]]";
    assertEquals(expected, OWNS.toString());
  }
}
