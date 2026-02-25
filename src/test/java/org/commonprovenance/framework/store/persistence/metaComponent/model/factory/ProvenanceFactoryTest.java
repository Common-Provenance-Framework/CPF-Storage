package org.commonprovenance.framework.store.persistence.metaComponent.model.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.LangString;
import org.openprovenance.prov.model.Namespace;
import org.openprovenance.prov.model.NamespacePrefixMapper;
import org.openprovenance.prov.model.Other;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.Statement;
import org.openprovenance.prov.model.Type;
import org.openprovenance.prov.model.WasDerivedFrom;

import cz.muni.fi.cpm.constants.CpmNamespaceConstants;

class ProvenanceFactoryTest {

  private static Method toValueMethod;

  @BeforeAll
  static void setUpReflection() throws Exception {
    toValueMethod = ProvenanceFactory.class.getDeclaredMethod("toValue", String.class, Namespace.class);
    toValueMethod.setAccessible(true);
  }

  @Test
  void toValue_shouldReturnQualifiedName_whenValueContainsColonAndKnownPrefix() throws Exception {
    Namespace ns = new Namespace();
    ns.register("ex", "http://example.com/");

    Object result = toValueMethod.invoke(null, "ex:item", ns);

    QualifiedName qn = assertInstanceOf(QualifiedName.class, result);
    assertEquals("http://example.com/", qn.getNamespaceURI());
    assertEquals("item", qn.getLocalPart());
    assertEquals("ex", qn.getPrefix());
  }

  @Test
  void toValue_shouldReturnXsdStringQualifiedName_whenValueContainsColonAndUnknownPrefix() throws Exception {
    Namespace ns = new Namespace();
    ns.register("ex", "http://example.com/");

    Object result = toValueMethod.invoke(null, "unknown:item", ns);

    QualifiedName qn = assertInstanceOf(QualifiedName.class, result);
    assertEquals("xsd", qn.getPrefix());
    assertEquals("string", qn.getLocalPart());
  }

  @Test
  void toValue_shouldReturnLangString_whenValueContainsAtAndNoColon() throws Exception {
    Namespace ns = new Namespace();

    Object result = toValueMethod.invoke(null, "hello@en", ns);

    LangString langString = assertInstanceOf(LangString.class, result);
    assertEquals("hello", langString.getValue());
    assertEquals("en", langString.getLang());
  }

  @Test
  void toValue_shouldReturnRawString_whenValueContainsNeitherColonNorAt() throws Exception {
    Namespace ns = new Namespace();

    Object result = toValueMethod.invoke(null, "plainValue", ns);

    String value = assertInstanceOf(String.class, result);
    assertEquals("plainValue", value);
  }

  @Test
  void toProv_shouldCreateBundleWithNodesAndRelations() {
    AppConfiguration config = mock(AppConfiguration.class);
    when(config.getFqdn()).thenReturn("http://localhost:8080/api/v1/");

    EntityNode e2 = new EntityNode("e2", "{}");
    AgentNode ag1 = new AgentNode("ag1", "{}");
    EntityNode e1 = new EntityNode("e1", "{}")
        .wihtRevisionOfEntity(e2)
        .withWasAttributedToAgent(ag1);
    ActivityNode act1 = new ActivityNode("act1", "2024-01-01T10:15:30Z", "2024-01-01T10:16:30Z", "{}")
        .withUsedEntity(e1)
        .withWasAssociatedWithAgent(ag1);

    BundleNode bundleNode = new BundleNode("bundle-1", List.of(e1, e2), List.of(ag1), List.of(act1));

    Document document = ProvenanceFactory.toProv(config).apply(bundleNode).block();

    assertNotNull(document);
    assertEquals(1, document.getStatementOrBundle().size());

    Bundle bundle = assertInstanceOf(Bundle.class, document.getStatementOrBundle().getFirst());
    List<Statement> statements = bundle.getStatement();

    assertEquals(8, statements.size());
    assertEquals(2, statements.stream().filter(org.openprovenance.prov.model.Entity.class::isInstance).count());
    assertEquals(1, statements.stream().filter(org.openprovenance.prov.model.Agent.class::isInstance).count());
    assertEquals(1, statements.stream().filter(org.openprovenance.prov.model.Activity.class::isInstance).count());
    assertEquals(1, statements.stream().filter(org.openprovenance.prov.model.Used.class::isInstance).count());
    assertEquals(1,
        statements.stream().filter(org.openprovenance.prov.model.WasAssociatedWith.class::isInstance).count());
    assertEquals(1,
        statements.stream().filter(org.openprovenance.prov.model.WasAttributedTo.class::isInstance).count());
    assertEquals(1, statements.stream().filter(org.openprovenance.prov.model.WasDerivedFrom.class::isInstance).count());
  }

  @Test
  void toProv_shouldMarkRevisionRelationWithProvRevisionType() {
    AppConfiguration config = mock(AppConfiguration.class);
    when(config.getFqdn()).thenReturn("http://localhost:8080/api/v1/");

    EntityNode e2 = new EntityNode("e2", "{}");
    EntityNode e1 = new EntityNode("e1", "{}")
        .wihtRevisionOfEntity(e2);

    BundleNode bundleNode = new BundleNode("bundle-1", List.of(e1, e2), List.of(), List.of());

    Document document = ProvenanceFactory.toProv(config).apply(bundleNode).block();
    assertNotNull(document);

    Bundle bundle = assertInstanceOf(Bundle.class, document.getStatementOrBundle().getFirst());
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
  void toProv_shouldThrowWhenAttributesJsonIsMalformed() {
    AppConfiguration config = mock(AppConfiguration.class);
    when(config.getFqdn()).thenReturn("http://localhost:8080/api/v1/");

    EntityNode malformed = new EntityNode("e1", "{not-valid-json");
    BundleNode bundleNode = new BundleNode("bundle-1", List.of(malformed), List.of(), List.of());

    RuntimeException ex = assertThrows(RuntimeException.class,
        () -> ProvenanceFactory.toProv(config).apply(bundleNode));

    assertTrue(ex.getMessage().contains("Cannot parse entity attributes JSON"));
  }

  @Test
  void toProv_shouldParseProvidedCpmAttributesJsonCorrectly() {
    AppConfiguration config = mock(AppConfiguration.class);
    when(config.getFqdn()).thenReturn("http://localhost:8080/api/v1/");

    String attributesJson = """
        {"cpm:originatorId": "ORG1", "cpm:authorityId": "Trusted_Party", "cpm:tokenTimestamp": 1771176308, "cpm:documentCreationTimestamp": 1771176307, "cpm:documentDigest": "7c102e3408bedfcd572fa4576fcac02f30fe601ca215c02a0705723432023492", "cpm:bundle": "http://prov-storage-1:8000/api/v1/organizations/ORG1/documents/SamplingBundle_V1", "cpm:hashFunction": "SHA256", "cpm:trustedPartyUri": "trusted-party:8020", "cpm:trustedPartyCertificate": "-----BEGIN CERTIFICATE-----\\nMIICMjCCAdigAwIBAgIUSLj5Y7PXIS13qPEPDdlINBnQzogwCgYIKoZIzj0EAwIw\\nbTELMAkGA1UEBhMCRVUxOjA4BgNVBAoMMURpc3RyaWJ1dGVkIFByb3ZlbmFuY2Ug\\nRGVtbyBDZXJ0aWZpY2F0ZSBBdXRob3JpdHkxIjAgBgNVBAMMGURQRCBDZXJ0aWZp\\nY2F0ZSBBdXRob3JpdHkwHhcNMjQxMTE2MDI1OTUyWhcNMzQxMTE0MDI1OTUyWjBd\\nMQswCQYDVQQGEwJDWjEyMDAGA1UECgwpRGlzdHJpYnV0ZWQgUHJvdmVuYW5jZSBE\\nZW1vIFRydXN0ZWQgUGFydHkxGjAYBgNVBAMMEURQRCBUcnVzdGVkIFBhcnR5MFkw\\nEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE+V8kT4jkvEWmX301KAS9eklmnRNi6gU9\\n+KHxuQpkSOhMTq96CBXFpfokRd7t5VdrRy0uqZsySNp5kW0hnQMJWaNmMGQwEgYD\\nVR0TAQH/BAgwBgEB/wIBADAOBgNVHQ8BAf8EBAMCAYYwHQYDVR0OBBYEFMCnPRji\\nXokT7quwZRB16AAgz7bnMB8GA1UdIwQYMBaAFCyEKwi1jvdPqfiU+NdH/nvh7PYZ\\nMAoGCCqGSM49BAMCA0gAMEUCIQCyZrUShVqrohDqdzdOFmAyFDpwMAO8I6jahvg1\\nFRAZYgIgVh4S2tQn12XYdd5ISsCpABsh6ZrjSiVYrt2T1O1nQsw=\\n-----END CERTIFICATE-----\\n", "cpm:signature": "MEYCIQCZGVGDTGLmSdp9IZcXfEbs/iJq2wKC11Oabtne8Yo4zQIhAI3rrCE8+++KDmJTg4Ol5QsPLZiVMunm1oFIGr4bb7S7", "prov:type": "cpm:token"}
        """;

    EntityNode entityNode = new EntityNode("entity-1", attributesJson);
    BundleNode bundleNode = new BundleNode("bundle-1", List.of(entityNode), List.of(), List.of());

    Document document = ProvenanceFactory.toProv(config).apply(bundleNode).block();
    assertNotNull(document);

    Bundle bundle = assertInstanceOf(Bundle.class, document.getStatementOrBundle().getFirst());
    org.openprovenance.prov.model.Entity entity = bundle.getStatement().stream()
        .filter(org.openprovenance.prov.model.Entity.class::isInstance)
        .map(org.openprovenance.prov.model.Entity.class::cast)
        .findFirst()
        .orElseThrow();

    // Check Type
    assertEquals(1, entity.getType().size());
    QualifiedName type = assertInstanceOf(QualifiedName.class, entity.getType().getFirst().getValue());
    assertEquals(CpmNamespaceConstants.CPM_PREFIX, type.getPrefix());
    assertEquals("token", type.getLocalPart());
    assertEquals(CpmNamespaceConstants.CPM_NS, type.getNamespaceURI());

    entity.getOther().stream()
        .forEach(other -> {
          QualifiedName elementName = other.getElementName();
          if (elementName.getLocalPart().equals("originatorId")) {
            assertEquals(CpmNamespaceConstants.CPM_PREFIX, elementName.getPrefix());
            assertEquals(CpmNamespaceConstants.CPM_NS, elementName.getNamespaceURI());

            QualifiedName attrType = assertInstanceOf(QualifiedName.class, other.getType());
            assertEquals(NamespacePrefixMapper.XSD_PREFIX, attrType.getPrefix());
            assertEquals(NamespacePrefixMapper.XSD_NS, attrType.getNamespaceURI());
            assertEquals("string", attrType.getLocalPart());

            LangString value = assertInstanceOf(LangString.class, other.getValue());
            assertEquals("ORG1", value.getValue());
            assertNull(value.getLang());
          } else if (elementName.getLocalPart().equals("authorityId")) {
            assertEquals(CpmNamespaceConstants.CPM_PREFIX, elementName.getPrefix());
            assertEquals(CpmNamespaceConstants.CPM_NS, elementName.getNamespaceURI());

            QualifiedName attrType = assertInstanceOf(QualifiedName.class, other.getType());
            assertEquals(NamespacePrefixMapper.XSD_PREFIX, attrType.getPrefix());
            assertEquals(NamespacePrefixMapper.XSD_NS, attrType.getNamespaceURI());
            assertEquals("string", attrType.getLocalPart());

            LangString value = assertInstanceOf(LangString.class, other.getValue());
            assertEquals("Trusted_Party", value.getValue());
            assertNull(value.getLang());
          } else if (elementName.getLocalPart().equals("tokenTimestamp")) {
            assertEquals(CpmNamespaceConstants.CPM_PREFIX, elementName.getPrefix());
            assertEquals(CpmNamespaceConstants.CPM_NS, elementName.getNamespaceURI());

            QualifiedName attrType = assertInstanceOf(QualifiedName.class, other.getType());
            assertEquals(NamespacePrefixMapper.XSD_PREFIX, attrType.getPrefix());
            assertEquals(NamespacePrefixMapper.XSD_NS, attrType.getNamespaceURI());
            assertEquals("int", attrType.getLocalPart());

            String value = assertInstanceOf(String.class, other.getValue());
            assertEquals("1771176308", value);
          } else if (elementName.getLocalPart().equals("documentCreationTimestamp")) {
            assertEquals(CpmNamespaceConstants.CPM_PREFIX, elementName.getPrefix());
            assertEquals(CpmNamespaceConstants.CPM_NS, elementName.getNamespaceURI());

            QualifiedName attrType = assertInstanceOf(QualifiedName.class, other.getType());
            assertEquals(NamespacePrefixMapper.XSD_PREFIX, attrType.getPrefix());
            assertEquals(NamespacePrefixMapper.XSD_NS, attrType.getNamespaceURI());
            assertEquals("int", attrType.getLocalPart());

            String value = assertInstanceOf(String.class, other.getValue());
            assertEquals("1771176307", value);
          } else if (elementName.getLocalPart().equals("documentDigest")) {
            assertEquals(CpmNamespaceConstants.CPM_PREFIX, elementName.getPrefix());
            assertEquals(CpmNamespaceConstants.CPM_NS, elementName.getNamespaceURI());

            QualifiedName attrType = assertInstanceOf(QualifiedName.class, other.getType());
            assertEquals(NamespacePrefixMapper.XSD_PREFIX, attrType.getPrefix());
            assertEquals(NamespacePrefixMapper.XSD_NS, attrType.getNamespaceURI());
            assertEquals("string", attrType.getLocalPart());

            LangString value = assertInstanceOf(LangString.class, other.getValue());
            assertEquals("7c102e3408bedfcd572fa4576fcac02f30fe601ca215c02a0705723432023492", value.getValue());
            assertNull(value.getLang());
          } else if (elementName.getLocalPart().equals("bundle")) {
            assertEquals(CpmNamespaceConstants.CPM_PREFIX, elementName.getPrefix());
            assertEquals(CpmNamespaceConstants.CPM_NS, elementName.getNamespaceURI());

            QualifiedName attrType = assertInstanceOf(QualifiedName.class, other.getType());
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

            QualifiedName attrType = assertInstanceOf(QualifiedName.class, other.getType());
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

            QualifiedName attrType = assertInstanceOf(QualifiedName.class, other.getType());
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

            QualifiedName attrType = assertInstanceOf(QualifiedName.class, other.getType());
            assertEquals(NamespacePrefixMapper.XSD_PREFIX, attrType.getPrefix());
            assertEquals(NamespacePrefixMapper.XSD_NS, attrType.getNamespaceURI());
            assertEquals("string", attrType.getLocalPart());

            LangString value = assertInstanceOf(LangString.class, other.getValue());
            assertEquals("""
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
                  """,
                value.getValue());
            assertNull(value.getLang());
          } else if (elementName.getLocalPart().equals("signature")) {
            assertEquals(CpmNamespaceConstants.CPM_PREFIX, elementName.getPrefix());
            assertEquals(CpmNamespaceConstants.CPM_NS, elementName.getNamespaceURI());

            QualifiedName attrType = assertInstanceOf(QualifiedName.class, other.getType());
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
