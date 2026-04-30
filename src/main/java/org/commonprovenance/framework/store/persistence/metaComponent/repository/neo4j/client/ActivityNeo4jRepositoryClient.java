package org.commonprovenance.framework.store.persistence.metaComponent.repository.neo4j.client;

import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface ActivityNeo4jRepositoryClient extends ReactiveNeo4jRepository<ActivityNode, String> {
  @Query("""
          MATCH (activity:Activity)
          WHERE activity.identifier = $identifier
          RETURN activity
      """)
  Mono<ActivityNode> findByIdentifier(@Param("identifier") String identifier);

  @Query("""
          MATCH (activity:Activity)
          WHERE activity.identifier = $identifier

          OPTIONAL MATCH (activity)-[rAssociation:was_associated_with]->(agent:Agent)
          OPTIONAL MATCH (activity)-[rUsed:used]->(entity:Entity)

          RETURN activity,
            collect(DISTINCT rAssociation), collect(DISTINCT agent),
            collect(DISTINCT rUsed),  collect(DISTINCT entity)
      """)
  Mono<ActivityNode> findByIdentifierWithRelations(@Param("identifier") String identifier);

  @Query("""
      MATCH (activity:Activity) WHERE elementId(activity)=$activityId
      MATCH (agent:Agent) WHERE elementId(agent)=$agentId
        MERGE (activity)-[:was_associated_with]->(agent)
        RETURN true
      """)
  Mono<Boolean> createWasAssociatedWithRelationship(
      @Param("activityId") String activityId,
      @Param("agentId") String agentId);

  @Query("""
      MATCH (activity:Activity) WHERE elementId(activity)=$activityId
      MATCH (entity:Entity) WHERE elementId(entity)=$entityId
      MERGE (activity)-[:used]->(entity)
      RETURN true
      """)
  Mono<Boolean> createUsedRelationship(
      @Param("activityId") String activityId,
      @Param("entityId") String entityId);
}
