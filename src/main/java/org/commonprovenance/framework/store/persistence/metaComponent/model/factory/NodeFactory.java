package org.commonprovenance.framework.store.persistence.metaComponent.model.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Agent;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Element;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.SpecializationOf;
import org.openprovenance.prov.model.Statement;
import org.openprovenance.prov.model.Used;
import org.openprovenance.prov.model.WasAssociatedWith;
import org.openprovenance.prov.model.WasAttributedTo;
import org.openprovenance.prov.model.WasDerivedFrom;
import org.openprovenance.prov.model.WasGeneratedBy;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

public class NodeFactory {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static Mono<BundleNode> toEntity(Document document) {

    Function<Document, Mono<Bundle>> getBundle = (Document doc) -> Mono.justOrEmpty(doc)
        .map(Document::getStatementOrBundle)
        .flatMap(MONO.makeSure(
            statements -> statements.size() != 1,
            _ -> new InternalApplicationException("Document should have exact one Statement")))
        .map(List::getFirst)
        .flatMap(sOrB -> (sOrB instanceof Bundle b)
            ? Mono.just(b)
            : Mono.error(new InternalApplicationException("Statement in Document should be Bundle")));

    Function<List<Statement>, HashMap<Class<?>, List<Statement>>> getIndexedStatements = (
        List<Statement> statements) -> statements.stream()
            .collect(Collectors.groupingBy((Statement statement) -> {
              if (statement instanceof Entity)
                return Entity.class;
              if (statement instanceof Activity)
                return Activity.class;
              if (statement instanceof Agent)
                return Agent.class;
              if (statement instanceof WasDerivedFrom)
                return WasDerivedFrom.class;
              if (statement instanceof SpecializationOf)
                return SpecializationOf.class;
              if (statement instanceof Used)
                return Used.class;
              if (statement instanceof WasAssociatedWith)
                return WasAssociatedWith.class;
              if (statement instanceof WasAttributedTo)
                return WasAttributedTo.class;
              if (statement instanceof WasGeneratedBy)
                return WasGeneratedBy.class;
              return Statement.class; // fallback bucket
            },
                HashMap::new,
                Collectors.toList()));

    Function<Element, Mono<String>> getAttributesAsJsonString = (Element element) -> Mono.justOrEmpty(element)
        .map((Element e) -> {
          Map<String, Object> attributesMap = new LinkedHashMap<>();
          attributesMap.put("label", e.getLabel());
          attributesMap.put("type", e.getType());
          attributesMap.put("location", e.getLocation());
          if (e instanceof Entity entity)
            attributesMap.put("value", entity.getValue());
          attributesMap.put("other", e.getOther());

          return attributesMap;
        })
        .flatMap(attrs -> Mono.fromCallable(() -> OBJECT_MAPPER.writeValueAsString(attrs)))
        .switchIfEmpty(Mono.just("{}"))
        .onErrorMap(e -> new InternalApplicationException("Can not serialize Entity attributes to JSON", e));

    return Mono.justOrEmpty(document)
        .flatMap(getBundle)
        .flatMap(bundle -> {
          Map<Class<?>, List<Statement>> statements = getIndexedStatements.apply(bundle.getStatement());

          Mono<Map<String, EntityNode>> entitiesMono = Flux
              .fromIterable(statements.getOrDefault(Entity.class, List.of()))
              .cast(Entity.class)
              .flatMap((Entity e) -> getAttributesAsJsonString.apply(e)
                  .map(attrs -> Map.entry(e.getId().getLocalPart(), new EntityNode(e.getId().getLocalPart(), attrs))))
              .collectMap(Map.Entry::getKey, Map.Entry::getValue);

          Mono<Map<String, AgentNode>> agentsMono = Flux.fromIterable(statements.getOrDefault(Agent.class, List.of()))
              .cast(Agent.class)
              .flatMap(a -> getAttributesAsJsonString.apply(a)
                  .map(attrs -> Map.entry(a.getId().getLocalPart(), new AgentNode(a.getId().getLocalPart(), attrs))))
              .collectMap(Map.Entry::getKey, Map.Entry::getValue);

          Mono<Map<String, ActivityNode>> activitiesMono = Flux
              .fromIterable(statements.getOrDefault(Activity.class, List.of()))
              .cast(Activity.class)
              .flatMap(a -> getAttributesAsJsonString.apply(a)
                  .map(attrs -> Map.entry(a.getId().getLocalPart(),
                      new ActivityNode(a.getId().getLocalPart(), String.valueOf(a.getStartTime()),
                          String.valueOf(a.getEndTime()), attrs))))
              .collectMap(Map.Entry::getKey, Map.Entry::getValue);

          return Mono.zip(entitiesMono, agentsMono, activitiesMono)
              .map(nodes -> {
                Map<String, EntityNode> entities = nodes.getT1();
                Map<String, AgentNode> agents = nodes.getT2();
                Map<String, ActivityNode> activities = nodes.getT3();

                statements.getOrDefault(WasDerivedFrom.class, List.of()).stream()
                    .map(WasDerivedFrom.class::cast)
                    .forEach(wdf -> {
                      entities.replace(
                          wdf.getGeneratedEntity().getLocalPart(),
                          entities.get(wdf.getGeneratedEntity().getLocalPart())
                              .wihtRevisionOfEntity(entities.get(wdf.getUsedEntity().getLocalPart())));
                    });

                statements.getOrDefault(SpecializationOf.class, List.of()).stream()
                    .map(SpecializationOf.class::cast)
                    .forEach(sOf -> {
                      entities.replace(
                          sOf.getSpecificEntity().getLocalPart(),
                          entities.get(sOf.getSpecificEntity().getLocalPart())
                              .withSpecializationOfEntity(entities.get(sOf.getGeneralEntity().getLocalPart())));
                    });

                statements.getOrDefault(WasAttributedTo.class, List.of()).stream()
                    .map(WasAttributedTo.class::cast)
                    .forEach(wat -> {
                      entities.replace(
                          wat.getEntity().getLocalPart(),
                          entities.get(wat.getEntity().getLocalPart())
                              .withWasAttributedToAgent(agents.get(wat.getAgent().getLocalPart())));
                    });

                statements.getOrDefault(WasAssociatedWith.class, List.of()).stream()
                    .map(WasAssociatedWith.class::cast)
                    .forEach(waw -> {
                      activities.replace(
                          waw.getActivity().getLocalPart(),
                          activities.get(waw.getActivity().getLocalPart())
                              .withWasAssociatedWithAgent(agents.get(waw.getAgent().getLocalPart())));
                    });

                statements.getOrDefault(WasGeneratedBy.class, List.of()).stream()
                    .map(WasGeneratedBy.class::cast)
                    .forEach(wgb -> {
                      entities.replace(
                          wgb.getEntity().getLocalPart(),
                          entities.get(wgb.getEntity().getLocalPart())
                              .withWasGeneratedByActivity(activities.get(wgb.getActivity().getLocalPart())));
                    });
                return new BundleNode(
                    bundle.getId().getLocalPart(),
                    entities.values(),
                    agents.values(),
                    activities.values());
              });
        });
  }
}
