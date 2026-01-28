package org.commonprovenance.framework.storage.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Agent;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.Namespace;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.WasAssociatedWith;
import org.openprovenance.prov.model.WasAttributedTo;
import org.openprovenance.prov.model.interop.Formats;
import org.openprovenance.prov.model.WasGeneratedBy;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

import tools.jackson.databind.ObjectMapper;

@DisplayName("Provenance JSON Utils Test")
public class ProvDocumentUtilsSerializerTest {
  private final ProvFactory provFactory = new org.openprovenance.prov.vanilla.ProvFactory();

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
       """;

  private Document getTestDocument() {
    Namespace nsDocument = provFactory.newNamespace();
    nsDocument.setDefaultNamespace("https://www.default.com/");
    nsDocument.register("xsd", "http://www.w3.org/2001/XMLSchema#");
    nsDocument.register("ex", "https://www.example.com/");
    nsDocument.register("prov", "http://www.w3.org/ns/prov#");

    Namespace nsBundle = provFactory.newNamespace();
    nsBundle.register("ex", "https://www.example.com/");

    QualifiedName entityId = provFactory.newQualifiedName(nsDocument.getDefaultNamespace(), "entity1", null);
    Entity entity = provFactory.newEntity(entityId);
    entity.setValue(this.provFactory.newValue(42));
    entity.getLabel().add(new org.openprovenance.prov.vanilla.LangString("Entity Label", "en"));
    entity.getLocation().add(this.provFactory.newLocation(
        "Entity Location",
        this.provFactory.getName().XSD_STRING));
    entity.getType().add(this.provFactory.newType(
        "Document",
        this.provFactory.getName().XSD_STRING));

    entity.getOther().add(this.provFactory.newOther(
        this.provFactory.newQualifiedName("https://www.example.com/", "version", "ex"),
        2,
        this.provFactory.getName().XSD_INT));

    entity.getOther().add(this.provFactory.newOther(
        this.provFactory.newQualifiedName("https://www.example.com/", "byteSize", "ex"),
        1034,
        this.provFactory.getName().XSD_POSITIVE_INTEGER));
    entity.getOther().add(this.provFactory.newOther(
        this.provFactory.newQualifiedName("https://www.example.com/", "compression", "ex"),
        0.825,
        this.provFactory.getName().XSD_DOUBLE));
    entity.getOther().add(this.provFactory.newOther(
        this.provFactory.newQualifiedName("https://www.example.com/", "content", "ex"),
        "Y29udGVudCBoZXJl",
        this.provFactory.getName().XSD_BASE64_BINARY));

    QualifiedName activityId = new org.openprovenance.prov.vanilla.QualifiedName(
        "https://www.example.com/", "activity1", "ex");
    Activity activity = provFactory.newActivity(activityId);
    activity.setStartTime(provFactory.newISOTime("2025-08-16T12:00:00.000+02:00"));
    activity.setEndTime(provFactory.newISOTime("2025-08-16T13:00:00.000+02:00"));
    activity.getType().add(this.provFactory.newType(
        this.provFactory.newQualifiedName("https://www.example.com/", "edit", "ex"),
        this.provFactory.getName().newXsdQualifiedName("QName")));
    activity.getOther().add(this.provFactory.newOther(
        this.provFactory.newQualifiedName("https://www.example.com/", "host", "ex"),
        "server.example.org",
        this.provFactory.getName().XSD_STRING));

    QualifiedName agentId = new org.openprovenance.prov.vanilla.QualifiedName(
        "https://www.example.com/", "agent1", "ex");
    Agent agent = provFactory.newAgent(agentId);

    agent.getOther().add(this.provFactory.newOther(
        this.provFactory.newQualifiedName("https://www.example.com/", "employee", "ex"),
        1234,
        this.provFactory.getName().XSD_INT));

    agent.getOther().add(this.provFactory.newOther(
        this.provFactory.newQualifiedName("https://www.example.com/", "name", "ex"),
        "Alice",
        this.provFactory.getName().XSD_STRING));

    agent.getType().add(this.provFactory.newType(this.provFactory.getName().PROV_PERSON,
        this.provFactory.getName().newXsdQualifiedName("QName")));

    WasAssociatedWith wasAssociatedWith = provFactory
        .newWasAssociatedWith(
            null,
            activityId,
            agentId,
            provFactory.newQualifiedName("https://www.example.com/", "rec-advance", "ex"));
    wasAssociatedWith.getRole().add(provFactory.newRole(
        "editor",
        this.provFactory.getName().newXsdQualifiedName("string")));

    WasAttributedTo wasAttributedTo = provFactory
        .newWasAttributedTo(
            null,
            entityId,
            agentId);

    WasGeneratedBy wasGeneratedBy = provFactory
        .newWasGeneratedBy(
            null,
            entityId,
            activityId,
            provFactory.newISOTime("2025-08-16T13:00:00.000+02:00"));

    QualifiedName bundleId = provFactory.newQualifiedName("https://www.example.com/", "bundleA", "ex");

    Bundle bundle = provFactory.newNamedBundle(
        bundleId,
        nsBundle,
        List.of(entity, agent, activity, wasAttributedTo, wasAssociatedWith, wasGeneratedBy));

    return provFactory.newDocument(nsDocument, List.of(bundle));
  }

  @Test
  @DisplayName("should serialize provenance Document into exact json - Serializer")
  public void shouldSerializeDocumentIntoExactJson() {
    String prov = ProvDocumentUtils.serialize(
        getTestDocument(),
        Formats.ProvFormat.JSON);

    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode expected = mapper.readTree(this.DOCUMENT_JSON);

      assertEquals(expected, mapper.readTree(prov));
    } catch (JacksonException e) {
      fail(e.getMessage());
    }
  }
}
