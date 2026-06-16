package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.types;

import java.util.List;

import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;

public interface HasTrustedPartyNodes {

  List<TrustedPartyNode> getTrustedParties();

}
