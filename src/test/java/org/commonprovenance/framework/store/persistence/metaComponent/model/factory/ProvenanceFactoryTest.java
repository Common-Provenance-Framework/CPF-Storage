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
  private final String JWT = "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiIsInRydXN0ZWRQYXJ0eVVyaSI6ImxvY2FsaG9zdDo4MDIwIiwieDVjIjpbIk1JSUNNakNDQWRpZ0F3SUJBZ0lVU0xqNVk3UFhJUzEzcVBFUERkbElOQm5Rem9nd0NnWUlLb1pJemowRUF3SXdiVEVMTUFrR0ExVUVCaE1DUlZVeE9qQTRCZ05WQkFvTU1VUnBjM1J5YVdKMWRHVmtJRkJ5YjNabGJtRnVZMlVnUkdWdGJ5QkRaWEowYVdacFkyRjBaU0JCZFhSb2IzSnBkSGt4SWpBZ0JnTlZCQU1NR1VSUVJDQkRaWEowYVdacFkyRjBaU0JCZFhSb2IzSnBkSGt3SGhjTk1qUXhNVEUyTURJMU9UVXlXaGNOTXpReE1URTBNREkxT1RVeVdqQmRNUXN3Q1FZRFZRUUdFd0pEV2pFeU1EQUdBMVVFQ2d3cFJHbHpkSEpwWW5WMFpXUWdVSEp2ZG1WdVlXNWpaU0JFWlcxdklGUnlkWE4wWldRZ1VHRnlkSGt4R2pBWUJnTlZCQU1NRVVSUVJDQlVjblZ6ZEdWa0lGQmhjblI1TUZrd0V3WUhLb1pJemowQ0FRWUlLb1pJemowREFRY0RRZ0FFK1Y4a1Q0amt2RVdtWDMwMUtBUzlla2xtblJOaTZnVTkrS0h4dVFwa1NPaE1UcTk2Q0JYRnBmb2tSZDd0NVZkclJ5MHVxWnN5U05wNWtXMGhuUU1KV2FObU1HUXdFZ1lEVlIwVEFRSC9CQWd3QmdFQi93SUJBREFPQmdOVkhROEJBZjhFQkFNQ0FZWXdIUVlEVlIwT0JCWUVGTUNuUFJqaVhva1Q3cXV3WlJCMTZBQWd6N2JuTUI4R0ExVWRJd1FZTUJhQUZDeUVLd2kxanZkUHFmaVUrTmRIL252aDdQWVpNQW9HQ0NxR1NNNDlCQU1DQTBnQU1FVUNJUUN5WnJVU2hWcXJvaERxZHpkT0ZtQXlGRHB3TUFPOEk2amFodmcxRlJBWllnSWdWaDRTMnRRbjEyWFlkZDVJU3NDcEFCc2g2WnJqU2lWWXJ0MlQxTzFuUXN3PSJdfQ.eyJzdWIiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXBpL3YxL2RvY3VtZW50cy9tZXRhLzM3MTU2OWVjLWY4NWMtNGNhNS04NmY1LWY2OGRiYTUzZDVkNyIsImhhc2hfYWxnIjoiU0hBMjU2IiwiZG9jX2RpZ2VzdCI6ImYzODA3MDBlM2EzN2ZlN2ZkNzNiYzg4YjllNjZhZmQ0OWM3NzYxNjkyYTkwZmM0NzdmYmQ0NzEzNWE1YTE1ZWIiLCJvcmdfaWQiOiI2ZmIyOTJhYS1lZTM4LTQ4YWUtOTk4Zi0wNzlhZDlkMDFlN2MiLCJpc3MiOiJUcnVzdGVkUGFydHkiLCJpYXQiOjE3ODg5NzI2MzUsImRvY19pYXQiOjE3ODg5NzI2MzV9.Gv9BtPBIMQmQ2kj17n_6Az-MGn8zCb6pMRibx5SlqTRBo99uwNOTZdzGuuXCA00xGP7M3Olhq7uIYJYgj-Cr7A";

  @Test
  void toProv_shouldCreateBundleWithNodesAndRelations() {
    AppConfiguration config = mock(AppConfiguration.class);
    when(config.getFqdn()).thenReturn("http://localhost:8080/api/v1/");

    EntityNode e2 = new EntityNode(
        "e2",
        "cpm:token",
        Map.of("jwt", this.JWT),
        Map.of());

    AgentNode ag1 = new AgentNode(
        "ag1",
        "cpm:trustedParty",
        Map.of("trustedPartyUri", "trusted-party:8020"));
    EntityNode e1 = new EntityNode(
        "e1",
        "prov:Bundle",
        Map.of(),
        Map.of("version", 1))
        .withRevisionOfEntity(e2)
        .withWasAttributedToAgent(ag1);
    ActivityNode act1 = new ActivityNode("act1", "cpm:tokenGeneration", "2024-01-01T10:15:30Z", "2024-01-01T10:16:30Z",
        Map.of())
        .withUsedEntity(e1)
        .withWasAssociatedWithAgent(ag1);

    BundleNode bundleNode = new BundleNode("bundle-1")
        .withActivities(List.of(act1))
        .withAgents(List.of(ag1))
        .withEntities(List.of(e1, e2));

    Document document = NodeToProvFactory.bundleToProv(config).apply(bundleNode).block();

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
        provFactory.getName().XSD_INTEGER)));
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
    assertEquals(1, entity2.get().getOther().size());
    assertTrue(entity2.get().getOther().contains(provFactory.newOther(
        provFactory.newQualifiedName(CpmNamespaceConstants.CPM_NS, "jwt", CpmNamespaceConstants.CPM_PREFIX),
        this.JWT,
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

    Object value = agent1.get().getOther().getFirst().getValue();

    assertInstanceOf(String.class, value);
    assertEquals("trusted-party:8020", ((String) value));

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
        .withRevisionOfEntity(e2);
    EntityNode e3 = new EntityNode(
        "e3",
        "cpm:token",
        Map.of("jwt", this.JWT),
        Map.of());

    BundleNode bundleNode = new BundleNode("bundle-1")
        .withEntities(List.of(e1, e2, e3));

    Document document = NodeToProvFactory.bundleToProv(config).apply(bundleNode).block();
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

}
