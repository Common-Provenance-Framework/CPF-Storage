package org.commonprovenance.framework.store.persistence.metaComponent.model.factory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BaseProvClassNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.Contains;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.RevisionOf;
import org.junit.jupiter.api.Test;
import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Agent;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.SpecializationOf;
import org.openprovenance.prov.model.Statement;
import org.openprovenance.prov.model.Used;
import org.openprovenance.prov.model.WasAssociatedWith;
import org.openprovenance.prov.model.WasAttributedTo;
import org.openprovenance.prov.model.WasDerivedFrom;
import org.openprovenance.prov.model.WasGeneratedBy;
import org.openprovenance.prov.vanilla.ProvFactory;

import reactor.test.StepVerifier;

class NodeFactoryTest {
  private final ProvFactory provFactory = new org.openprovenance.prov.vanilla.ProvFactory();

  @Test
  void toEntity_nullDocument_returnsEmptyMono() {
    StepVerifier.create(NodeFactory.toEntity(null))
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  void toEntity_documentWithNoStatementOrBundle_throwsInternalApplicationException() {
    Document document = mock(Document.class);
    when(document.getStatementOrBundle()).thenReturn(List.of());

    StepVerifier.create(NodeFactory.toEntity(document))
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

    StepVerifier.create(NodeFactory.toEntity(document))
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

    StepVerifier.create(NodeFactory.toEntity(document))
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
    when(entity.getOther()).thenReturn(List.of());
    when(entity.getType()).thenReturn(List.of(provFactory.newType(
        provFactory.newQualifiedName(
            "https://www.commonprovenancemodel.org/cpm-namespace-v1-0/", "token", "cpm"),
        provFactory.getName().PROV_TYPE)));

    when(entity.getLabel()).thenReturn(List.of());
    when(entity.getValue()).thenReturn(provFactory.newValue(42));

    Agent agent = mock(Agent.class);
    when(agent.getId()).thenReturn(agentId);
    when(agent.getLocation()).thenReturn(List.of());
    when(agent.getOther()).thenReturn(List.of());
    when(agent.getType()).thenReturn(List.of());
    when(agent.getLabel()).thenReturn(List.of());

    Activity activity = mock(Activity.class);
    when(activity.getId()).thenReturn(activityId);
    when(activity.getLocation()).thenReturn(List.of());
    when(activity.getOther()).thenReturn(List.of());
    when(activity.getType()).thenReturn(List.of());
    when(activity.getLabel()).thenReturn(List.of());

    when(bundle.getId()).thenReturn(bundleId);
    when(bundle.getStatement()).thenReturn(List.of(entity, agent, activity));
    when(document.getStatementOrBundle()).thenReturn(List.of(bundle));

    StepVerifier.create(NodeFactory.toEntity(document))
        .assertNext((BundleNode b) -> {
          assertEquals("bundle-1", b.getId());
          assertEquals("{}", b.getAttributes());

          List<BaseProvClassNode> nodes = b.getContains().stream()
              .map(Contains::getNode)
              .toList();

          assertEquals(3, nodes.size());

          List<BaseProvClassNode> entities = nodes.stream()
              .filter(n -> (n instanceof EntityNode _) ? true : false)
              .toList();

          assertEquals(1, entities.size());
          assertEquals("entity-1", entities.getFirst().getId());
          assertEquals("{\"prov:type\":[\"cpm:token\"],\"prov:value\":[\"42\"]}", entities.getFirst().getAttributes());

          List<BaseProvClassNode> agents = nodes.stream()
              .filter(n -> (n instanceof AgentNode _) ? true : false)
              .toList();

          assertEquals(1, agents.size());
          assertEquals("agent-1", agents.getFirst().getId());
          assertEquals("{}", agents.getFirst().getAttributes());

          List<BaseProvClassNode> activities = nodes.stream()
              .filter(n -> (n instanceof ActivityNode _) ? true : false)
              .toList();

          assertEquals(1, activities.size());
          assertEquals("activity-1", activities.getFirst().getId());
          assertEquals("{}", activities.getFirst().getAttributes());

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
    StepVerifier.create(NodeFactory.toEntity(document))
        .assertNext((BundleNode b) -> {
          List<BaseProvClassNode> nodes = b.getContains().stream()
              .map(Contains::getNode)
              .toList();
          assertEquals(2, nodes.size());

          List<BaseProvClassNode> entities = nodes.stream()
              .filter(n -> (n instanceof EntityNode _) ? true : false)
              .toList();
          assertEquals(2, entities.size());

          EntityNode generated = (EntityNode) entities.stream().filter(e -> e.getId() == "entity-generated").toList()
              .getFirst();
          assertEquals("entity-used", generated.getRevisionOf().getFirst().getEntity().getId());

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

    StepVerifier.create(NodeFactory.toEntity(document))
        .assertNext((BundleNode b) -> {
          List<EntityNode> entities = NodeFactoryTest.getNodesByType(b, EntityNode.class);
          List<ActivityNode> activities = NodeFactoryTest.getNodesByType(b, ActivityNode.class);

          NodeFactoryTest.getNodeById(entities, e1.getLocalPart())
              .map(eNode1 -> {
                List<RevisionOf> revisionOfs = eNode1.getRevisionOf();
                assertEquals(1, revisionOfs.size());
                assertEquals(e2.getLocalPart(), revisionOfs.getFirst().getEntity().getId());

                List<org.commonprovenance.framework.store.persistence.metaComponent.model.relation.SpecializationOf> specializationOfs = eNode1
                    .getSpecializationOf();
                assertEquals(1, specializationOfs.size());
                assertEquals(e2.getLocalPart(), specializationOfs.getFirst().getEntity().getId());

                List<org.commonprovenance.framework.store.persistence.metaComponent.model.relation.WasAttributedTo> wasAttributedTos = eNode1
                    .getWasAttributedTo();
                assertEquals(1, wasAttributedTos.size());
                assertEquals(a1.getLocalPart(), wasAttributedTos.getFirst().getAgent().getId());

                List<org.commonprovenance.framework.store.persistence.metaComponent.model.relation.WasGeneratedBy> wasGeneratedBies = eNode1
                    .getWasGeneratedBy();
                assertEquals(1, wasGeneratedBies.size());
                assertEquals(act1.getLocalPart(), wasGeneratedBies.getFirst().getActivity().getId());
                return null;
              });

          NodeFactoryTest.getNodeById(activities, act1.getLocalPart())
              .map(actNode1 -> {
                List<org.commonprovenance.framework.store.persistence.metaComponent.model.relation.Used> useds = actNode1
                    .getUsed();
                assertEquals(1, useds.size());
                assertEquals(e1.getLocalPart(), useds.getFirst().getEntity().getId());

                List<org.commonprovenance.framework.store.persistence.metaComponent.model.relation.WasAssociatedWith> wasAssociatedWiths = actNode1
                    .getWasAssociatedWith();
                assertEquals(1, wasAssociatedWiths.size());
                assertEquals(a1.getLocalPart(),
                    wasAssociatedWiths.getFirst().getAgent().getId());

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
    return bundle.getContains().stream()
        .map(Contains::getNode)
        .filter(classType::isInstance)
        .map(classType::cast)
        .toList();
  }

  private static <T extends BaseProvClassNode> Optional<T> getNodeById(List<T> nodes, String id) {
    return nodes.stream()
        .filter(node -> node.getId().equals(id))
        .findFirst();
  }
}