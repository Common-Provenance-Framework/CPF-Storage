package org.commonprovenance.framework.store.persistence.finalizedProvComponent.model.node;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TrustedPartyNodeTest {
  private final TrustedPartyNode TRUSTED_PARTY = new TrustedPartyNode("name", "cert", true, true, true);

  @Test
  void withNamePreserveValuesAndRelationships() {
    String newName = "new_name";
    TrustedPartyNode node = TRUSTED_PARTY.withName(newName);

    assertEquals(newName, node.name());
    assertEquals(TRUSTED_PARTY.clientCertificate(), node.clientCertificate());
    assertEquals(TRUSTED_PARTY.url(), node.url());
    assertEquals(TRUSTED_PARTY.isChecked(), node.isChecked());
    assertEquals(TRUSTED_PARTY.isValid(), node.isValid());
    assertEquals(TRUSTED_PARTY.isDefault(), node.isDefault());
  }

  @Test
  void withClientCertificatePreserveValuesAndRelationships() {
    String newCert = "new_cert";
    TrustedPartyNode node = TRUSTED_PARTY.withClientCertificate(newCert);

    assertEquals(TRUSTED_PARTY.name(), node.name());
    assertEquals(newCert, node.clientCertificate());
    assertEquals(TRUSTED_PARTY.url(), node.url());
    assertEquals(TRUSTED_PARTY.isChecked(), node.isChecked());
    assertEquals(TRUSTED_PARTY.isValid(), node.isValid());
    assertEquals(TRUSTED_PARTY.isDefault(), node.isDefault());
  }

  @Test
  void withUrlPreserveValuesAndRelationships() {
    String newUrl = "new_url";
    TrustedPartyNode node = TRUSTED_PARTY.withUrl(newUrl);

    assertEquals(TRUSTED_PARTY.name(), node.name());
    assertEquals(TRUSTED_PARTY.clientCertificate(), node.clientCertificate());
    assertEquals(newUrl, node.url());
    assertEquals(TRUSTED_PARTY.isChecked(), node.isChecked());
    assertEquals(TRUSTED_PARTY.isValid(), node.isValid());
    assertEquals(TRUSTED_PARTY.isDefault(), node.isDefault());
  }

  @Test
  void withIsCheckedPreserveValuesAndRelationships() {
    Boolean is = false;
    TrustedPartyNode node = TRUSTED_PARTY.withIsChecked(is);

    assertEquals(TRUSTED_PARTY.name(), node.name());
    assertEquals(TRUSTED_PARTY.clientCertificate(), node.clientCertificate());
    assertEquals(TRUSTED_PARTY.url(), node.url());
    assertEquals(is, node.isChecked());
    assertEquals(TRUSTED_PARTY.isValid(), node.isValid());
    assertEquals(TRUSTED_PARTY.isDefault(), node.isDefault());
  }

  @Test
  void withIsValidPreserveValuesAndRelationships() {
    Boolean is = false;
    TrustedPartyNode node = TRUSTED_PARTY.withIsValid(is);

    assertEquals(TRUSTED_PARTY.name(), node.name());
    assertEquals(TRUSTED_PARTY.clientCertificate(), node.clientCertificate());
    assertEquals(TRUSTED_PARTY.url(), node.url());
    assertEquals(TRUSTED_PARTY.isChecked(), node.isChecked());
    assertEquals(is, node.isValid());
    assertEquals(TRUSTED_PARTY.isDefault(), node.isDefault());
  }

  @Test
  void withIsDefaultPreserveValuesAndRelationships() {
    Boolean is = false;
    TrustedPartyNode node = TRUSTED_PARTY.withIsDefault(is);

    assertEquals(TRUSTED_PARTY.name(), node.name());
    assertEquals(TRUSTED_PARTY.clientCertificate(), node.clientCertificate());
    assertEquals(TRUSTED_PARTY.url(), node.url());
    assertEquals(TRUSTED_PARTY.isChecked(), node.isChecked());
    assertEquals(TRUSTED_PARTY.isValid(), node.isValid());
    assertEquals(is, node.isDefault());
  }

  @Test
  void toStringExactString() {
    String expected = "TrustedPartyNode [id=null, name=name, clientCertificate=cert, url=null, isChecked=true, isValid=true, isDefault=true]";
    assertEquals(expected, TRUSTED_PARTY.toString());
  }
}
