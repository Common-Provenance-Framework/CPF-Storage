package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.commonprovenance.framework.store.model.Document;
import org.commonprovenance.framework.store.model.GraphFormat;
import org.commonprovenance.framework.store.model.Organization;
import org.commonprovenance.framework.store.model.Token;
import org.commonprovenance.framework.store.model.TrustedParty;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.DocumentNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.OrganizationNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TokenNode;
import org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node.TrustedPartyNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.vavr.control.Either;

@ExtendWith(MockitoExtension.class)
public class OrganizationNodeFactoryTest {
  private final String ORGANIZATION_ID = "organization_identifier";
  private final String ORGANIZATION_CERT = "client_cert";
  private final String ORGANIZATION_INT_CERT_1 = "int_cert_1";
  private final String ORGANIZATION_INT_CERT_2 = "int_cert_2";

  private final String DOCUMENT_IDENTIFIER = "document_identifier";
  private final String DOCUMENT_GRAPH = "graph";
  private final GraphFormat DOCUMENT_GRAPH_FORMAT = GraphFormat.JSON;

  private final String TOKEN_JWT = "jwt";

  private final String TP_NAME = "name";
  private final String TP_CERT = "cert";

  @Mock
  private Organization organization;

  @Mock
  private Document document;

  @Mock
  private Token token;

  @Mock
  private TrustedParty trustedParty;

  @BeforeEach
  void setUp() {
    when(organization.getIdentifier()).thenReturn(ORGANIZATION_ID);
    when(organization.getClientCertificate()).thenReturn(ORGANIZATION_CERT);
    when(organization.getIntermediateCertificates()).thenReturn(List.of(ORGANIZATION_INT_CERT_1, ORGANIZATION_INT_CERT_2));
  }

  @Test
  void build_buildNode() {
    OrganizationNode result = OrganizationNodeFactory.build(organization);
    OrganizationNode expected = new OrganizationNode(ORGANIZATION_ID, ORGANIZATION_CERT, List.of(ORGANIZATION_INT_CERT_1, ORGANIZATION_INT_CERT_2));
    assertEquals(expected, result);
  }

  @Test
  void buildWithRelations_buildNodeWithDocumentAndTrustedParty() {
    when(organization.getTrustedParty()).thenReturn(Optional.of(trustedParty));
    when(organization.getDocument()).thenReturn(Optional.of(document));

    when(trustedParty.getId()).thenReturn(TP_NAME);
    when(trustedParty.getClientCertificate()).thenReturn(TP_CERT);
    when(trustedParty.getIsChecked()).thenReturn(true);
    when(trustedParty.getIsValid()).thenReturn(true);
    when(trustedParty.getIsDefault()).thenReturn(true);
    when(trustedParty.getUrl()).thenReturn(Optional.empty());

    when(document.getIdentifier()).thenReturn(Either.right(DOCUMENT_IDENTIFIER));
    when(document.getGraph()).thenReturn(DOCUMENT_GRAPH);
    when(document.getFormat()).thenReturn(DOCUMENT_GRAPH_FORMAT);

    OrganizationNode result = OrganizationNodeFactory.buildWithRelations(organization);

    OrganizationNode expected = new OrganizationNode(ORGANIZATION_ID, ORGANIZATION_CERT, List.of(ORGANIZATION_INT_CERT_1, ORGANIZATION_INT_CERT_2))
        .withDocument(new DocumentNode(DOCUMENT_IDENTIFIER, DOCUMENT_GRAPH, DOCUMENT_GRAPH_FORMAT.toString()))
        .withTrustedParty(new TrustedPartyNode(TP_NAME, TP_CERT, true, true, true));

    assertEquals(expected, result);
  }

  @Test
  void buildWithFullRelations_buildNodeFullHydratation() {
    when(organization.getTrustedParty()).thenReturn(Optional.of(trustedParty));
    when(organization.getDocument()).thenReturn(Optional.of(document));

    when(trustedParty.getId()).thenReturn(TP_NAME);
    when(trustedParty.getClientCertificate()).thenReturn(TP_CERT);
    when(trustedParty.getIsChecked()).thenReturn(true);
    when(trustedParty.getIsValid()).thenReturn(true);
    when(trustedParty.getIsDefault()).thenReturn(true);
    when(trustedParty.getUrl()).thenReturn(Optional.empty());

    when(document.getIdentifier()).thenReturn(Either.right(DOCUMENT_IDENTIFIER));
    when(document.getGraph()).thenReturn(DOCUMENT_GRAPH);
    when(document.getFormat()).thenReturn(DOCUMENT_GRAPH_FORMAT);
    when(document.getToken()).thenReturn(Optional.of(token));

    when(token.getJwt()).thenReturn(TOKEN_JWT);
    when(token.getTrustedParty()).thenReturn(Optional.of(trustedParty));

    OrganizationNode result = OrganizationNodeFactory.buildWithFullRelations(organization);

    TrustedPartyNode tp = new TrustedPartyNode(TP_NAME, TP_CERT, true, true, true);

    TokenNode expectedToken = new TokenNode(TOKEN_JWT)
        .withTrustedParty(tp);

    OrganizationNode expected = new OrganizationNode(ORGANIZATION_ID, ORGANIZATION_CERT, List.of(ORGANIZATION_INT_CERT_1, ORGANIZATION_INT_CERT_2))
        .withDocument(new DocumentNode(DOCUMENT_IDENTIFIER, DOCUMENT_GRAPH, DOCUMENT_GRAPH_FORMAT.toString()).withToken(expectedToken))
        .withTrustedParty(new TrustedPartyNode(TP_NAME, TP_CERT, true, true, true));

    assertEquals(expected, result);
  }

}
