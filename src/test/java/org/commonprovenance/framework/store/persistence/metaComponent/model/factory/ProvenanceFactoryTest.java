package org.commonprovenance.framework.store.persistence.metaComponent.model.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Agent;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.LangString;
import org.openprovenance.prov.model.NamespacePrefixMapper;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.SpecializationOf;
import org.openprovenance.prov.model.Statement;
import org.openprovenance.prov.model.Type;
import org.openprovenance.prov.model.Used;
import org.openprovenance.prov.model.WasAssociatedWith;
import org.openprovenance.prov.model.WasAttributedTo;
import org.openprovenance.prov.model.WasDerivedFrom;

import cz.muni.fi.cpm.constants.CpmNamespaceConstants;

class ProvenanceFactoryTest {
  private final ProvFactory provFactory = new org.openprovenance.prov.vanilla.ProvFactory();

  @Test
  void toProv_shouldCreateBundleWithNodesAndRelations() {
    AppConfiguration config = mock(AppConfiguration.class);
    when(config.getFqdn()).thenReturn("http://localhost:8080/api/v1/");

    EntityNode e2 = new EntityNode("e2", "cpm:token", Map.of("originatorId", "ORG1", "authorityId", "Trusted_Party"),
        Map.of());
    AgentNode ag1 = new AgentNode("ag1", "cpm:trustedParty", Map.of("trustedPartyUri", "trusted-party:8020"));
    EntityNode e1 = new EntityNode("e1", "prov:Bundle", Map.of(), Map.of("version", 1))
        .wihtRevisionOfEntity(e2)
        .withWasAttributedToAgent(ag1);
    ActivityNode act1 = new ActivityNode("act1", "cpm:tokenGeneration", "2024-01-01T10:15:30Z", "2024-01-01T10:16:30Z",
        Map.of())
        .withUsedEntity(e1)
        .withWasAssociatedWithAgent(ag1);

    BundleNode bundleNode = new BundleNode("bundle-1")
        .withActivities(List.of(act1))
        .withAgents(List.of(ag1))
        .withEntities(List.of(e1, e2));

    Document document = ProvenanceFactory.bundleToProv(config).apply(bundleNode).block();

    assertNotNull(document);
    assertEquals(1, document.getStatementOrBundle().size());

    Bundle bundle = assertInstanceOf(Bundle.class,
        document.getStatementOrBundle().getFirst());
    List<Statement> statements = bundle.getStatement();

    assertEquals(8, statements.size());

    List<Entity> entities = statements.stream()
        .filter(Entity.class::isInstance)
        .map(Entity.class::cast)
        .toList();
    assertEquals(2, entities.size());

    Optional<Entity> entity1 = entities.stream()
        .filter(e -> e.getId().getLocalPart().equals(e1.getIdentifier()))
        .findFirst();
    assertTrue(entity1.isPresent());

    assertEquals(1, entity1.get().getType().size());
    assertTrue(entity1.get().getType().contains(provFactory.newType(
        provFactory.getName().PROV_BUNDLE,
        provFactory.getName().PROV_QUALIFIED_NAME)));

    assertEquals(1, entity1.get().getOther().size());
    assertTrue(entity1.get().getOther().contains(provFactory.newOther(
        provFactory.newQualifiedName("http://purl.org/pav/", "version", "pav"),
        1,
        provFactory.getName().XSD_INT)));
    assertEquals(0, entity1.get().getLabel().size());
    assertEquals(0, entity1.get().getLocation().size());
    assertNull(entity1.get().getValue());

    Optional<Entity> entity2 = entities.stream()
        .filter(e -> e.getId().getLocalPart().equals(e2.getIdentifier()))
        .findFirst();
    assertTrue(entity2.isPresent());

    assertEquals(1, entity2.get().getType().size());
    assertTrue(entity2.get().getType().contains(provFactory.newType(
        provFactory.newQualifiedName(CpmNamespaceConstants.CPM_NS, "token", CpmNamespaceConstants.CPM_PREFIX),
        provFactory.getName().PROV_QUALIFIED_NAME)));
    assertEquals(2, entity2.get().getOther().size());
    assertTrue(entity2.get().getOther().contains(provFactory.newOther(
        provFactory.newQualifiedName(CpmNamespaceConstants.CPM_NS, "originatorId", CpmNamespaceConstants.CPM_PREFIX),
        "ORG1",
        provFactory.getName().XSD_STRING)));
    assertTrue(entity2.get().getOther().contains(provFactory.newOther(
        provFactory.newQualifiedName(CpmNamespaceConstants.CPM_NS, "authorityId", CpmNamespaceConstants.CPM_PREFIX),
        "Trusted_Party",
        provFactory.getName().XSD_STRING)));
    assertEquals(0, entity2.get().getLabel().size());
    assertEquals(0, entity2.get().getLocation().size());
    assertNull(entity2.get().getValue());

    List<Agent> agents = statements.stream()
        .filter(Agent.class::isInstance)
        .map(Agent.class::cast)
        .toList();
    assertEquals(1, agents.size());

    Optional<Agent> agent1 = agents.stream()
        .filter(a -> a.getId().getLocalPart().equals(ag1.getIdentifier()))
        .findFirst();
    assertTrue(agent1.isPresent());
    assertEquals(1, agent1.get().getType().size());
    assertTrue(agent1.get().getType().contains(provFactory.newType(
        provFactory.newQualifiedName(CpmNamespaceConstants.CPM_NS, "trustedParty", CpmNamespaceConstants.CPM_PREFIX),
        provFactory.getName().PROV_QUALIFIED_NAME)));
    assertEquals(1, agent1.get().getOther().size());
    assertTrue(agent1.get().getOther().contains(provFactory.newOther(
        provFactory.newQualifiedName(CpmNamespaceConstants.CPM_NS, "trustedPartyUri", CpmNamespaceConstants.CPM_PREFIX),
        "trusted-party:8020",
        provFactory.getName().XSD_STRING)));

    List<Activity> activities = statements.stream()
        .filter(Activity.class::isInstance)
        .map(Activity.class::cast)
        .toList();
    assertEquals(1, activities.size());

    Optional<Activity> activity1 = activities.stream()
        .filter(a -> a.getId().getLocalPart().equals(act1.getIdentifier()))
        .findFirst();
    assertTrue(activity1.isPresent());
    assertEquals(1, activity1.get().getType().size());

    assertTrue(activity1.get().getType().contains(provFactory.newType(
        provFactory.newQualifiedName(CpmNamespaceConstants.CPM_NS, "tokenGeneration", CpmNamespaceConstants.CPM_PREFIX),
        provFactory.getName().PROV_QUALIFIED_NAME)));
    assertEquals(0, activity1.get().getOther().size());
    assertEquals(0, activity1.get().getLabel().size());
    assertEquals(0, activity1.get().getLocation().size());
    assertEquals("2024-01-01T10:15:30Z", activity1.get().getStartTime().toString());
    assertEquals("2024-01-01T10:16:30Z", activity1.get().getEndTime().toString());
    // ----
    List<Used> usedRelation = statements.stream()
        .filter(Used.class::isInstance)
        .map(Used.class::cast)
        .toList();

    assertEquals(1, usedRelation.size());
    assertEquals(e1.getIdentifier(), usedRelation.getFirst().getEntity().getLocalPart());
    assertEquals(act1.getIdentifier(), usedRelation.getFirst().getActivity().getLocalPart());
    assertEquals(0, usedRelation.getFirst().getType().size());

    // ----
    List<WasAssociatedWith> wawRelation = statements.stream()
        .filter(WasAssociatedWith.class::isInstance)
        .map(WasAssociatedWith.class::cast)
        .toList();

    assertEquals(1, wawRelation.size());
    assertEquals(act1.getIdentifier(), wawRelation.getFirst().getActivity().getLocalPart());
    assertEquals(ag1.getIdentifier(), wawRelation.getFirst().getAgent().getLocalPart());
    assertEquals(0, wawRelation.getFirst().getType().size());
    // ----
    List<WasDerivedFrom> derivedRelation = statements.stream()
        .filter(WasDerivedFrom.class::isInstance)
        .map(WasDerivedFrom.class::cast)
        .toList();

    assertEquals(1, derivedRelation.size());

    assertEquals(e1.getIdentifier(), derivedRelation.getFirst().getGeneratedEntity().getLocalPart());
    assertEquals(e2.getIdentifier(), derivedRelation.getFirst().getUsedEntity().getLocalPart());

    assertEquals(1, derivedRelation.getFirst().getType().size());
    assertTrue(derivedRelation.getFirst().getType().contains(provFactory.newType(
        provFactory.getName().PROV_REVISION,
        provFactory.getName().PROV_QUALIFIED_NAME)));
    // ----
    List<SpecializationOf> specializationOfRelation = statements.stream()
        .filter(SpecializationOf.class::isInstance)
        .map(SpecializationOf.class::cast)
        .toList();

    assertEquals(0, specializationOfRelation.size());
    // ----
    List<WasAttributedTo> watRelation = statements.stream()
        .filter(WasAttributedTo.class::isInstance)
        .map(WasAttributedTo.class::cast)
        .toList();

    assertEquals(1, watRelation.size());
    assertEquals(e1.getIdentifier(), watRelation.getFirst().getEntity().getLocalPart());
    assertEquals(ag1.getIdentifier(), watRelation.getFirst().getAgent().getLocalPart());

    assertEquals(0, watRelation.getFirst().getType().size());
  }

  @Test
  void toProv_shouldMarkRevisionRelationWithProvRevisionType() {
    AppConfiguration config = mock(AppConfiguration.class);
    when(config.getFqdn()).thenReturn("http://localhost:8080/api/v1/");

    EntityNode e2 = new EntityNode("e2", "prov:Bundle", Map.of(), Map.of());
    EntityNode e1 = new EntityNode("e1", "prov:Bundle", Map.of(), Map.of("version", 1))
        .wihtRevisionOfEntity(e2);

    BundleNode bundleNode = new BundleNode("bundle-1")
        .withEntities(List.of(e1, e2));

    Document document = ProvenanceFactory.bundleToProv(config).apply(bundleNode).block();
    assertNotNull(document);

    Bundle bundle = assertInstanceOf(Bundle.class,
        document.getStatementOrBundle().getFirst());
    WasDerivedFrom relation = bundle.getStatement().stream()
        .filter(WasDerivedFrom.class::isInstance)
        .map(WasDerivedFrom.class::cast)
        .findFirst()
        .orElseThrow();

    boolean hasProvRevisionType = relation.getType().stream()
        .map(Type::getValue)
        .filter(QualifiedName.class::isInstance)
        .map(QualifiedName.class::cast)
        .anyMatch(qn -> qn.equals(new org.openprovenance.prov.vanilla.ProvFactory().getName().PROV_REVISION));

    assertTrue(hasProvRevisionType);
  }

  @Test
  void toProv_shouldParseProvidedCpmAttributesJsonCorrectly() {
    AppConfiguration config = mock(AppConfiguration.class);
    when(config.getFqdn()).thenReturn("http://localhost:8080/api/v1/");

    String cert = """
        -----BEGIN CERTIFICATE-----
        MIICMjCCAdigAwIBAgIUSLj5Y7PXIS13qPEPDdlINBnQzogwCgYIKoZIzj0EAwIw
        bTELMAkGA1UEBhMCRVUxOjA4BgNVBAoMMURpc3RyaWJ1dGVkIFByb3ZlbmFuY2Ug
        RGVtbyBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkxIjAgBgNVBAMMGURQRCBDZXJ0aWZp
        Y2F0ZSBBdXRob3JpdHkwHhcNMjQxMTE2MDI1OTUyWhcNMzQxMTE0MDI1OTUyWjBd
        MQswCQYDVQQGEwJDWjEyMDAGA1UECgwpRGlzdHJpYnV0ZWQgUHJvdmVuYW5jZSBE
        ZW1vIFRydXN0ZWQgUGFydHkxGjAYBgNVBAMMEURQRCBUcnVzdGVkIFBhcnR5MFkw
        EwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE+V8kT4jkvEWmX301KAS9eklmnRNi6gU9
        +KHxuQpkSOhMTq96CBXFpfokRd7t5VdrRy0uqZsySNp5kW0hnQMJWaNmMGQwEgYD
        VR0TAQH/BAgwBgEB/wIBADAOBgNVHQ8BAf8EBAMCAYYwHQYDVR0OBBYEFMCnPRji
        XokT7quwZRB16AAgz7bnMB8GA1UdIwQYMBaAFCyEKwi1jvdPqfiU+NdH/nvh7PYZ
        MAoGCCqGSM49BAMCA0gAMEUCIQCyZrUShVqrohDqdzdOFmAyFDpwMAO8I6jahvg1
        FRAZYgIgVh4S2tQn12XYdd5ISsCpABsh6ZrjSiVYrt2T1O1nQsw=
        -----END CERTIFICATE-----
          """;

    EntityNode entityNode = new EntityNode(
        "entity-1",
        "cpm:token",
        Map.of(
            "originatorId", "ORG1",
            "authorityId", "Trusted_Party",
            "tokenTimestamp", 1771176308,
            "documentCreationTimestamp", 1771176307,
            "documentDigest", "7c102e3408bedfcd572fa4576fcac02f30fe601ca215c02a0705723432023492",
            "bundle", "http://prov-storage-1:8000/api/v1/organizations/ORG1/documents/SamplingBundle_V1",
            "hashFunction", "SHA256",
            "trustedPartyUri", "trusted-party:8020",
            "trustedPartyCertificate", cert,
            "signature",
            "MEYCIQCZGVGDTGLmSdp9IZcXfEbs/iJq2wKC11Oabtne8Yo4zQIhAI3rrCE8+++KDmJTg4Ol5QsPLZiVMunm1oFIGr4bb7S7"),
        Map.of()

    );
    BundleNode bundleNode = new BundleNode("bundle-1")
        .withEntities(List.of(entityNode));

    Document document = ProvenanceFactory.bundleToProv(config).apply(bundleNode).block();
    assertNotNull(document);

    Bundle bundle = assertInstanceOf(Bundle.class,
        document.getStatementOrBundle().getFirst());
    org.openprovenance.prov.model.Entity entity = bundle.getStatement().stream()
        .filter(org.openprovenance.prov.model.Entity.class::isInstance)
        .map(org.openprovenance.prov.model.Entity.class::cast)
        .findFirst()
        .orElseThrow();

    // Check Type
    assertEquals(1, entity.getType().size());
    QualifiedName type = assertInstanceOf(QualifiedName.class,
        entity.getType().getFirst().getValue());
    assertEquals(CpmNamespaceConstants.CPM_PREFIX, type.getPrefix());
    assertEquals("token", type.getLocalPart());
    assertEquals(CpmNamespaceConstants.CPM_NS, type.getNamespaceURI());

    entity.getOther().stream()
        .forEach(other -> {
          QualifiedName elementName = other.getElementName();
          if (elementName.getLocalPart().equals("originatorId")) {
            assertEquals(CpmNamespaceConstants.CPM_PREFIX, elementName.getPrefix());
            assertEquals(CpmNamespaceConstants.CPM_NS, elementName.getNamespaceURI());

            QualifiedName attrType = assertInstanceOf(QualifiedName.class,
                other.getType());
            assertEquals(NamespacePrefixMapper.XSD_PREFIX, attrType.getPrefix());
            assertEquals(NamespacePrefixMapper.XSD_NS, attrType.getNamespaceURI());
            assertEquals("string", attrType.getLocalPart());

            LangString value = assertInstanceOf(LangString.class, other.getValue());
            assertEquals("ORG1", value.getValue());
            assertNull(value.getLang());
          } else if (elementName.getLocalPart().equals("authorityId")) {
            assertEquals(CpmNamespaceConstants.CPM_PREFIX, elementName.getPrefix());
            assertEquals(CpmNamespaceConstants.CPM_NS, elementName.getNamespaceURI());

            QualifiedName attrType = assertInstanceOf(QualifiedName.class,
                other.getType());
            assertEquals(NamespacePrefixMapper.XSD_PREFIX, attrType.getPrefix());
            assertEquals(NamespacePrefixMapper.XSD_NS, attrType.getNamespaceURI());
            assertEquals("string", attrType.getLocalPart());

            LangString value = assertInstanceOf(LangString.class, other.getValue());
            assertEquals("Trusted_Party", value.getValue());
            assertNull(value.getLang());
          } else if (elementName.getLocalPart().equals("tokenTimestamp")) {
            assertEquals(CpmNamespaceConstants.CPM_PREFIX, elementName.getPrefix());
            assertEquals(CpmNamespaceConstants.CPM_NS, elementName.getNamespaceURI());

            QualifiedName attrType = assertInstanceOf(QualifiedName.class,
                other.getType());
            assertEquals(NamespacePrefixMapper.XSD_PREFIX, attrType.getPrefix());
            assertEquals(NamespacePrefixMapper.XSD_NS, attrType.getNamespaceURI());
            assertEquals("int", attrType.getLocalPart());

            String value = assertInstanceOf(String.class, other.getValue());
            assertEquals("1771176308", value);
          } else if (elementName.getLocalPart().equals("documentCreationTimestamp")) {
            assertEquals(CpmNamespaceConstants.CPM_PREFIX, elementName.getPrefix());
            assertEquals(CpmNamespaceConstants.CPM_NS, elementName.getNamespaceURI());

            QualifiedName attrType = assertInstanceOf(QualifiedName.class,
                other.getType());
            assertEquals(NamespacePrefixMapper.XSD_PREFIX, attrType.getPrefix());
            assertEquals(NamespacePrefixMapper.XSD_NS, attrType.getNamespaceURI());
            assertEquals("int", attrType.getLocalPart());

            String value = assertInstanceOf(String.class, other.getValue());
            assertEquals("1771176307", value);
          } else if (elementName.getLocalPart().equals("documentDigest")) {
            assertEquals(CpmNamespaceConstants.CPM_PREFIX, elementName.getPrefix());
            assertEquals(CpmNamespaceConstants.CPM_NS, elementName.getNamespaceURI());

            QualifiedName attrType = assertInstanceOf(QualifiedName.class,
                other.getType());
            assertEquals(NamespacePrefixMapper.XSD_PREFIX, attrType.getPrefix());
            assertEquals(NamespacePrefixMapper.XSD_NS, attrType.getNamespaceURI());
            assertEquals("string", attrType.getLocalPart());

            LangString value = assertInstanceOf(LangString.class, other.getValue());
            assertEquals("7c102e3408bedfcd572fa4576fcac02f30fe601ca215c02a0705723432023492",
                value.getValue());
            assertNull(value.getLang());
          } else if (elementName.getLocalPart().equals("bundle")) {
            assertEquals(CpmNamespaceConstants.CPM_PREFIX, elementName.getPrefix());
            assertEquals(CpmNamespaceConstants.CPM_NS, elementName.getNamespaceURI());

            QualifiedName attrType = assertInstanceOf(QualifiedName.class,
                other.getType());
            assertEquals(NamespacePrefixMapper.XSD_PREFIX, attrType.getPrefix());
            assertEquals(NamespacePrefixMapper.XSD_NS, attrType.getNamespaceURI());
            assertEquals("string", attrType.getLocalPart());

            LangString value = assertInstanceOf(LangString.class, other.getValue());
            assertEquals("http://prov-storage-1:8000/api/v1/organizations/ORG1/documents/SamplingBundle_V1",
                value.getValue());
            assertNull(value.getLang());
          } else if (elementName.getLocalPart().equals("hashFunction")) {
            assertEquals(CpmNamespaceConstants.CPM_PREFIX, elementName.getPrefix());
            assertEquals(CpmNamespaceConstants.CPM_NS, elementName.getNamespaceURI());

            QualifiedName attrType = assertInstanceOf(QualifiedName.class,
                other.getType());
            assertEquals(NamespacePrefixMapper.XSD_PREFIX, attrType.getPrefix());
            assertEquals(NamespacePrefixMapper.XSD_NS, attrType.getNamespaceURI());
            assertEquals("string", attrType.getLocalPart());

            LangString value = assertInstanceOf(LangString.class, other.getValue());
            assertEquals("SHA256",
                value.getValue());
            assertNull(value.getLang());
          } else if (elementName.getLocalPart().equals("trustedPartyUri")) {
            assertEquals(CpmNamespaceConstants.CPM_PREFIX, elementName.getPrefix());
            assertEquals(CpmNamespaceConstants.CPM_NS, elementName.getNamespaceURI());

            QualifiedName attrType = assertInstanceOf(QualifiedName.class,
                other.getType());
            assertEquals(NamespacePrefixMapper.XSD_PREFIX, attrType.getPrefix());
            assertEquals(NamespacePrefixMapper.XSD_NS, attrType.getNamespaceURI());
            assertEquals("string", attrType.getLocalPart());

            LangString value = assertInstanceOf(LangString.class, other.getValue());
            assertEquals("trusted-party:8020",
                value.getValue());
            assertNull(value.getLang());
          } else if (elementName.getLocalPart().equals("trustedPartyCertificate")) {
            assertEquals(CpmNamespaceConstants.CPM_PREFIX, elementName.getPrefix());
            assertEquals(CpmNamespaceConstants.CPM_NS, elementName.getNamespaceURI());

            QualifiedName attrType = assertInstanceOf(QualifiedName.class,
                other.getType());
            assertEquals(NamespacePrefixMapper.XSD_PREFIX, attrType.getPrefix());
            assertEquals(NamespacePrefixMapper.XSD_NS, attrType.getNamespaceURI());
            assertEquals("string", attrType.getLocalPart());

            LangString value = assertInstanceOf(LangString.class, other.getValue());
            assertEquals(cert, value.getValue());
            assertNull(value.getLang());
          } else if (elementName.getLocalPart().equals("signature")) {
            assertEquals(CpmNamespaceConstants.CPM_PREFIX, elementName.getPrefix());
            assertEquals(CpmNamespaceConstants.CPM_NS, elementName.getNamespaceURI());

            QualifiedName attrType = assertInstanceOf(QualifiedName.class,
                other.getType());
            assertEquals(NamespacePrefixMapper.XSD_PREFIX, attrType.getPrefix());
            assertEquals(NamespacePrefixMapper.XSD_NS, attrType.getNamespaceURI());
            assertEquals("string", attrType.getLocalPart());

            LangString value = assertInstanceOf(LangString.class, other.getValue());
            assertEquals(
                "MEYCIQCZGVGDTGLmSdp9IZcXfEbs/iJq2wKC11Oabtne8Yo4zQIhAI3rrCE8+++KDmJTg4Ol5QsPLZiVMunm1oFIGr4bb7S7",
                value.getValue());
            assertNull(value.getLang());
          }
        });

  }

}
