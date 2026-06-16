package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory;

import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;

public class TrustedPartyNodeFactory {

  public static TrustedPartyNode build(TrustedParty trustedParty) {
    return new TrustedPartyNode(
        trustedParty.getName(),
        trustedParty.getClientCertificate(),
        trustedParty.getIsChecked(),
        trustedParty.getIsValid(),
        trustedParty.getIsDefault())
        .withUrl(trustedParty.getUrl());
  }

}
