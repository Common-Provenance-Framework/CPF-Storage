package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TokenNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
public class DocumentNodeFactoryTest {
  private final String DOCUMENT_IDENTIFIER = "identifier";
  private final String DOCUMENT_GRAPH = "graph";
  private final GraphFormat DOCUMENT_GRAPH_FORMAT = GraphFormat.JSON;

  private final String TOKEN_JWT = "jwt";

  private final String TP_NAME = "name";
  private final String TP_CERT = "cert";

  @Mock
  private Document document;

  @Mock
  private Token token;

  @Mock
  private TrustedParty trustedParty;

  @BeforeEach
  void setUp() {
    when(document.getIdentifier()).thenReturn(Either.right(DOCUMENT_IDENTIFIER));
    when(document.getGraph()).thenReturn(DOCUMENT_GRAPH);
    when(document.getFormat()).thenReturn(DOCUMENT_GRAPH_FORMAT);
  }

  @Test
  void build_buildNode() {
    DocumentNode result = DocumentNodeFactory.build(document);
    DocumentNode expected = new DocumentNode(DOCUMENT_IDENTIFIER, DOCUMENT_GRAPH, DOCUMENT_GRAPH_FORMAT.toString());
    assertEquals(expected, result);
  }

  @Test
  void buildWithRelations_buildNodeWithToken() {
    when(document.getToken()).thenReturn(Optional.of(token));
    when(token.getJwt()).thenReturn(TOKEN_JWT);

    DocumentNode result = DocumentNodeFactory.buildWithRelations(document);

    DocumentNode expected = new DocumentNode(DOCUMENT_IDENTIFIER, DOCUMENT_GRAPH, DOCUMENT_GRAPH_FORMAT.toString())
        .withToken(new TokenNode(TOKEN_JWT));

    assertEquals(expected, result);
  }

  @Test
  void buildWithFullRelations_buildNodeFullHydratation() {
    when(document.getToken()).thenReturn(Optional.of(token));
    when(token.getJwt()).thenReturn(TOKEN_JWT);
    when(token.getTrustedParty()).thenReturn(Optional.of(trustedParty));

    when(trustedParty.getId()).thenReturn(TP_NAME);
    when(trustedParty.getClientCertificate()).thenReturn(TP_CERT);
    when(trustedParty.getIsChecked()).thenReturn(true);
    when(trustedParty.getIsValid()).thenReturn(true);
    when(trustedParty.getIsDefault()).thenReturn(true);
    when(trustedParty.getUrl()).thenReturn(Optional.empty());

    DocumentNode result = DocumentNodeFactory.buildWithFullRelations(document);

    TrustedPartyNode tp = new TrustedPartyNode(TP_NAME, TP_CERT, true, true, true);
    TokenNode expectedToken = new TokenNode(TOKEN_JWT)
        .withTrustedParty(tp);

    DocumentNode expected = new DocumentNode(DOCUMENT_IDENTIFIER, DOCUMENT_GRAPH, DOCUMENT_GRAPH_FORMAT.toString())
        .withToken(expectedToken);

    assertEquals(expected, result);
  }

}
