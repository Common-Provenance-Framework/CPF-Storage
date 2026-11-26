package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.relation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;
import org.junit.jupiter.api.Test;

class WasIssuedByTest {

  TrustedPartyNode TRUSTED_PARTY = new TrustedPartyNode("name", "cert", true, true, true);
  WasIssuedBy TRUSTS = new WasIssuedBy(TRUSTED_PARTY);

  @Test
  void withTrustedPartyPreserveValuesAndRelationships() {
    TrustedPartyNode tp = new TrustedPartyNode("new_name", "new_cert", true, true, true);
    WasIssuedBy relation = TRUSTS.withTrustedParty(tp);

    assertEquals(TRUSTS.id(), relation.id());
    assertEquals(tp, relation.trustedParty());
  }

  @Test
  void toStringExactString() {
    String expected = "WasIssuedBy [trustedParty=TrustedPartyNode [id=null, name=name, clientCertificate=cert, url=null, isChecked=true, isValid=true, isDefault=true]]";
    assertEquals(expected, TRUSTS.toString());
  }
}
