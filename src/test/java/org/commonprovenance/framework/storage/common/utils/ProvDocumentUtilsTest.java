package org.commonprovenance.framework.storage.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Agent;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.LangString;
import org.openprovenance.prov.model.Namespace;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.ProvUtilities;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.Relation;
import org.openprovenance.prov.model.StatementOrBundle;
import org.openprovenance.prov.model.WasAssociatedWith;
import org.openprovenance.prov.model.WasAttributedTo;
import org.openprovenance.prov.model.StatementOrBundle.Kind;
import org.openprovenance.prov.model.interop.Formats;
import org.openprovenance.prov.vanilla.WasGeneratedBy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@DisplayName("Provenance JSON Utils Test")
public class ProvDocumentUtilsTest {
  private final String BLANK_URI = "https://openprovenance.org/blank/";
  private final ProvFactory provFactory = new org.openprovenance.prov.vanilla.ProvFactory();
  private final ProvUtilities provUtilities = new ProvUtilities();

  private final String DOCUMENT_JSON = "{\"prefix\":{\"xsd\":\"http://www.w3.org/2001/XMLSchema#\",\"ex\":\"www.example.com/\",\"blank\":\"https://openprovenance.org/blank/\",\"prov\":\"http://www.w3.org/ns/prov#\"},\"bundle\":{\"ex:bundleA\":{\"entity\":{\"blank:entity1\":{\"prov:value\":[{\"$\":\"42\",\"type\":\"xsd:int\"}],\"prov:label\":[{\"$\":\"Entity Label\",\"lang\":\"en\"}],\"prov:location\":\"Entity Location\",\"prov:type\":\"Document\",\"ex:version\":{\"$\":2,\"type\":\"xsd:int\"},\"ex:byteSize\":{\"$\":1034,\"type\":\"xsd:positiveInteger\"},\"ex:compression\":{\"$\":0.825,\"type\":\"xsd:double\"},\"ex:content\":{\"$\":\"Y29udGVudCBoZXJl\",\"type\":\"xsd:base64Binary\"}}},\"activity\":{\"blank:activity1\":{\"prov:startTime\":\"2025-08-16T12:00:00.000+02:00\",\"prov:endTime\":\"2025-08-16T13:00:00.000+02:00\",\"ex:host\":\"server.example.org\",\"prov:type\":{\"$\":\"ex:edit\",\"type\":\"xsd:QName\"}}},\"agent\":{\"blank:agent1\":{\"ex:employee\":{\"$\":1234,\"type\":\"xsd:int\"},\"ex:name\":\"Alice\",\"prov:type\":{\"$\":\"prov:Person\",\"type\":\"xsd:QName\"}}},\"wasAssociatedWith\":{\"blank:assoc\":{\"prov:activity\":\"blank:activity1\",\"prov:agent\":\"blank:agent1\"}},\"wasAttributedTo\":{\"blank:attr\":{\"prov:entity\":\"blank:entity1\",\"prov:agent\":\"blank:agent1\"}},\"wasGeneratedBy\":{\"blank:gen\":{\"prov:entity\":\"blank:entity1\",\"prov:activity\":\"blank:activity1\"}}}}}";

  private Document provDoc;

  private Document getTestDocument() {
    Namespace nsDocument = provFactory.newNamespace();
    nsDocument.register("xsd", "http://www.w3.org/2001/XMLSchema#");
    nsDocument.register("ex", "www.example.com/");
    nsDocument.register("blank", "https://openprovenance.org/blank/");
    nsDocument.register("prov", "http://www.w3.org/ns/prov#");
    Namespace nsBundle = provFactory.newNamespace();
    nsBundle.register("xsd", "http://www.w3.org/2001/XMLSchema#");
    nsBundle.register("ex", "www.example.com/");
    nsBundle.register("blank", "https://openprovenance.org/blank/");
    nsBundle.register("prov", "http://www.w3.org/ns/prov#");
    // nsBundle.register("_", "https://openprovenance.org/blank#");

    QualifiedName entityId = new org.openprovenance.prov.vanilla.QualifiedName(BLANK_URI, "entity1", "blank");
    Entity entity = provFactory.newEntity(entityId);
    entity.setValue(this.provFactory.newValue(42));
    entity.getLocation().add(this.provFactory.newLocation(
        "Entity Location",
        this.provFactory.getName().XSD_STRING));
    entity.getLabel().add(new org.openprovenance.prov.vanilla.LangString("Entity Label", "en"));
    entity.getType().add(this.provFactory.newType(
        "Document",
        this.provFactory.getName().XSD_STRING));
    entity.getOther().add(this.provFactory.newOther(
        this.provFactory.newQualifiedName("www.example.com/", "byteSize", "ex"),
        1034,
        this.provFactory.getName().XSD_POSITIVE_INTEGER));
    entity.getOther().add(this.provFactory.newOther(
        this.provFactory.newQualifiedName("www.example.com/", "compression", "ex"),
        0.825,
        this.provFactory.getName().XSD_DOUBLE));
    entity.getOther().add(this.provFactory.newOther(
        this.provFactory.newQualifiedName("www.example.com/", "version", "ex"),
        2,
        this.provFactory.getName().XSD_INT));
    entity.getOther().add(this.provFactory.newOther(
        this.provFactory.newQualifiedName("www.example.com/", "content", "ex"),
        "Y29udGVudCBoZXJl",
        this.provFactory.getName().XSD_BASE64_BINARY));

    QualifiedName activityId = new org.openprovenance.prov.vanilla.QualifiedName(BLANK_URI, "activity1", "blank");
    Activity activity = provFactory.newActivity(activityId);
    activity.setStartTime(provFactory.newISOTime("2025-08-16T12:00:00.000+02:00"));
    activity.setEndTime(provFactory.newISOTime("2025-08-16T13:00:00.000+02:00"));
    activity.getType().add(this.provFactory.newType(
        this.provFactory.newQualifiedName("www.example.com/", "edit", "ex"),
        this.provFactory.getName().newXsdQualifiedName("QName")));
    activity.getOther().add(this.provFactory.newOther(
        this.provFactory.newQualifiedName("www.example.com/", "host", "ex"),
        "server.example.org",
        this.provFactory.getName().XSD_STRING));
    QualifiedName agentId = new org.openprovenance.prov.vanilla.QualifiedName(BLANK_URI, "agent1", "blank");
    Agent agent = provFactory.newAgent(agentId);

    agent.getType().add(this.provFactory.newType(this.provFactory.getName().PROV_PERSON,
        this.provFactory.getName().newXsdQualifiedName("QName")));
    agent.getOther().add(this.provFactory.newOther(
        this.provFactory.newQualifiedName("www.example.com/", "name", "ex"),
        "Alice",
        this.provFactory.getName().XSD_STRING));
    agent.getOther().add(this.provFactory.newOther(
        this.provFactory.newQualifiedName("www.example.com/", "employee", "ex"),
        1234,
        this.provFactory.getName().XSD_INT));

    Relation relation2 = provFactory
        .newWasAssociatedWith(
            new org.openprovenance.prov.vanilla.QualifiedName(BLANK_URI, "assoc", "blank"),
            activityId,
            agentId);
    Relation relation = provFactory
        .newWasAttributedTo(
            new org.openprovenance.prov.vanilla.QualifiedName(BLANK_URI, "attr", "blank"),
            entityId,
            agentId);
    Relation relation3 = provFactory
        .newWasGeneratedBy(
            new org.openprovenance.prov.vanilla.QualifiedName(BLANK_URI, "gen", "blank"),
            entityId,
            activityId);

    QualifiedName bundleId = provFactory.newQualifiedName("www.example.com/", "bundleA", "ex");

    Bundle bundle = provFactory.newNamedBundle(
        bundleId,
        nsBundle,
        List.of(entity, agent, activity, relation, relation2, relation3));

    return provFactory.newDocument(nsDocument, List.of(bundle));
  }

  private void testInit() {
    try {
      if (this.provDoc == null)
        provDoc = ProvDocumentUtils.deserialize(this.DOCUMENT_JSON, Formats.ProvFormat.JSON);
    } catch (Exception e) {
      fail(e.getMessage(), e.getCause());
    }
  }

  @Test
  @DisplayName("should have exact 4 namespaces - Document - Deserializer")
  public void shouldHaveExactNamespaces() {
    this.testInit();

    Namespace ns = this.provDoc.getNamespace();

    assertEquals(4, ns.getPrefixes().size(), "should have 4 namespaces");

    assertTrue(
        ns.getPrefixes().containsKey("xsd"),
        "should have namespace with prefix 'xsd'");
    assertEquals(
        "http://www.w3.org/2001/XMLSchema#",
        ns.getPrefixes().get("xsd"),
        "should have exact value");

    assertTrue(
        ns.getPrefixes().containsKey("ex"),
        "should have namespace with prefix 'ex'");
    assertEquals(
        "www.example.com/",
        ns.getPrefixes().get("ex"),
        "should have exact value");

    assertTrue(
        ns.getPrefixes().containsKey("blank"),
        "should have namespace with prefix 'blank'");
    assertEquals(
        "https://openprovenance.org/blank/",
        ns.getPrefixes().get("blank"),
        "should have exact value");

    assertTrue(
        ns.getPrefixes().containsKey("prov"),
        "should have namespace with prefix 'prov'");
    assertEquals(
        "http://www.w3.org/ns/prov#",
        ns.getPrefixes().get("prov"),
        "should have exact value");
  }

  @Test
  @DisplayName("should have exact 1 Bundle - Document - Deserializer")
  public void shouldHaveOneBundle() {
    this.testInit();

    List<StatementOrBundle> statementOrBundles = this.provDoc.getStatementOrBundle();
    assertEquals(1, statementOrBundles.size(), "should have exact one statement");
    assertInstanceOf(Bundle.class, statementOrBundles.getFirst(), "should be a Bundle");

  }

  @Test
  @DisplayName("should have exact 5 namespaces - Bundle - Deserializer")
  public void shouldHaveExactNamespacesBundle() {
    this.testInit();

    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();

    assertEquals(5, bundle.getNamespace().getPrefixes().size(), "should not have namespaces");

    assertTrue(
        bundle.getNamespace().getPrefixes().containsKey("xsd"),
        "should have namespace with prefix 'xsd'");
    assertEquals(
        "http://www.w3.org/2001/XMLSchema#",
        bundle.getNamespace().getPrefixes().get("xsd"),
        "should have exact value");

    assertTrue(
        bundle.getNamespace().getPrefixes().containsKey("ex"),
        "should have namespace with prefix 'ex'");
    assertEquals(
        "www.example.com/",
        bundle.getNamespace().getPrefixes().get("ex"),
        "should have exact value");

    assertTrue(
        bundle.getNamespace().getPrefixes().containsKey("blank"),
        "should have namespace with prefix 'blank'");
    assertEquals(
        "https://openprovenance.org/blank/",
        bundle.getNamespace().getPrefixes().get("blank"),
        "should have exact value");

    assertTrue(
        bundle.getNamespace().getPrefixes().containsKey("prov"),
        "should have namespace with prefix 'prov'");
    assertEquals(
        "http://www.w3.org/ns/prov#",
        bundle.getNamespace().getPrefixes().get("prov"),
        "should have exact value");

    assertTrue(
        bundle.getNamespace().getPrefixes().containsKey("_"),
        "should have namespace with prefix '_'");
    assertEquals(
        "https://openprovenance.org/blank#",
        bundle.getNamespace().getPrefixes().get("_"),
        "should have exact value");
  }

  @Test
  @DisplayName("should have exact Id - Bundle - Deserializer")
  public void shouldHaveExactIdBundle() {
    this.testInit();
    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();

    assertEquals(provFactory.newQualifiedName(
        "www.example.com/", "bundleA", "ex"),
        bundle.getId(),
        "should have exact Id");
  }

  @Test
  @DisplayName("should have exact 1 Entity - Bundle - Deserializer")
  public void shouldHaveExactEntityBundle() {
    this.testInit();
    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();

    assertEquals(1, this.provUtilities.getEntity(bundle).size());

    Entity entity = this.provUtilities.getEntity(bundle).getFirst();

    assertEquals(
        provFactory.newQualifiedName("https://openprovenance.org/blank/", "entity1", "blank"),
        entity.getId(),
        "should have exact Id");

    assertEquals("Entity Label",
        entity.getLabel().getFirst().getValue(),
        "should have exact label");

    assertEquals("en",
        entity.getLabel().getFirst().getLang(),
        "should have exact label lang");

    assertEquals("Entity Location",
        ((LangString) entity.getLocation().getFirst().getValue()).getValue(),
        "should have exact location");

    assertEquals("Document",
        ((LangString) entity.getType().getFirst().getValue()).getValue(),
        "should have exact type");

    assertEquals("42",
        entity.getValue().getValue(),
        "should have exact value");

    assertEquals(provFactory.newQualifiedName("http://www.w3.org/2001/XMLSchema#", "int", "xsd"),
        entity.getValue().getType(),
        "should be an int value");

    assertEquals(4, entity.getOther().size(), "should have exact 4 Other attributes");
    assertTrue(
        entity.getOther().stream()
            .filter(o -> o.getElementName().getPrefix().equals("ex")
                && o.getElementName().getLocalPart().equals("byteSize")
                && o.getValue().equals("1034")
                && o.getType().getPrefix().equals("xsd")
                && o.getType().getLocalPart().equals("positiveInteger"))
            .count() == 1,
        "should have attribute 'ex:byteSize' with exact value");

    assertTrue(
        entity.getOther().stream()
            .filter(o -> o.getElementName().getPrefix().equals("ex")
                && o.getElementName().getLocalPart().equals("compression")
                && o.getValue().equals("0.825")
                && o.getType().getPrefix().equals("xsd")
                && o.getType().getLocalPart().equals("double"))
            .count() == 1,
        "should have attribute 'ex:compression' with exact value");

    assertTrue(
        entity.getOther().stream()
            .filter(o -> o.getElementName().getPrefix().equals("ex")
                && o.getElementName().getLocalPart().equals("version")
                && o.getValue().equals("2")
                && o.getType().getPrefix().equals("xsd")
                && o.getType().getLocalPart().equals("int"))
            .count() == 1,
        "should have attribute 'ex:version' with exact value");

    assertTrue(
        entity.getOther().stream()
            .filter(o -> o.getElementName().getPrefix().equals("ex")
                && o.getElementName().getLocalPart().equals("content")
                && o.getValue().equals("Y29udGVudCBoZXJl")
                && o.getType().getPrefix().equals("xsd")
                && o.getType().getLocalPart().equals("base64Binary"))
            .count() == 1,
        "should have attribute 'ex:content' with exact value");
  }

  @Test
  @DisplayName("should have exact 1 Activity - Bundle - Deserializer")
  public void shouldHaveExactActivityBundle() {
    this.testInit();
    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();

    assertEquals(1, this.provUtilities.getActivity(bundle).size());

    Activity activity = this.provUtilities.getActivity(bundle).getFirst();

    assertEquals(
        provFactory.newQualifiedName("https://openprovenance.org/blank/", "activity1", "blank"),
        activity.getId(),
        "should have exact Id");

    assertTrue(activity.getLabel().size() == 0, "should not have Label attributes");
    assertTrue(activity.getLocation().size() == 0, "should not have Location attributes");

    assertTrue(
        activity.getType().getFirst().getValue().equals("ex:edit")
            && provFactory.newQualifiedName("http://www.w3.org/2001/XMLSchema#", "QName", "xsd")
                .equals(activity.getType().getFirst().getType()),
        "should have exact type, which is QualifiedName");

    assertEquals(
        "2025-08-16T12:00:00.000+02:00",
        activity.getStartTime().toString(),
        "should have exact start time");

    assertEquals(
        "2025-08-16T13:00:00.000+02:00",
        activity.getEndTime().toString(),
        "should have exact end time");

    assertEquals(1, activity.getOther().size(), "should have exact 1 Other attributes");

    assertTrue(
        activity.getOther().stream()
            .filter(o -> o.getElementName()
                .equals(provFactory.newQualifiedName("www.example.com/", "host", "ex"))
                && o.getValue()
                    .equals(new org.openprovenance.prov.vanilla.LangString("server.example.org")))
            .count() == 1,
        "should have attribute 'ex:byteSize' with exact value");

  }

  @Test
  @DisplayName("should have exact 1 Agent - Bundle - Deserializer")
  public void shouldHaveExactAgentBundle() {
    this.testInit();
    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();

    assertEquals(1, this.provUtilities.getAgent(bundle).size());

    Agent agent = this.provUtilities.getAgent(bundle).getFirst();

    assertEquals(
        provFactory.newQualifiedName("https://openprovenance.org/blank/", "agent1", "blank"),
        agent.getId(),
        "should have exact Id");

    assertTrue(agent.getLabel().size() == 0, "should not have Label attributes");
    assertTrue(agent.getLocation().size() == 0, "should not have Location attributes");

    assertTrue(
        agent.getType().getFirst().getValue().equals("prov:Person")
            && provFactory.newQualifiedName("http://www.w3.org/2001/XMLSchema#", "QName", "xsd")
                .equals(agent.getType().getFirst().getType()),
        "should have exact type, which is QualifiedName");

    assertEquals(2, agent.getOther().size(), "should have exact 2 Other attributes");

    assertTrue(
        agent.getOther().stream()
            .filter(o -> o.getElementName()
                .equals(provFactory.newQualifiedName("www.example.com/", "name", "ex"))
                && o.getValue().equals(new org.openprovenance.prov.vanilla.LangString("Alice")))
            .count() == 1,
        "should have attribute 'ex:name' with exact value");

    assertTrue(
        agent.getOther().stream()
            .filter(o -> o.getElementName()
                .equals(provFactory.newQualifiedName("www.example.com/", "employee", "ex"))
                && o.getValue().equals("1234")
                && o.getType().equals(this.provFactory.getName().XSD_INT))

            .count() == 1,
        "should have attribute 'ex:employee' with exact value");

  }

  @Test
  @DisplayName("should have activity associated with agent - Bundle - Deserializer")
  public void shouldHaveExactActivityAssociatedWithAgentBundle() {
    this.testInit();
    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();

    List<Relation> associations = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_ASSOCIATION))
        .collect(Collectors.toList());

    assertEquals(1, associations.size());

    WasAssociatedWith association = (WasAssociatedWith) associations.getFirst();

    assertEquals(
        provFactory.newQualifiedName("https://openprovenance.org/blank/", "assoc",
            "blank"),
        association.getId(),
        "should have exact Id");

    assertTrue(association.getLabel().size() == 0, "should not have Label attributes");
    assertTrue(association.getPlan() == null, "should not have Plan attribute");
    assertTrue(association.getRole().size() == 0, "should not have Role attributes");
    assertTrue(association.getType().size() == 0, "should not have Type attributes");
    assertTrue(association.getOther().size() == 0, "should not have Other attributes");

    assertEquals(
        this.provFactory.newQualifiedName("https://openprovenance.org/blank/", "activity1", "blank"),
        association.getActivity(),
        "should have exact activity");
    assertEquals(
        this.provFactory.newQualifiedName("https://openprovenance.org/blank/", "agent1", "blank"),
        association.getAgent(),
        "should have exact agent");
  }

  @Test
  @DisplayName("should have entity attributed to agent - Bundle - Deserializer")
  public void shouldHaveExactEntityAttributedToAgentBundle() {
    this.testInit();
    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();

    List<Relation> attributions = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_ATTRIBUTION))
        .collect(Collectors.toList());

    assertEquals(1, attributions.size());

    WasAttributedTo attribution = (WasAttributedTo) attributions.getFirst();

    assertEquals(
        provFactory.newQualifiedName("https://openprovenance.org/blank/", "attr", "blank"),
        attribution.getId(),
        "should have exact Id");

    assertTrue(attribution.getLabel().size() == 0, "should not have Label attributes");
    assertTrue(attribution.getType().size() == 0, "should not have Type attributes");
    assertTrue(attribution.getOther().size() == 0, "should not have Other attributes");

    assertEquals(
        this.provFactory.newQualifiedName("https://openprovenance.org/blank/", "entity1", "blank"),
        attribution.getEntity(),
        "should have exact entity");
    assertEquals(
        this.provFactory.newQualifiedName("https://openprovenance.org/blank/", "agent1", "blank"),
        attribution.getAgent(),
        "should have exact agent");
  }

  @Test
  @DisplayName("should have entity generated by activity - Bundle - Deserializer")
  public void shouldHaveExactEntityGeneratedByActivityBundle() {
    this.testInit();
    Bundle bundle = (Bundle) provDoc.getStatementOrBundle().getFirst();

    List<Relation> generations = this.provUtilities.getRelations(bundle).stream()
        .filter(r -> r.getKind().equals(Kind.PROV_GENERATION))
        .collect(Collectors.toList());

    assertEquals(1, generations.size());

    WasGeneratedBy generation = (WasGeneratedBy) generations.getFirst();

    assertEquals(
        provFactory.newQualifiedName("https://openprovenance.org/blank/", "gen", "blank"),
        generation.getId(),
        "should have exact Id");

    assertTrue(generation.getLabel().size() == 0, "should not have Label attributes");
    assertTrue(generation.getType().size() == 0, "should not have Type attributes");
    assertTrue(generation.getRole().size() == 0, "should not have Role attributes");
    assertTrue(generation.getLocation().size() == 0, "should not have Locations attributes");
    assertTrue(generation.getOther().size() == 0, "should not have Other attributes");

    assertEquals(
        this.provFactory.newQualifiedName("https://openprovenance.org/blank/", "entity1", "blank"),
        generation.getEntity(),
        "should have exact entity");
    assertEquals(
        this.provFactory.newQualifiedName("https://openprovenance.org/blank/", "activity1", "blank"),
        generation.getActivity(),
        "should have exact agent");
  }

  @Test
  @DisplayName("should serialize provenance Document into exact json - Serializer")
  public void shouldSerializeDocumentIntoExactJson() {
    String prov = ProvDocumentUtils.serialize(
        getTestDocument(),
        Formats.ProvFormat.JSON);

    try {

      ObjectMapper mapper = new ObjectMapper();
      JsonNode expected = mapper.readTree(
          ProvJsonUtils.postprocessJsonAfterSerialization(
              ProvJsonUtils.preprocessJsonForDeserialization(this.DOCUMENT_JSON, false),
              false));

      assertEquals(expected, mapper.readTree(prov));
    } catch (Exception e) {
      System.out.println(e.getMessage());
      fail(e.getMessage());
    }
  }
}
