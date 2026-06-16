package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory;

import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TokenNode;

public class TokenNodeFactory {

  public static TokenNode build(Token token) {
    return new TokenNode(token.getJwt());
  }

  public static TokenNode buildWithRelations(Token token) {
    return TokenNodeFactory.build(token)
        .withTrustedParty(token.getTrustedParty().map(TrustedPartyNodeFactory::build));
  }

}
