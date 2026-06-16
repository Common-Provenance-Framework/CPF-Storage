package org.commonprovenance.framework.store.persistence.metaComponent.model.factory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.xml.datatype.XMLGregorianCalendar;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BaseProvClassNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.BundleActivities;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.BundleAgents;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.BundleEntities;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.RevisionOf;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Agent;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.Other;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.SpecializationOf;
import org.openprovenance.prov.model.Statement;
import org.openprovenance.prov.model.Type;
import org.openprovenance.prov.model.Used;
import org.openprovenance.prov.model.WasAssociatedWith;
import org.openprovenance.prov.model.WasAttributedTo;
import org.openprovenance.prov.model.WasDerivedFrom;
import org.openprovenance.prov.model.WasGeneratedBy;
import org.openprovenance.prov.vanilla.ProvFactory;
import org.openprovenance.prov.vanilla.ProvUtilities;

import cz.muni.fi.cpm.vanilla.CpmProvFactory;
import reactor.test.StepVerifier;

class NodeFactoryTest {
  private final ProvFactory provFactory = new org.openprovenance.prov.vanilla.ProvFactory();
  private final CpmProvFactory cpmProvFactory = new CpmProvFactory(provFactory);

  @Test
  void toEntity_generalEntity() {
    QualifiedName generatedEntityId = qn("entity-generated");
    Type type = provFactory.newType(
        provFactory.getName().PROV_BUNDLE,
        provFactory.getName().PROV_QUALIFIED_NAME);

    Entity generatedEntity = mock(Entity.class);
    when(generatedEntity.getId()).thenReturn(generatedEntityId);
    when(generatedEntity.getType()).thenReturn(List.of(type));

    EntityNode node = ProvToNodeFactory.toEntity(generatedEntity);

    assertEquals("entity-generated", node.getIdentifier());
    assertEquals("prov:Bundle", node.getProvType());
    assertEquals(0, node.getCpm().entrySet().size());
  }

  @Test
  void toEntity_versionEntity() {
    QualifiedName generatedEntityId = qn("entity-generated");
    Type type = provFactory.newType(
        provFactory.getName().PROV_BUNDLE,
        provFactory.getName().PROV_QUALIFIED_NAME);
    Other other = provFactory.newOther(
        provFactory.newQualifiedName("http://purl.org/pav/", "version", "pav"),
        1,
        provFactory.getName().XSD_INTEGER);
    ;

    Entity generatedEntity = mock(Entity.class);
    when(generatedEntity.getId()).thenReturn(generatedEntityId);
    when(generatedEntity.getType()).thenReturn(List.of(type));
    when(generatedEntity.getOther()).thenReturn(List.of(other));

    EntityNode node = ProvToNodeFactory.toEntity(generatedEntity);

    assertEquals("entity-generated", node.getIdentifier());
    assertEquals("prov:Bundle", node.getProvType());
    assertEquals(0, node.getCpm().entrySet().size());
    assertEquals(1, node.getPav().entrySet().size());
    assertEquals(1, node.getPav().get("version"));
  }

  @Test
  void toEntity_nullDocument_returnsEmptyMono() {
    StepVerifier.create(ProvToNodeFactory.toEntity((Document) null))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  void toEntity_documentWithNoStatementOrBundle_throwsInternalApplicationException() {
    Document document = mock(Document.class);
    when(document.getStatementOrBundle()).thenReturn(List.of());

    StepVerifier.create(ProvToNodeFactory.toEntity(document))
        .verifyErrorSatisfies(e -> {
          assertInstanceOf(InternalApplicationException.class, e);
          assertEquals(
              "Document should have exact one Statement, but has 0!",
              e.getMessage());
        });
  }

  @Test
  void toEntity_documentWithMultipleStatementOrBundle_throwsInternalApplicationException() {
    Document document = mock(Document.class);
    Bundle b1 = mock(Bundle.class);
    Bundle b2 = mock(Bundle.class);
    when(document.getStatementOrBundle()).thenReturn(List.of(b1, b2));

    StepVerifier.create(ProvToNodeFactory.toEntity(document))
        .verifyErrorSatisfies(e -> {
          assertInstanceOf(InternalApplicationException.class, e);
          assertEquals(
              "Document should have exact one Statement, but has 2!",
              e.getMessage());
        });
  }

  @Test
  void toEntity_singleNonBundleEntry_throwsInternalApplicationException() {
    Document document = mock(Document.class);
    Statement statement = mock(Statement.class);
    when(document.getStatementOrBundle()).thenReturn(List.of(statement));

    StepVerifier.create(ProvToNodeFactory.toEntity(document))
        .verifyErrorSatisfies(e -> {
          assertInstanceOf(InternalApplicationException.class, e);
          assertEquals(
              "Statement in Document should be Bundle",
              e.getMessage());
        });
  }

  @Test
  void toEntity_validBundle_buildsBundleNodeWithAllNodeTypes() {
    Document document = mock(Document.class);
    Bundle bundle = mock(Bundle.class);

    QualifiedName bundleId = qn("bundle-1");
    QualifiedName entityId = qn("entity-1");
    QualifiedName agentId = qn("agent-1");
    QualifiedName activityId = qn("activity-1");

    Entity entity = mock(Entity.class);
    when(entity.getId()).thenReturn(entityId);
    when(entity.getLocation()).thenReturn(List.of());
    when(entity.getType()).thenReturn(List.of(
        provFactory.newType(
            cpmProvFactory.newCpmQualifiedName("token"),
            provFactory.getName().PROV_QUALIFIED_NAME)));
    when(entity.getOther()).thenReturn(List.of(
        provFactory.newOther(
            cpmProvFactory.newCpmQualifiedName("originatorId"),
            "org1",
            provFactory.getName().XSD_STRING),
        provFactory.newOther(
            cpmProvFactory.newCpmQualifiedName("authorityId"),
            "Trusted_Party",
            provFactory.getName().XSD_STRING),
        provFactory.newOther(
            cpmProvFactory.newCpmQualifiedName("tokenTimestamp"),
            1774016549,
            provFactory.getName().XSD_LONG),
        provFactory.newOther(
            cpmProvFactory.newCpmQualifiedName("signature"),
            "MEYCIQDfGrAbyowhyFgQr1y8WUHfgtc96vwT0/kQcl84ePlRMQIhAJMwXzDo52ThMm/1L4vfT0kDW/IMWFvP3kg3mvjD/bYm",
            provFactory.getName().XSD_STRING)));

    when(entity.getLabel()).thenReturn(List.of());
    when(entity.getValue()).thenReturn(provFactory.newValue(42));

    Agent agent = mock(Agent.class);
    when(agent.getId()).thenReturn(agentId);
    when(agent.getLocation()).thenReturn(List.of());
    when(agent.getOther()).thenReturn(List.of(
        provFactory.newOther(
            cpmProvFactory.newCpmQualifiedName("trustedPartyUri"),
            "trusted-party:8020",
            provFactory.getName().XSD_ANY_URI)

    ));
    when(agent.getType()).thenReturn(List.of(
        provFactory.newType(
            cpmProvFactory.newCpmQualifiedName("trustedParty"),
            provFactory.getName().PROV_QUALIFIED_NAME)));
    when(agent.getLabel()).thenReturn(List.of());

    Activity activity = mock(Activity.class);
    XMLGregorianCalendar timestampVal = ProvUtilities
        .toXMLGregorianCalendar(Date.from(Instant.ofEpochSecond(1774016549)));
    when(activity.getId()).thenReturn(activityId);
    when(activity.getLocation()).thenReturn(List.of());
    when(activity.getOther()).thenReturn(List.of());
    when(activity.getType()).thenReturn(List.of(
        provFactory.newType(
            cpmProvFactory.newCpmQualifiedName("tokenGeneration"),
            provFactory.getName().PROV_QUALIFIED_NAME)));
    when(activity.getLabel()).thenReturn(List.of());
    when(activity.getStartTime()).thenReturn(timestampVal);
    when(activity.getEndTime()).thenReturn(timestampVal);

    when(bundle.getId()).thenReturn(bundleId);
    when(bundle.getStatement()).thenReturn(List.of(entity, agent, activity));
    when(document.getStatementOrBundle()).thenReturn(List.of(bundle));

    StepVerifier.create(ProvToNodeFactory.toEntity(document))
        .assertNext((BundleNode b) -> {
          assertEquals("bundle-1", b.getIdentifier());
          assertNull(b.getId());

          List<EntityNode> entities = b.getBundleEntities().stream()
              .map(BundleEntities::getEntity)
              .toList();

          // assertEquals(3, nodes.size());
          assertEquals(1, entities.size());
          EntityNode entityNode = entities.getFirst();
          assertEquals("entity-1", entityNode.getIdentifier());
          assertNull(entityNode.getId());
          assertEquals("cpm:token", entityNode.getProvType());

          assertEquals(4, entityNode.getCpm().entrySet().size());
          assertEquals("org1", entityNode.getCpm().get("originatorId"));
          assertEquals("Trusted_Party", entityNode.getCpm().get("authorityId"));
          assertEquals(1774016549L, entityNode.getCpm().get("tokenTimestamp"));
          assertEquals(
              "MEYCIQDfGrAbyowhyFgQr1y8WUHfgtc96vwT0/kQcl84ePlRMQIhAJMwXzDo52ThMm/1L4vfT0kDW/IMWFvP3kg3mvjD/bYm",
              entityNode.getCpm().get("signature"));

          List<AgentNode> agents = b.getBundleAgents().stream()
              .map(BundleAgents::getAgent)
              .toList();

          assertEquals(1, agents.size());
          AgentNode agentNode = agents.getFirst();

          assertEquals("agent-1", agentNode.getIdentifier());
          assertNull(agentNode.getId());
          assertEquals("cpm:trustedParty", agentNode.getProvType());

          assertEquals(1, agentNode.getCpm().entrySet().size());
          assertEquals("trusted-party:8020", agentNode.getCpm().get("trustedPartyUri"));

          List<ActivityNode> activities = b.getBundleActivities().stream()
              .map(BundleActivities::getActivity)
              .toList();

          assertEquals(1, activities.size());
          ActivityNode activityNode = activities.getFirst();

          assertEquals("activity-1", activityNode.getIdentifier());
          assertNull(activityNode.getId());
          assertEquals("cpm:tokenGeneration", activityNode.getProvType());
          assertEquals(0, activityNode.getCpm().entrySet().size());
          assertEquals("2026-03-20T15:22:29.000+01:00", activityNode.getStartTime());
          assertEquals("2026-03-20T15:22:29.000+01:00", activityNode.getEndTime());

          // assertEquals("{}", activities.getFirst().getCpm());

        })
        .verifyComplete();
  }

  @Test
  void revisionOf_pass() {
    Document document = mock(Document.class);
    Bundle bundle = mock(Bundle.class);

    QualifiedName bundleId = qn("bundle-1");
    QualifiedName usedEntityId = qn("entity-used");
    QualifiedName generatedEntityId = qn("entity-generated");

    Entity usedEntity = mock(Entity.class);
    when(usedEntity.getId()).thenReturn(usedEntityId);

    Entity generatedEntity = mock(Entity.class);
    when(generatedEntity.getId()).thenReturn(generatedEntityId);

    WasDerivedFrom wasDerivedFrom = mock(WasDerivedFrom.class);
    when(wasDerivedFrom.getUsedEntity()).thenReturn(usedEntityId);
    when(wasDerivedFrom.getGeneratedEntity()).thenReturn(generatedEntityId);

    when(bundle.getId()).thenReturn(bundleId);
    when(bundle.getStatement()).thenReturn(List.of(usedEntity, generatedEntity, wasDerivedFrom));
    when(document.getStatementOrBundle()).thenReturn(List.of(bundle));
    StepVerifier.create(ProvToNodeFactory.toEntity(document))
        .assertNext((BundleNode b) -> {

          List<EntityNode> entities = b.getBundleEntities().stream()
              .map(BundleEntities::getEntity)
              .toList();

          assertEquals(2, entities.size());

          EntityNode generated = entities.stream()
              .filter(e -> e.getIdentifier() == "entity-generated")
              .toList()
              .getFirst();
          assertEquals("entity-used", generated.getRevisionOf().getFirst().getEntity().getIdentifier());

        })
        .verifyComplete();
  }

  @Test
  void toEntity_withAllRelationStatements_doesNotFailWhenReferencesExist() {
    Document document = mock(Document.class);
    Bundle bundle = mock(Bundle.class);

    QualifiedName bundleId = qn("bundle-1");
    QualifiedName e1 = qn("e1");
    QualifiedName e2 = qn("e2");
    QualifiedName a1 = qn("a1");
    QualifiedName act1 = qn("act1");

    Entity entity1 = mock(Entity.class);
    Entity entity2 = mock(Entity.class);
    Agent agent = mock(Agent.class);
    Activity activity = mock(Activity.class);

    when(entity1.getId()).thenReturn(e1);
    when(entity2.getId()).thenReturn(e2);
    when(agent.getId()).thenReturn(a1);
    when(activity.getId()).thenReturn(act1);

    WasDerivedFrom wdf = mock(WasDerivedFrom.class);
    when(wdf.getGeneratedEntity()).thenReturn(e1);
    when(wdf.getUsedEntity()).thenReturn(e2);

    SpecializationOf sof = mock(SpecializationOf.class);
    when(sof.getSpecificEntity()).thenReturn(e1);
    when(sof.getGeneralEntity()).thenReturn(e2);

    Used used = mock(Used.class);
    when(used.getActivity()).thenReturn(act1);
    when(used.getEntity()).thenReturn(e1);

    WasAssociatedWith waw = mock(WasAssociatedWith.class);
    when(waw.getActivity()).thenReturn(act1);
    when(waw.getAgent()).thenReturn(a1);

    WasAttributedTo wat = mock(WasAttributedTo.class);
    when(wat.getEntity()).thenReturn(e1);
    when(wat.getAgent()).thenReturn(a1);

    WasGeneratedBy wgb = mock(WasGeneratedBy.class);
    when(wgb.getEntity()).thenReturn(e1);
    when(wgb.getActivity()).thenReturn(act1);

    when(bundle.getId()).thenReturn(bundleId);
    when(bundle.getStatement()).thenReturn(List.of(
        entity1, entity2, agent, activity,
        wdf, sof, used, waw, wat, wgb));

    when(document.getStatementOrBundle()).thenReturn(List.of(bundle));

    StepVerifier.create(ProvToNodeFactory.toEntity(document))
        .assertNext((BundleNode b) -> {
          List<EntityNode> entities = NodeFactoryTest.getNodesByType(b, EntityNode.class);
          List<ActivityNode> activities = NodeFactoryTest.getNodesByType(b, ActivityNode.class);

          NodeFactoryTest.getNodeById(entities, e1.getLocalPart())
              .map(eNode1 -> {
                List<RevisionOf> revisionOfs = eNode1.getRevisionOf();
                assertEquals(1, revisionOfs.size());
                assertEquals(e2.getLocalPart(), revisionOfs.getFirst().getEntity().getIdentifier());

                List<org.commonprovenance.framework.store.persistence.metaComponent.model.relation.SpecializationOf> specializationOfs = eNode1
                    .getSpecializationOf();
                assertEquals(1, specializationOfs.size());
                assertEquals(e2.getLocalPart(), specializationOfs.getFirst().getEntity().getIdentifier());

                List<org.commonprovenance.framework.store.persistence.metaComponent.model.relation.WasAttributedTo> wasAttributedTos = eNode1
                    .getWasAttributedTo();
                assertEquals(1, wasAttributedTos.size());
                assertEquals(a1.getLocalPart(), wasAttributedTos.getFirst().getAgent().getIdentifier());

                List<org.commonprovenance.framework.store.persistence.metaComponent.model.relation.WasGeneratedBy> wasGeneratedBies = eNode1
                    .getWasGeneratedBy();
                assertEquals(1, wasGeneratedBies.size());
                assertEquals(act1.getLocalPart(), wasGeneratedBies.getFirst().getActivity().getIdentifier());
                return null;
              });

          NodeFactoryTest.getNodeById(activities, act1.getLocalPart())
              .map(actNode1 -> {
                List<org.commonprovenance.framework.store.persistence.metaComponent.model.relation.Used> useds = actNode1
                    .getUsed();
                assertEquals(1, useds.size());
                assertEquals(e1.getLocalPart(), useds.getFirst().getEntity().getIdentifier());

                List<org.commonprovenance.framework.store.persistence.metaComponent.model.relation.WasAssociatedWith> wasAssociatedWiths = actNode1
                    .getWasAssociatedWith();
                assertEquals(1, wasAssociatedWiths.size());
                assertEquals(a1.getLocalPart(),
                    wasAssociatedWiths.getFirst().getAgent().getIdentifier());

                return null;
              });
        })
        .verifyComplete();
  }

  private static QualifiedName qn(String localPart) {
    QualifiedName qn = mock(QualifiedName.class);
    when(qn.getLocalPart()).thenReturn(localPart);
    return qn;
  }

  private static <T extends BaseProvClassNode> List<T> getNodesByType(BundleNode bundle, Class<T> classType) {
    return bundle.getBundleEntities().stream()
        .map(BundleEntities::getEntity)
        .filter(classType::isInstance)
        .map(classType::cast)
        .toList();
  }

  private static <T extends BaseProvClassNode> Optional<T> getNodeById(List<T> nodes, String id) {
    return nodes.stream()
        .filter(node -> node.getIdentifier().equals(id))
        .findFirst();
  }
}
