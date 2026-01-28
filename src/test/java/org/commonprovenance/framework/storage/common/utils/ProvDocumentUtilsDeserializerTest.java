package org.commonprovenance.framework.storage.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Agent;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.LangString;
import org.openprovenance.prov.model.Location;
import org.openprovenance.prov.model.Namespace;
import org.openprovenance.prov.model.Other;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.ProvUtilities;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.Relation;
import org.openprovenance.prov.model.Role;
import org.openprovenance.prov.model.StatementOrBundle;
import org.openprovenance.prov.model.Type;
import org.openprovenance.prov.model.Value;
import org.openprovenance.prov.model.WasAssociatedWith;
import org.openprovenance.prov.model.WasAttributedTo;
import org.openprovenance.prov.model.Attribute.AttributeKind;
import org.openprovenance.prov.model.StatementOrBundle.Kind;
import org.openprovenance.prov.model.interop.Formats;
import org.openprovenance.prov.model.WasGeneratedBy;

@DisplayName("Provenance JSON Utils Test")
public class ProvDocumentUtilsDeserializerTest {
  private final ProvFactory provFactory = new org.openprovenance.prov.vanilla.ProvFactory();
  private final ProvUtilities provUtilities = new ProvUtilities();

  private final String DOCUMENT_JSON = """
      {
        "prefix": {
          "xsd": "http://www.w3.org/2001/XMLSchema#",
          "default": "https://www.default.com/",
          "ex": "https://www.example.com/",
          "prov": "http://www.w3.org/ns/prov#"
        },
        "bundle": {
          "ex:bundleA": {
            "prefix": {
              "ex": "https://www.example.com/"
            },
            "entity": {
              "entity1": {
                "prov:value": [
                  {
                    "type": "xsd:int",
                    "$": "42"
                  }
                ],
                "ex:version": [
                  {
                    "type": "xsd:int",
                    "$": "2"
                  }
                ],
                "ex:byteSize": [
                  {
                    "type": "xsd:positiveInteger",
                    "$": "1034"
                  }
                ],
                "ex:compression": [
                  {
                    "type": "xsd:double",
                    "$": "0.825"
                  }
                ],
                "prov:location": [
                  "Entity Location"
                ],
                "ex:content": [
                  {
                    "type": "xsd:base64Binary",
                    "$": "Y29udGVudCBoZXJl"
                  }
                ],
                "prov:type": [
                  "Document"
                ],
                "prov:label": [
                  {
                    "$": "Entity Label",
                    "lang": "en"
                  }
                ]
              }
            },
            "activity": {
              "ex:activity1": {
                "prov:startTime": "2025-08-16T12:00:00.000+02:00",
                "prov:endTime": "2025-08-16T13:00:00.000+02:00",
                "ex:host": [
                  "server.example.org"
                ],
                "prov:type": [
                  {
                    "type": "xsd:QName",
                    "$": "ex:edit"
                  }
                ]
              }
            },
            "agent": {
              "ex:agent1": {
                "ex:employee": [
                  {
                    "type": "xsd:int",
                    "$": "1234"
                  }
                ],
                "ex:name": [
                  "Alice"
                ],
                "prov:type": [
                  {
                    "type": "xsd:QName",
                    "$": "prov:Person"
                  }
                ]
              }
            },
            "wasAssociatedWith": {
              "_:n1": {
                "prov:activity": "ex:activity1",
                "prov:agent": "ex:agent1",
                "prov:plan": "ex:rec-advance",
                "prov:role": [
                  "editor"
                ]
              }
            },
            "wasAttributedTo": {
              "_:n0": {
                "prov:entity": "entity1",
                "prov:agent": "ex:agent1"
              }
            },
            "wasGeneratedBy": {
              "_:n2": {
                "prov:entity": "entity1",
                "prov:activity": "ex:activity1",
                "prov:time": "2025-08-16T13:00:00.000+02:00"
              }
            }
          }
        }
      }
       """;;

  private Document provDoc;

  private void testInit() {
    try {
      if (this.provDoc == null)
        this.provDoc = ProvDocumentUtils.deserialize(this.DOCUMENT_JSON, Formats.ProvFormat.JSON);
    } catch (Exception e) {
      fail(e.getMessage(), e.getCause());
    }
  }

  @Test
  @DisplayName("should have exact 3 namespaces - Document - Deserializer")
  public void should_have_exact_3_namespaces() {
    this.testInit();

    Map<String, String> nsPrefixes = this.provDoc.getNamespace().getPrefixes();

    assertEquals(3, nsPrefixes.size(), "should have 3 namespaces");

    assertTrue(
        nsPrefixes.containsKey("xsd"),
        "should have namespace with prefix 'xsd'");
    assertEquals(
        "http://www.w3.org/2001/XMLSchema#",
        nsPrefixes.get("xsd"),
        "should have exact value");

    assertTrue(
        nsPrefixes.containsKey("ex"),
        "should have namespace with prefix 'ex'");
    assertEquals(
        "https://www.example.com/",
        nsPrefixes.get("ex"),
        "should have exact value");

    assertTrue(
        nsPrefixes.containsKey("prov"),
        "should have namespace with prefix 'prov'");
    assertEquals(
        "http://www.w3.org/ns/prov#",
        nsPrefixes.get("prov"),
        "should have exact value");
  }

  @Test
  @DisplayName("should have exact default namespace - Document - Deserializer")
  public void should_have_exact_default_namespaces() {
    this.testInit();

    String nsDefault = this.provDoc.getNamespace().getDefaultNamespace();

    assertEquals(
        "https://www.default.com/",
        nsDefault,
        "should have exact value");

  }

  @Test
  @DisplayName("should not have parent namespace - Document - Deserializer")
  public void should_not_have_parent_namespaces() {
    this.testInit();

    Namespace parentDefault = this.provDoc.getNamespace().getParent();

    assertNull(
        parentDefault,
        "should not have parent namespace");

  }

  @Test
  @DisplayName("should have exact one Bundle - Document - Deserializer")
  public void should_have_exact_one_bundle() {
    this.testInit();

    List<StatementOrBundle> statementOrBundles = this.provDoc.getStatementOrBundle();
    assertEquals(1, statementOrBundles.size(), "should have exact one statement");
    assertInstanceOf(Bundle.class, statementOrBundles.getFirst(), "should be a Bundle");

  }

  @Test
  @DisplayName("should have exat 2 namespaces - Bundle - Deserializer")
  public void should_have_2_namespaces_bundle() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Map<String, String> bundleNsPrefixes = bundle.getNamespace().getPrefixes();

    assertEquals(2, bundleNsPrefixes.size(), "should have exac two namespaces");

    // extra blank namespace has been added by deserializer implicitly
    assertTrue(
        bundleNsPrefixes.containsKey("_"),
        "should have namespace with prefix '_'");
    assertEquals(
        "https://openprovenance.org/blank#",
        bundleNsPrefixes.get("_"),
        "should have exact value");

    // ex namespac has to by in bundle prefixes, otherwise bundle id will be parsed
    // as 'ex:{{null}}bundleA'
    // instead of 'ex:{{www.example.com/}}bundleA'
    assertTrue(
        bundleNsPrefixes.containsKey("ex"),
        "should have namespace with prefix 'ex'");
    assertEquals(
        "https://www.example.com/",
        bundleNsPrefixes.get("ex"),
        "should have exact value");
  }

  @Test
  @DisplayName("should not have default namespace - Bundle - Deserializer")
  public void should_have_default_namespaces_bundle() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();

    assertNull(bundle.getNamespace().getDefaultNamespace(),
        "should not have default namespace");
  }

  @Test
  @DisplayName("should have exact 4 parent namespaces - Bundle - Deserializer")
  public void should__have_4_parent_namespaces_bundle() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Map<String, String> nsPrefixes = bundle.getNamespace().getParent().getPrefixes();

    assertEquals(4, nsPrefixes.size(), "should have exact 4 namespaces");

    assertTrue(
        nsPrefixes.containsKey("xsd"),
        "should have namespace with prefix 'xsd'");
    assertEquals(
        "http://www.w3.org/2001/XMLSchema#",
        nsPrefixes.get("xsd"),
        "should have exact value");

    assertTrue(
        nsPrefixes.containsKey("ex"),
        "should have namespace with prefix 'ex'");
    assertEquals(
        "https://www.example.com/",
        nsPrefixes.get("ex"),
        "should have exact value");

    assertTrue(
        nsPrefixes.containsKey("prov"),
        "should have namespace with prefix 'prov'");
    assertEquals(
        "http://www.w3.org/ns/prov#",
        nsPrefixes.get("prov"),
        "should have exact value");

    // extra blank namespace has been added by deserializer implicitly
    assertTrue(
        nsPrefixes.containsKey("_"),
        "should have namespace with prefix '_'");
    assertEquals(
        "https://openprovenance.org/blank#",
        nsPrefixes.get("_"),
        "should have exact value");
  }

  @Test
  @DisplayName("should have exact Id - Bundle - Deserializer")
  public void should_have_exact_id_bundle() {
    this.testInit();
    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();

    // namespace '"ex": "https://www.example.com/"' has to be in bundle
    // namespaces!!!
    // otherwise bundle id will be deserialized as 'ex:{{null}}bundleA'
    assertEquals(provFactory.newQualifiedName(
        "https://www.example.com/", "bundleA", "ex"),
        bundle.getId(),
        "should have exact Id");
  }

  // ---

  @Test
  @DisplayName("should have exact 1 Entity - Bundle - Deserializer")
  public void should_have_exact_one_entity_bundle_deserializer() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();

    assertEquals(1, this.provUtilities.getEntity(bundle).size());
  }

  @Test
  @DisplayName("should have exact 1 Activity - Bundle - Deserializer")
  public void should_have_exact_one_activity_bundle() {
    this.testInit();
    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();

    assertEquals(1, this.provUtilities.getActivity(bundle).size());
  }

  @Test
  @DisplayName("should have exact 1 Agent - Bundle - Deserializer")
  public void should_have_exact_one_agent_bundle() {
    this.testInit();
    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();

    assertEquals(1, this.provUtilities.getAgent(bundle).size());
  }

  // ---

  @Test
  @DisplayName("should have exact id - Entity - Deserializer")
  public void should_have_exact_id_Entity() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Entity entity = this.provUtilities.getEntity(bundle).getFirst();

    assertInstanceOf(QualifiedName.class, entity.getId(),
        "should be QualifiedName");

    assertEquals(
        provFactory.newQualifiedName("https://www.default.com/", "entity1", null),
        entity.getId(),
        "should have exact Id");
  }

  @Test
  @DisplayName("should have exact label - Entity - Deserializer")
  public void should_have_exact_label_Entity() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Entity entity = this.provUtilities.getEntity(bundle).getFirst();

    // Check label: '"prov:label": [{"$": "Entity Label","lang": "en"}]'
    assertEquals(1, entity.getLabel().size(),
        "should have exact one value");

    assertInstanceOf(LangString.class, entity.getLabel().getFirst(),
        "should by deserialized as LangString");

    LangString value = entity.getLabel().getFirst();
    assertEquals("Entity Label", value.getValue(),
        "should have exact value");

    assertEquals("en", value.getLang(),
        "should have exact lang");
  }

  @Test
  @DisplayName("should have exact location - Entity - Deserializer")
  public void should_have_exact_location_Entity() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Entity entity = this.provUtilities.getEntity(bundle).getFirst();

    // Check locaton: '"prov:location": ["Entity Location"]'
    assertEquals(1, entity.getLocation().size(),
        "should have exact one value");

    assertInstanceOf(Location.class, entity.getLocation().getFirst(),
        "should by deserialized as Location");

    Location location = entity.getLocation().getFirst();

    assertInstanceOf(LangString.class, entity.getLocation().getFirst().getValue(),
        "should by LangString - value of Location");

    LangString value = (LangString) location.getValue();

    assertEquals("Entity Location", value.getValue(),
        "should have exact value");

    assertNull(value.getLang(),
        "should be null - lang has not been specified");

    assertNull(location.getConvertedValue(),
        "should be null - ???");

    assertEquals(this.provFactory.newQualifiedName("http://www.w3.org/ns/prov#", "location", "prov"),
        location.getElementName(),
        "should be exact qualified name");

    assertEquals(AttributeKind.PROV_LOCATION,
        location.getKind(),
        "should have exact kind");

    assertEquals(this.provFactory.newQualifiedName("http://www.w3.org/2001/XMLSchema#", "string", "xsd"),
        location.getType(),
        "should be string");
  }

  @Test
  @DisplayName("should have exact type - Entity - Deserializer")
  public void should_have_exact_type_Entity() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Entity entity = this.provUtilities.getEntity(bundle).getFirst();

    // Check type: '"prov:type": ["Document"]'
    assertEquals(1, entity.getType().size(),
        "should have exact one value");

    assertInstanceOf(Type.class, entity.getType().getFirst(),
        "should by deserialized as Type");

    Type type = entity.getType().getFirst();

    assertInstanceOf(LangString.class, type.getValue(),
        "should by LangString - value of Type");

    LangString value = (LangString) type.getValue();
    assertEquals("Document", value.getValue(),
        "should have exact value");

    assertNull(value.getLang(),
        "should be null - lang has not been specified");

    assertNull(type.getConvertedValue(),
        "should be null - ???");

    assertEquals(this.provFactory.newQualifiedName("http://www.w3.org/ns/prov#", "type", "prov"),
        type.getElementName(),
        "should be exact qualified name");

    assertEquals(AttributeKind.PROV_TYPE,
        type.getKind(),
        "should have exact kind");

    assertEquals(this.provFactory.newQualifiedName("http://www.w3.org/2001/XMLSchema#", "string", "xsd"),
        type.getType(),
        "should be String");
  }

  @Test
  @DisplayName("should have exact value - Entity - Deserializer")
  public void should_have_exact_value_Entity() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Entity entity = this.provUtilities.getEntity(bundle).getFirst();

    // Check value: '"prov:value": [{"$": "42","type": "xsd:int"}]'
    assertInstanceOf(Value.class, entity.getValue(),
        "should by deserialized as Value");

    Value val = entity.getValue();

    assertInstanceOf(String.class,
        val.getValue(),
        "should by String - value of Value");

    assertEquals("42", val.getValue(),
        "should have exact value");

    assertNull(val.getConvertedValue(),
        "should be null - ???");

    assertEquals(provFactory.newQualifiedName("http://www.w3.org/2001/XMLSchema#", "int", "xsd"),
        val.getType(),
        "should have exact type");

    assertEquals(this.provFactory.newQualifiedName("http://www.w3.org/ns/prov#", "value", "prov"),
        val.getElementName(),
        "should be exact qualified name");

    assertEquals(AttributeKind.PROV_VALUE,
        val.getKind(),
        "should have exact kind");
  }

  @Test
  @DisplayName("should have exat 4 attributes - Entity - Deserializer")
  public void should_have_4_attributes_Entity() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Entity entity = this.provUtilities.getEntity(bundle).getFirst();

    assertEquals(4, entity.getOther().size(), "should have exact 4 Other attributes");
  }

  @Test
  @DisplayName("should have version attribute - Entity - Deserializer")
  public void should_have_version_attribute_Entity() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Entity entity = this.provUtilities.getEntity(bundle).getFirst();

    // Check value: '"ex:version": [{"$": "2","type": "xsd:int"}]'
    List<Other> versions = entity.getOther().stream()
        .filter(o -> o.getElementName()
            .equals(this.provFactory.newQualifiedName("https://www.example.com/", "version", "ex")))
        .collect(Collectors.toList());

    assertEquals(1, versions.size(),
        "should have exact one value");

    Other version = versions.getFirst();

    assertInstanceOf(String.class, version.getValue(),
        "should by String - value of version");

    String valueOfVersion = (String) version.getValue();

    assertEquals("2", valueOfVersion,
        "should have exact value");

    assertNull(version.getConvertedValue(),
        "should be null - ???");

    assertEquals(this.provFactory.newQualifiedName("https://www.example.com/", "version", "ex"),
        version.getElementName(),
        "should be exact qualified name");

    assertEquals(AttributeKind.OTHER,
        version.getKind(),
        "should have exact kind");

    assertEquals(this.provFactory.newQualifiedName("http://www.w3.org/2001/XMLSchema#", "int", "xsd"),
        version.getType(),
        "should be string");
  }

  @Test
  @DisplayName("should have byteSize attribute - Entity - Deserializer")
  public void should_have_byteSize_attribute_Entity() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Entity entity = this.provUtilities.getEntity(bundle).getFirst();

    // Check value: '"ex:byteSize": [{"$": "1034","type": "xsd:positiveInteger"}]'
    List<Other> versions = entity.getOther().stream()
        .filter(o -> o.getElementName()
            .equals(this.provFactory.newQualifiedName("https://www.example.com/", "byteSize", "ex")))
        .collect(Collectors.toList());

    assertEquals(1, versions.size(),
        "should have exact one value");

    Other byteSize = versions.getFirst();

    assertInstanceOf(String.class, byteSize.getValue(),
        "should by String - value of byteSize");

    String valueOfByteSize = (String) byteSize.getValue();

    assertEquals("1034", valueOfByteSize,
        "should have exact value");

    assertNull(byteSize.getConvertedValue(),
        "should be null - ???");

    assertEquals(this.provFactory.newQualifiedName("https://www.example.com/", "byteSize", "ex"),
        byteSize.getElementName(),
        "should be exact qualified name");

    assertEquals(AttributeKind.OTHER,
        byteSize.getKind(),
        "should have exact kind");

    assertEquals(this.provFactory.newQualifiedName("http://www.w3.org/2001/XMLSchema#", "positiveInteger", "xsd"),
        byteSize.getType(),
        "should be string");
  }

  @Test
  @DisplayName("should have compression attribute - Entity - Deserializer")
  public void should_have_compression_attribute_Entity() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Entity entity = this.provUtilities.getEntity(bundle).getFirst();

    // Check value: '"ex:compression": [{"$": "0.825","type": "xsd:double"}]'
    List<Other> compressions = entity.getOther().stream()
        .filter(
            o -> o.getElementName()
                .equals(this.provFactory.newQualifiedName("https://www.example.com/", "compression", "ex")))
        .collect(Collectors.toList());

    assertEquals(1, compressions.size(),
        "should have exact one value");

    Other compression = compressions.getFirst();

    assertInstanceOf(String.class, compression.getValue(),
        "should by String - value of compression");

    String valueOfCompression = (String) compression.getValue();

    assertEquals("0.825", valueOfCompression,
        "should have exact value");

    assertNull(compression.getConvertedValue(),
        "should be null - ???");

    assertEquals(this.provFactory.newQualifiedName("https://www.example.com/", "compression", "ex"),
        compression.getElementName(),
        "should be exact qualified name");

    assertEquals(AttributeKind.OTHER,
        compression.getKind(),
        "should have exact kind");

    assertEquals(this.provFactory.newQualifiedName("http://www.w3.org/2001/XMLSchema#", "double", "xsd"),
        compression.getType(),
        "should be string");
  }

  @Test
  @DisplayName("should have content attribute - Entity - Deserializer")
  public void should_have_content_attribute_Entity() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Entity entity = this.provUtilities.getEntity(bundle).getFirst();

    // Check value:
    // '"ex:content":[{"$":"Y29udGVudCBoZXJl","type":"xsd:base64Binary"}]'
    List<Other> contents = entity.getOther().stream()
        .filter(
            o -> o.getElementName()
                .equals(this.provFactory.newQualifiedName("https://www.example.com/", "content", "ex")))
        .collect(Collectors.toList());

    assertEquals(1, contents.size(),
        "should have exact one value");

    Other content = contents.getFirst();

    assertInstanceOf(String.class, content.getValue(),
        "should by String - value of content");

    String valueOfContent = (String) content.getValue();

    assertEquals("Y29udGVudCBoZXJl", valueOfContent,
        "should have exact value");

    assertNull(content.getConvertedValue(),
        "should be null - ???");

    assertEquals(this.provFactory.newQualifiedName("https://www.example.com/", "content", "ex"),
        content.getElementName(),
        "should be exact qualified name");

    assertEquals(AttributeKind.OTHER,
        content.getKind(),
        "should have exact kind");

    assertEquals(this.provFactory.newQualifiedName("http://www.w3.org/2001/XMLSchema#", "base64Binary", "xsd"),
        content.getType(),
        "should be string");
  }

  // ---

  @Test
  @DisplayName("should have exact id - Activity - Deserializer")
  public void should_have_exact_id_Activity() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Activity activity = this.provUtilities.getActivity(bundle).getFirst();

    assertInstanceOf(QualifiedName.class, activity.getId(),
        "should be QualifiedName");

    assertEquals(
        provFactory.newQualifiedName("https://www.example.com/", "activity1", "ex"),
        activity.getId(),
        "should have exact Id");
  }

  @Test
  @DisplayName("should not have label - Activity - Deserializer")
  public void should_not_have_label_Activity() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Activity activity = this.provUtilities.getActivity(bundle).getFirst();

    // Check label: ''
    assertEquals(0, activity.getLabel().size(),
        "should not have value");
  }

  @Test
  @DisplayName("should not have location - Activity - Deserializer")
  public void should_not_have_location_Activity() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Activity activity = this.provUtilities.getActivity(bundle).getFirst();

    // Check label: ''
    assertEquals(0, activity.getLocation().size(),
        "should not have value");
  }

  @Test
  @DisplayName("should have exact type - Activity - Deserializer")
  public void should_have_exact_type_Activity() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Activity activity = this.provUtilities.getActivity(bundle).getFirst();

    // Check type: '"prov:type": [{"$": "ex:edit","type": "xsd:QName"}]'
    assertEquals(1, activity.getType().size(),
        "should have exact one value");

    assertInstanceOf(Type.class, activity.getType().getFirst(),
        "should by deserialized as Type");

    Type type = activity.getType().getFirst();

    // since the value is equal to "ex:edit" I expected QualifiedName but is String
    assertInstanceOf(String.class, type.getValue(),
        "should by String - value of Type");

    String value = (String) type.getValue();
    assertEquals("ex:edit", value,
        "should have exact value");

    assertNull(type.getConvertedValue(),
        "should be null - ???");

    assertEquals(this.provFactory.newQualifiedName("http://www.w3.org/ns/prov#", "type", "prov"),
        type.getElementName(),
        "should be exact qualified name");

    assertEquals(AttributeKind.PROV_TYPE,
        type.getKind(),
        "should have exact kind");

    assertEquals(this.provFactory.newQualifiedName("http://www.w3.org/2001/XMLSchema#", "QName", "xsd"),
        type.getType(),
        "should be QualifiedName");
  }

  @Test
  @DisplayName("should have exat 1 attribute - Activity - Deserializer")
  public void should_have_1_attributes_Activity() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Activity activity = this.provUtilities.getActivity(bundle).getFirst();

    assertEquals(1, activity.getOther().size(), "should have exact 1 Other attribute");
  }

  @Test
  @DisplayName("should have host attribute - Activity - Deserializer")
  public void should_have_host_attribute_Activity() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Activity activity = this.provUtilities.getActivity(bundle).getFirst();

    // Check value: '"ex:host": ["server.example.org"]'
    List<Other> hosts = activity.getOther().stream()
        .filter(
            o -> o.getElementName().equals(this.provFactory.newQualifiedName("https://www.example.com/", "host", "ex")))
        .collect(Collectors.toList());

    assertEquals(1, hosts.size(),
        "should have exact one value");

    Other host = hosts.getFirst();

    assertInstanceOf(LangString.class, host.getValue(),
        "should by String - value of version");

    LangString valueOfHost = (LangString) host.getValue();

    assertEquals("server.example.org", valueOfHost.getValue(),
        "should have exact value");
    assertEquals(null, valueOfHost.getLang(),
        "should not have lang");

    assertNull(host.getConvertedValue(),
        "should be null - ???");

    assertEquals(this.provFactory.newQualifiedName("https://www.example.com/", "host", "ex"),
        host.getElementName(),
        "should be exact qualified name");

    assertEquals(AttributeKind.OTHER,
        host.getKind(),
        "should have exact kind");

    assertEquals(this.provFactory.newQualifiedName("http://www.w3.org/2001/XMLSchema#", "string", "xsd"),
        host.getType(),
        "should be string");
  }

  @Test
  @DisplayName("should have startTime attribute - Activity - Deserializer")
  public void should_have_startTime_attribute_Activity() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Activity activity = this.provUtilities.getActivity(bundle).getFirst();

    // Check value: '"prov:startTime": "2025-08-16T12:00:00.000+02:00"'
    XMLGregorianCalendar startTime = activity.getStartTime();

    assertEquals("2025-08-16T12:00:00.000+02:00", startTime.toString(),
        "should have exact value");
  }

  @Test
  @DisplayName("should have endTime attribute - Activity - Deserializer")
  public void should_have_endTime_attribute_Activity() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Activity activity = this.provUtilities.getActivity(bundle).getFirst();

    // Check value: '"prov:endTime": "2025-08-16T13:00:00.000+02:00"'
    XMLGregorianCalendar endTime = activity.getEndTime();

    assertEquals("2025-08-16T13:00:00.000+02:00", endTime.toString(),
        "should have exact value");
  }

  // ---
  @Test
  @DisplayName("should have exact id - Agent - Deserializer")
  public void should_have_exact_id_Agent() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Agent agent = this.provUtilities.getAgent(bundle).getFirst();

    assertInstanceOf(QualifiedName.class, agent.getId(),
        "should be QualifiedName");

    assertEquals(
        provFactory.newQualifiedName("https://www.example.com/", "agent1", "ex"),
        agent.getId(),
        "should have exact Id");
  }

  @Test
  @DisplayName("should not have label - Agent - Deserializer")
  public void should_not_have_label_Agent() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Agent agent = this.provUtilities.getAgent(bundle).getFirst();

    // Check label: ''
    assertEquals(0, agent.getLabel().size(),
        "should not have value");
  }

  @Test
  @DisplayName("should not have location - Agent - Deserializer")
  public void should_not_have_location_Agent() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Agent agent = this.provUtilities.getAgent(bundle).getFirst();

    // Check label: ''
    assertEquals(0, agent.getLocation().size(),
        "should not have value");
  }

  @Test
  @DisplayName("should have exact type - Agent - Deserializer")
  public void should_have_exact_type_Agent() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Agent agent = this.provUtilities.getAgent(bundle).getFirst();

    // Check type: '"prov:type": {"$": "prov:Person","type": "xsd:QName"}'
    assertEquals(1, agent.getType().size(),
        "should have exact one value");

    assertInstanceOf(Type.class, agent.getType().getFirst(),
        "should by deserialized as Type");

    Type type = agent.getType().getFirst();

    // since the value is equal to "prov:Person" I expected QualifiedName
    // but is String
    assertInstanceOf(String.class, type.getValue(),
        "should by String - value of Type");

    String value = (String) type.getValue();
    assertEquals("prov:Person", value,
        "should have exact value");

    assertNull(type.getConvertedValue(),
        "should be null - ???");

    assertEquals(this.provFactory.newQualifiedName("http://www.w3.org/ns/prov#", "type", "prov"),
        type.getElementName(),
        "should be exact qualified name");

    assertEquals(AttributeKind.PROV_TYPE,
        type.getKind(),
        "should have exact kind");

    assertEquals(this.provFactory.newQualifiedName("http://www.w3.org/2001/XMLSchema#", "QName", "xsd"),
        type.getType(),
        "should be QualifiedName");
  }

  @Test
  @DisplayName("should have exat 2 attributes - Agent - Deserializer")
  public void should_have_2_attributes_Agent() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Agent agent = this.provUtilities.getAgent(bundle).getFirst();

    assertEquals(2, agent.getOther().size(), "should have exact 2 Other attributes");
  }

  @Test
  @DisplayName("should have employee attribute - Agent - Deserializer")
  public void should_have_employee_attribute_Agent() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Agent agent = this.provUtilities.getAgent(bundle).getFirst();

    // Check value: '"ex:employee": {"$": 1234,"type": "xsd:int"}'
    List<Other> employees = agent.getOther().stream()
        .filter(
            o -> o.getElementName()
                .equals(this.provFactory.newQualifiedName("https://www.example.com/", "employee", "ex")))
        .collect(Collectors.toList());

    assertEquals(1, employees.size(),
        "should have exact one value");

    Other employee = employees.getFirst();

    assertInstanceOf(String.class, employee.getValue(),
        "should by String - value of version");

    String valueOfEmployee = (String) employee.getValue();

    assertEquals("1234", valueOfEmployee,
        "should have exact value");

    assertNull(employee.getConvertedValue(),
        "should be null - ???");

    assertEquals(this.provFactory.newQualifiedName("https://www.example.com/", "employee", "ex"),
        employee.getElementName(),
        "should be exact qualified name");

    assertEquals(AttributeKind.OTHER,
        employee.getKind(),
        "should have exact kind");

    assertEquals(this.provFactory.newQualifiedName("http://www.w3.org/2001/XMLSchema#", "int", "xsd"),
        employee.getType(),
        "should be integer");
  }

  @Test
  @DisplayName("should have name attribute - Agent - Deserializer")
  public void should_have_name_attribute_Agent() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Agent agent = this.provUtilities.getAgent(bundle).getFirst();

    // Check value: '"ex:employee": {"$": 1234,"type": "xsd:int"}'
    List<Other> names = agent.getOther().stream()
        .filter(
            o -> o.getElementName().equals(this.provFactory.newQualifiedName("https://www.example.com/", "name", "ex")))
        .collect(Collectors.toList());

    assertEquals(1, names.size(),
        "should have exact one value");

    Other name = names.getFirst();

    assertInstanceOf(LangString.class, name.getValue(),
        "should by LangString - value of version");

    LangString valueOfName = (LangString) name.getValue();

    assertEquals("Alice", valueOfName.getValue(),
        "should have exact value");
    assertEquals(null, valueOfName.getLang(),
        "should not have lang value");

    assertNull(name.getConvertedValue(),
        "should be null - ???");

    assertEquals(this.provFactory.newQualifiedName("https://www.example.com/", "name", "ex"),
        name.getElementName(),
        "should be exact qualified name");

    assertEquals(AttributeKind.OTHER,
        name.getKind(),
        "should have exact kind");

    assertEquals(this.provFactory.newQualifiedName("http://www.w3.org/2001/XMLSchema#", "string", "xsd"),
        name.getType(),
        "should be string");
  }

  // ---

  @Test
  @DisplayName("should have exact 1 Activity associated with Agent - Bundle - Deserializer")
  public void should_have_exact_one_association_bundle_deserializer() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();

    List<Relation> associations = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_ASSOCIATION))
        .collect(Collectors.toList());

    assertEquals(1, associations.size());
  }

  @Test
  @DisplayName("should have exact 1 Entity attributed to Agent - Bundle - Deserializer")
  public void should_have_exact_one_attribution_bundle_deserializer() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();

    List<Relation> attributions = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_ATTRIBUTION))
        .collect(Collectors.toList());

    assertEquals(1, attributions.size());
  }

  @Test
  @DisplayName("should have exact 1 Entity generated by Activity - Bundle - Deserializer")
  public void should_have_exact_one_generation_bundle_deserializer() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();

    List<Relation> generations = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_ATTRIBUTION))
        .collect(Collectors.toList());

    assertEquals(1, generations.size());
  }

  // ---

  @Test
  @DisplayName("should not have Id - WasAssociatedWith - Deserializer")
  public void should_not_have_id_WasAssociatedWith() {
    this.testInit();
    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();

    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_ASSOCIATION))
        .collect(Collectors.toList())
        .getFirst();

    assertInstanceOf(WasAssociatedWith.class, relation,
        "should be instance of WasAssociatedWith");

    WasAssociatedWith wasAssociatedWith = (WasAssociatedWith) relation;

    assertNull(wasAssociatedWith.getId(),
        "should not have Id");
  }

  @Test
  @DisplayName("should not have label - WasAssociatedWith - Deserializer")
  public void should_not_have_label_WasAssociatedWith() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_ASSOCIATION))
        .collect(Collectors.toList())
        .getFirst();

    WasAssociatedWith wasAssociatedWith = (WasAssociatedWith) relation;
    // Check label: ''
    assertEquals(0, wasAssociatedWith.getLabel().size(),
        "should not have value");
  }

  @Test
  @DisplayName("should have exact plan - WasAssociatedWith - Deserializer")
  public void should_have_exact_plan_WasAssociatedWith() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_ASSOCIATION))
        .collect(Collectors.toList())
        .getFirst();

    WasAssociatedWith wasAssociatedWith = (WasAssociatedWith) relation;
    // Check plan: '"prov:plan": "ex:rec-advance"'
    assertEquals(this.provFactory.newQualifiedName("https://www.example.com/", "rec-advance", "ex"),
        wasAssociatedWith.getPlan(),
        "should have exact value");
  }

  @Test
  @DisplayName("should have exact role - WasAssociatedWith - Deserializer")
  public void should_have_exact_role_WasAssociatedWith() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_ASSOCIATION))
        .collect(Collectors.toList())
        .getFirst();

    WasAssociatedWith wasAssociatedWith = (WasAssociatedWith) relation;

    // Check plan: '"prov:role": ["editor"]'
    assertEquals(1,
        wasAssociatedWith.getRole().size(),
        "should have exact one value");

    Role role = wasAssociatedWith.getRole().getFirst();

    // since the value is equal to "prov:Person" I expected QualifiedName
    // but is String
    assertInstanceOf(
        LangString.class, role.getValue(),
        "should by LangString - value of Role");

    LangString value = (LangString) role.getValue();
    assertEquals("editor", value.getValue(),
        "should have exact value");
    assertNull(value.getLang(),
        "should not have lang value");

    assertNull(role.getConvertedValue(),
        "should be null - ???");

    assertEquals(this.provFactory.newQualifiedName("http://www.w3.org/ns/prov#", "role", "prov"),
        role.getElementName(),
        "should be exact qualified name");

    assertEquals(AttributeKind.PROV_ROLE,
        role.getKind(),
        "should have exact kind");

    assertEquals(this.provFactory.newQualifiedName("http://www.w3.org/2001/XMLSchema#", "string", "xsd"),
        role.getType(),
        "should be String");
  }

  @Test
  @DisplayName("should not have type - WasAssociatedWith - Deserializer")
  public void should_not_have_type_WasAssociatedWith() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_ASSOCIATION))
        .collect(Collectors.toList())
        .getFirst();

    WasAssociatedWith wasAssociatedWith = (WasAssociatedWith) relation;

    // Check type: ''
    assertEquals(0,
        wasAssociatedWith.getType().size(),
        "should not have value");
  }

  @Test
  @DisplayName("should not have other attributes - WasAssociatedWith - Deserializer")
  public void should_not_have_other_WasAssociatedWith() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_ASSOCIATION))
        .collect(Collectors.toList())
        .getFirst();

    WasAssociatedWith wasAssociatedWith = (WasAssociatedWith) relation;

    // Check type: ''
    assertEquals(0,
        wasAssociatedWith.getOther().size(),
        "should not have value");
  }

  @Test
  @DisplayName("should have exact Activity - WasAssociatedWith - Deserializer")
  public void should_have_exact_Activity_WasAssociatedWith() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_ASSOCIATION))
        .collect(Collectors.toList())
        .getFirst();

    WasAssociatedWith wasAssociatedWith = (WasAssociatedWith) relation;

    // Check activity: '"prov:activity": "ex:activity1"'
    assertEquals(this.provFactory.newQualifiedName("https://www.example.com/", "activity1", "ex"),
        wasAssociatedWith.getActivity(),
        "should not have value");
  }

  @Test
  @DisplayName("should have exact Agent - WasAssociatedWith - Deserializer")
  public void should_have_exact_Agent_WasAssociatedWith() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_ASSOCIATION))
        .collect(Collectors.toList())
        .getFirst();

    WasAssociatedWith wasAssociatedWith = (WasAssociatedWith) relation;

    // Check agent: '"prov:agent": "ex:agent1"'
    assertEquals(this.provFactory.newQualifiedName("https://www.example.com/", "agent1", "ex"),
        wasAssociatedWith.getAgent(),
        "should not have value");
  }

  // ---

  @Test
  @DisplayName("should not have Id - WasAttributedTo - Deserializer")
  public void should_not_have_id_WasAttributedTo() {
    this.testInit();
    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();

    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_ATTRIBUTION))
        .collect(Collectors.toList())
        .getFirst();

    assertInstanceOf(WasAttributedTo.class, relation,
        "should be instance of WasAttributedTo");

    WasAttributedTo wasAttributedTo = (WasAttributedTo) relation;

    assertNull(wasAttributedTo.getId(),
        "should not have Id");
  }

  @Test
  @DisplayName("should not have label - WasAttributedTo - Deserializer")
  public void should_not_have_label_WasAttributedTo() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_ATTRIBUTION))
        .collect(Collectors.toList())
        .getFirst();

    WasAttributedTo wasAttributedTo = (WasAttributedTo) relation;
    // Check label: ''
    assertEquals(0, wasAttributedTo.getLabel().size(),
        "should not have value");
  }

  @Test
  @DisplayName("should not have type - WasAttributedTo - Deserializer")
  public void should_not_have_type_WasAttributedTo() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_ATTRIBUTION))
        .collect(Collectors.toList())
        .getFirst();

    WasAttributedTo wasAttributedTo = (WasAttributedTo) relation;

    // Check type: ''
    assertEquals(0,
        wasAttributedTo.getType().size(),
        "should not have value");
  }

  @Test
  @DisplayName("should not have other attributes - WasAttributedTo - Deserializer")
  public void should_not_have_other_WasAttributedTo() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_ATTRIBUTION))
        .collect(Collectors.toList())
        .getFirst();

    WasAttributedTo wasAttributedTo = (WasAttributedTo) relation;

    // Check type: ''
    assertEquals(0,
        wasAttributedTo.getOther().size(),
        "should not have value");
  }

  @Test
  @DisplayName("should have exact Entity - WasAttributedTo - Deserializer")
  public void should_have_exact_Entity_WasAttributedTo() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_ATTRIBUTION))
        .collect(Collectors.toList())
        .getFirst();

    WasAttributedTo wasAttributedTo = (WasAttributedTo) relation;

    // Check activity: '"prov:entity": "entity1"'
    assertEquals(this.provFactory.newQualifiedName("https://www.default.com/", "entity1", null),
        wasAttributedTo.getEntity(),
        "should not have value");
  }

  @Test
  @DisplayName("should have exact Agent - WasAttributedTo - Deserializer")
  public void should_have_exact_Agent_WasAttributedTo() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_ATTRIBUTION))
        .collect(Collectors.toList())
        .getFirst();

    WasAttributedTo wasAttributedTo = (WasAttributedTo) relation;

    // Check agent: '"prov:agent": "ex:agent1"'
    assertEquals(this.provFactory.newQualifiedName("https://www.example.com/", "agent1", "ex"),
        wasAttributedTo.getAgent(),
        "should not have value");
  }

  // ---

  @Test
  @DisplayName("should not have Id - WasGeneratedBy - Deserializer")
  public void should_not_have_id_WasGeneratedBy() {
    this.testInit();
    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();

    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_GENERATION))
        .collect(Collectors.toList())
        .getFirst();

    assertInstanceOf(WasGeneratedBy.class, relation,
        "should be instance of WasGeneratedBy");

    WasGeneratedBy wasGeneratedBy = (WasGeneratedBy) relation;

    assertNull(wasGeneratedBy.getId(),
        "should not have Id");
  }

  @Test
  @DisplayName("should not have label - WasGeneratedBy - Deserializer")
  public void should_not_have_label_WasGeneratedBy() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_GENERATION))
        .collect(Collectors.toList())
        .getFirst();

    WasGeneratedBy wasGeneratedBy = (WasGeneratedBy) relation;
    // Check label: ''
    assertEquals(0, wasGeneratedBy.getLabel().size(),
        "should not have value");
  }

  @Test
  @DisplayName("should not have location - WasGeneratedBy - Deserializer")
  public void should_not_have_location_WasGeneratedBy() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_GENERATION))
        .collect(Collectors.toList())
        .getFirst();

    WasGeneratedBy wasGeneratedBy = (WasGeneratedBy) relation;
    // Check location: ''
    assertEquals(0, wasGeneratedBy.getLocation().size(),
        "should not have value");
  }

  @Test
  @DisplayName("should not have role - WasGeneratedBy - Deserializer")
  public void should_not_have_role_WasGeneratedBy() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_GENERATION))
        .collect(Collectors.toList())
        .getFirst();

    WasGeneratedBy wasGeneratedBy = (WasGeneratedBy) relation;
    // Check role: ''
    assertEquals(0, wasGeneratedBy.getRole().size(),
        "should not have value");
  }

  @Test
  @DisplayName("should not have type - WasGeneratedBy - Deserializer")
  public void should_not_have_type_WasGeneratedBy() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_GENERATION))
        .collect(Collectors.toList())
        .getFirst();

    WasGeneratedBy wasGeneratedBy = (WasGeneratedBy) relation;
    // Check type: ''
    assertEquals(0, wasGeneratedBy.getType().size(),
        "should not have value");
  }

  @Test
  @DisplayName("should not have other attributes - WasGeneratedBy - Deserializer")
  public void should_not_have_other_WasGeneratedBy() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_GENERATION))
        .collect(Collectors.toList())
        .getFirst();

    WasGeneratedBy wasGeneratedBy = (WasGeneratedBy) relation;
    // Check other: ''
    assertEquals(0, wasGeneratedBy.getOther().size(),
        "should not have value");
  }

  @Test
  @DisplayName("should have exact time - WasGeneratedBy - Deserializer")
  public void should_have_time_WasGeneratedBy() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_GENERATION))
        .collect(Collectors.toList())
        .getFirst();

    WasGeneratedBy wasGeneratedBy = (WasGeneratedBy) relation;
    // Check time: '"prov:time": "2025-08-16T13:00:00.000+02:00"'
    assertEquals(
        "2025-08-16T13:00:00.000+02:00", wasGeneratedBy.getTime().toString(),
        "should be exact time value");
  }

  @Test
  @DisplayName("should have exact Activity - WasGeneratedBy - Deserializer")
  public void should_have_Activity_WasGeneratedBy() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_GENERATION))
        .collect(Collectors.toList())
        .getFirst();

    WasGeneratedBy wasGeneratedBy = (WasGeneratedBy) relation;
    // Check Activity: '"prov:activity": "ex:activity1"'
    assertEquals(this.provFactory.newQualifiedName("https://www.example.com/", "activity1", "ex"),
        wasGeneratedBy.getActivity(),
        "should be exact Activity id");
  }

  @Test
  @DisplayName("should have exact Entity - WasGeneratedBy - Deserializer")
  public void should_have_Entity_WasGeneratedBy() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();
    Relation relation = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_GENERATION))
        .collect(Collectors.toList())
        .getFirst();

    WasGeneratedBy wasGeneratedBy = (WasGeneratedBy) relation;
    // Check Entity: '"prov:entity": "entity1"'
    assertEquals(this.provFactory.newQualifiedName("https://www.default.com/", "entity1", null),
        wasGeneratedBy.getEntity(),
        "should be exact Activity id");
  }
}
