package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class OrganizationNodeTest {
  private final TrustedPartyNode TRUSTED_PARTY = new TrustedPartyNode("name", "cert", true, true, true);
  private final TokenNode TOKEN = new TokenNode("jwt").withTrustedParty(TRUSTED_PARTY);
  private final DocumentNode DOCUMENT = new DocumentNode("identifier", "graph", "format").withToken(TOKEN);
  private final OrganizationNode ORGANIZATION = new OrganizationNode(
      "identifier",
      "client_certificate",
      List.of("intermediate_certificate_1", "intermediate_certificate_2"))
      .withDocument(DOCUMENT)
      .withTrustedParty(TRUSTED_PARTY);

  @Test
  void withIdentifierPreserveValuesAndRelationships() {
    String newIdentifier = "new_identifier";
    OrganizationNode node = ORGANIZATION.withIdentifier(newIdentifier);

    assertEquals(newIdentifier, node.identifier());
    assertEquals(ORGANIZATION.clientCertificate(), node.clientCertificate());
    assertIterableEquals(ORGANIZATION.intermediateCertificates(), node.intermediateCertificates());
    assertEquals(ORGANIZATION.documents(), node.documents());
    assertEquals(ORGANIZATION.trustedParties(), node.trustedParties());
  }

  @Test
  void withClientCertificatePreserveValuesAndRelationships() {
    String newCert = "new_client_certificate";
    OrganizationNode node = ORGANIZATION.withClientCertificate(newCert);

    assertEquals(ORGANIZATION.identifier(), node.identifier());
    assertEquals(newCert, node.clientCertificate());
    assertIterableEquals(ORGANIZATION.intermediateCertificates(), node.intermediateCertificates());
    assertEquals(ORGANIZATION.documents(), node.documents());
    assertEquals(ORGANIZATION.trustedParties(), node.trustedParties());
  }

  @Test
  void withIntermediateCertificatesPreserveValuesAndRelationships() {
    List<String> newIntCerts = List.of("new_intermediate_certificate_1", "new_intermediate_certificate_2");
    OrganizationNode node = ORGANIZATION.withIntermediateCertificates(newIntCerts);

    assertEquals(ORGANIZATION.identifier(), node.identifier());
    assertEquals(ORGANIZATION.clientCertificate(), node.clientCertificate());
    assertIterableEquals(newIntCerts, node.intermediateCertificates());
    assertEquals(ORGANIZATION.documents(), node.documents());
    assertEquals(ORGANIZATION.trustedParties(), node.trustedParties());
  }

  @Test
  void withDocumentPreserveValuesAndRelationships() {
    DocumentNode document = new DocumentNode("new_graph");
    OrganizationNode node = ORGANIZATION.withDocument(document);

    assertEquals(ORGANIZATION.identifier(), node.identifier());
    assertEquals(ORGANIZATION.clientCertificate(), node.clientCertificate());
    assertIterableEquals(ORGANIZATION.intermediateCertificates(), node.intermediateCertificates());
    assertEquals(List.of(DOCUMENT, document), node.documents());
    assertEquals(ORGANIZATION.trustedParties(), node.trustedParties());
  }

  @Test
  void withTrustedPartyPreserveValuesAndRelationships() {
    TrustedPartyNode tp = new TrustedPartyNode("new_name", "new_cert", true, true, false)
        .withUrl("url");
    OrganizationNode node = ORGANIZATION.withTrustedParty(tp);

    assertEquals(ORGANIZATION.identifier(), node.identifier());
    assertEquals(ORGANIZATION.clientCertificate(), node.clientCertificate());
    assertIterableEquals(ORGANIZATION.intermediateCertificates(), node.intermediateCertificates());
    assertEquals(ORGANIZATION.documents(), node.documents());
    assertEquals(List.of(TRUSTED_PARTY, tp), node.trustedParties());
  }

  @Test
  void trustedPartiesReturnsTrustedPartyNodes() {
    assertIterableEquals(List.of(TRUSTED_PARTY), ORGANIZATION.trustedParties());
  }

  @Test
  void documentsReturnsDocumentNodes() {
    assertIterableEquals(List.of(DOCUMENT), ORGANIZATION.documents());
  }

  @Test
  void toStringExactString() {
    String expected = "OrganizationNode "
        + "[id=null, identifier=identifier, clientCertificate=client_certificate, intermediateCertificates=[intermediate_certificate_1, intermediate_certificate_2], "
        + "trusts=[Trusts [trustedParty=TrustedPartyNode [id=null, name=name, clientCertificate=cert, url=null, isChecked=true, isValid=true, isDefault=true]]], "
        + "owns=[Owns [document=DocumentNode [id=null, identifier=identifier, graph=graph, format=format, "
        + "hasToken=[HasToken [token=TokenNode [id=null, jwt=jwt, "
        + "wasIssuedBy=[WasIssuedBy [trustedParty=TrustedPartyNode [id=null, name=name, clientCertificate=cert, url=null, isChecked=true, isValid=true, isDefault=true"
        + "]]]]]]]]]]";
    assertEquals(expected, ORGANIZATION.toString());
  }

}
