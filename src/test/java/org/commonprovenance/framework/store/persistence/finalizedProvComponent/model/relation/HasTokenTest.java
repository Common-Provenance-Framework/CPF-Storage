package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TokenNode;
import org.junit.jupiter.api.Test;

class HasTokenTest {

  TokenNode TOKEN = new TokenNode("jwt");
  HasToken HAS_TOKEN = new HasToken(TOKEN);

  @Test
  void withTokenPreserveValuesAndRelationships() {
    TokenNode token = new TokenNode("new_jwt");
    HasToken relation = HAS_TOKEN.withToken(token);

    assertEquals(HAS_TOKEN.id(), relation.id());
    assertEquals(token, relation.token());
  }

  @Test
  void toStringExactString() {
    String expected = "HasToken [token=TokenNode [id=null, jwt=jwt, wasIssuedBy=[]]]";
    assertEquals(expected, HAS_TOKEN.toString());
  }
}
