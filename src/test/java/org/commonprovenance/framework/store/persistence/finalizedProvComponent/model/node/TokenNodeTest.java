package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class TokenNodeTest {
  private final TrustedPartyNode TRUSTED_PARTY = new TrustedPartyNode("name", "cert", true, true, true);
  private final TokenNode TOKEN = new TokenNode("jwt").withTrustedParty(TRUSTED_PARTY);

  @Test
  void withJwtPreserveValuesAndRelationships() {
    String newJwt = "new_jwt";
    TokenNode node = TOKEN.withJwt(newJwt);

    assertEquals(newJwt, node.jwt());
    assertEquals(TOKEN.trustedParties(), node.trustedParties());
  }

  @Test
  void withTrustedPartyPreserveValuesAndRelationships() {
    TrustedPartyNode tp = new TrustedPartyNode("new_name", "new_cert", true, true, false)
        .withUrl("url");
    TokenNode node = TOKEN.withTrustedParty(tp);

    assertEquals(TOKEN.jwt(), node.jwt());
    assertEquals(List.of(TRUSTED_PARTY, tp), node.trustedParties());
  }

  @Test
  void trustedPartiesReturnsTrustedPartyNodes() {
    assertIterableEquals(List.of(TRUSTED_PARTY), TOKEN.trustedParties());
  }

  @Test
  void toStringExactString() {
    String expected = "TokenNode [id=null, jwt=jwt, "
        + "wasIssuedBy=[WasIssuedBy [trustedParty=TrustedPartyNode [id=null, name=name, clientCertificate=cert, url=null, isChecked=true, isValid=true, isDefault=true"
        + "]]]]";
    assertEquals(expected, TOKEN.toString());
  }
}
