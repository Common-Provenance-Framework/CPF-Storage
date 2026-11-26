package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TokenNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TokenNodeFactoryTest {
  private final String TOKEN_JWT = "jwt";

  private final String TP_NAME = "name";
  private final String TP_CERT = "cert";

  @Mock
  private Token token;

  @Mock
  private TrustedParty trustedParty;

  @BeforeEach
  void setUp() {
    when(token.getJwt()).thenReturn(TOKEN_JWT);
  }

  @Test
  void build_buildNode() {
    TokenNode result = TokenNodeFactory.build(token);
    TokenNode expected = new TokenNode(TOKEN_JWT);
    assertEquals(expected, result);
  }

  @Test
  void buildWithRelations_buildNodeWithTrustedParty() {
    when(token.getTrustedParty()).thenReturn(Optional.of(trustedParty));

    when(trustedParty.getId()).thenReturn(TP_NAME);
    when(trustedParty.getClientCertificate()).thenReturn(TP_CERT);
    when(trustedParty.getIsChecked()).thenReturn(true);
    when(trustedParty.getIsValid()).thenReturn(true);
    when(trustedParty.getIsDefault()).thenReturn(true);
    when(trustedParty.getUrl()).thenReturn(Optional.empty());

    TokenNode result = TokenNodeFactory.buildWithRelations(token);

    TokenNode expected = new TokenNode(TOKEN_JWT)
        .withTrustedParty(new TrustedPartyNode(TP_NAME, TP_CERT, true, true, true));

    assertEquals(expected, result);
  }

}
