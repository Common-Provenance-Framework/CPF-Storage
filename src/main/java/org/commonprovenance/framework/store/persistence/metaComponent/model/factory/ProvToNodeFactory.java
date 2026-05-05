package org.commonprovenance.framework.store.persistence.metaComponent.model.factory;

import static org.commonprovenance.framework.store.common.publisher.PublisherHelper.MONO;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.datatype.XMLGregorianCalendar;

import org.commonprovenance.framework.store.exceptions.InternalApplicationException;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Agent;
import org.openprovenance.prov.model.Attribute;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Element;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.LangString;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.SpecializationOf;
import org.openprovenance.prov.model.Statement;
import org.openprovenance.prov.model.Used;
import org.openprovenance.prov.model.WasAssociatedWith;
import org.openprovenance.prov.model.WasAttributedTo;
import org.openprovenance.prov.model.WasDerivedFrom;
import org.openprovenance.prov.model.WasGeneratedBy;
import org.openprovenance.prov.vanilla.ProvFactory;

import reactor.core.publisher.Mono;

public class ProvToNodeFactory {
  private static final ProvFactory provFactory = new org.openprovenance.prov.vanilla.ProvFactory();

  public static EntityNode toEntity(Entity entity) {
    return new EntityNode(
        entity.getId().getLocalPart(),
        ProvToNodeFactory.getType(entity),
        ProvToNodeFactory.getCpmAttributes(entity),
        ProvToNodeFactory.getPavAttributes(entity));
  }

  public static AgentNode toEntity(Agent agent) {
    return new AgentNode(
        agent.getId().getLocalPart(),
        ProvToNodeFactory.getType(agent),
        ProvToNodeFactory.getCpmAttributes(agent));
  }

  public static ActivityNode toEntity(Activity activity) {
    return new ActivityNode(
        activity.getId().getLocalPart(),
        ProvToNodeFactory.getType(activity),
        Optional.ofNullable(activity.getStartTime())
            .map(XMLGregorianCalendar::toString).orElse(""),
        Optional.ofNullable(activity.getEndTime())
            .map(XMLGregorianCalendar::toString).orElse(""),
        ProvToNodeFactory.getCpmAttributes(activity));
  }

  public static Mono<BundleNode> toEntity(Document document) {

    Function<Document, Mono<Bundle>> getBundle = (Document doc) -> Mono.justOrEmpty(doc)
        .map(Document::getStatementOrBundle)
        .flatMap(MONO.makeSure(
            statements -> statements.size() == 1,
            statements -> new InternalApplicationException(
                "Document should have exact one Statement, but has " + statements.size() + "!")))
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
                LinkedHashMap::new,
                Collectors.toList()));

    return Mono.justOrEmpty(document)
        .flatMap(getBundle)
        .map((Bundle bundle) -> {
          Map<Class<?>, List<Statement>> statements = getIndexedStatements.apply(bundle.getStatement());

          Map<String, EntityNode> entities = statements.getOrDefault(Entity.class, List.of())
              .stream()
              .map(Entity.class::cast)
              .map((Entity e) -> Map.entry(
                  e.getId().getLocalPart(),
                  ProvToNodeFactory.toEntity(e)))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

          Map<String, AgentNode> agents = statements.getOrDefault(Agent.class, List.of())
              .stream()
              .map(Agent.class::cast)
              .map(a -> Map.entry(
                  a.getId().getLocalPart(),
                  ProvToNodeFactory.toEntity(a)))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

          Map<String, ActivityNode> activities = statements.getOrDefault(Activity.class, List.of())
              .stream()
              .map(Activity.class::cast)
              .map(a -> Map.entry(
                  a.getId().getLocalPart(),
                  ProvToNodeFactory.toEntity(a)))
              .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

          statements.getOrDefault(WasDerivedFrom.class, List.of()).stream()
              .map(WasDerivedFrom.class::cast)
              .forEach(wdf -> {
                entities.replace(
                    wdf.getGeneratedEntity().getLocalPart(),
                    entities.get(wdf.getGeneratedEntity().getLocalPart())
                        .withRevisionOfEntity(entities.get(wdf.getUsedEntity().getLocalPart())));
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

          statements.getOrDefault(Used.class, List.of()).stream()
              .map(Used.class::cast)
              .forEach(used -> {
                activities.replace(
                    used.getActivity().getLocalPart(),
                    activities.get(used.getActivity().getLocalPart())
                        .withUsedEntity(entities.get(used.getEntity().getLocalPart())));
              });

          statements.getOrDefault(WasGeneratedBy.class, List.of()).stream()
              .map(WasGeneratedBy.class::cast)
              .forEach(wgb -> {
                entities.replace(
                    wgb.getEntity().getLocalPart(),
                    entities.get(wgb.getEntity().getLocalPart())
                        .withWasGeneratedByActivity(activities.get(wgb.getActivity().getLocalPart())));
              });

          return new BundleNode(bundle.getId().getLocalPart())
              .withEntities(entities.values())
              .withActivities(activities.values())
              .withAgents(agents.values());
        });
  }

  private static Map<String, Object> getCpmAttributes(Element element) {
    return element.getOther().stream()
        .filter(attr -> attr.getElementName().getPrefix().equals("cpm"))
        .map(ProvToNodeFactory::attributeToMapEntry)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static Map<String, Object> getPavAttributes(Element element) {
    return element.getOther().stream()
        .filter(attr -> attr.getElementName().getPrefix().equals("pav"))
        .map(ProvToNodeFactory::attributeToMapEntry)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static String getType(Element element) {
    Map<String, List<Object>> types = element.getType().stream()
        .map(ProvToNodeFactory::attributeToMapEntry)
        .collect(Collectors.groupingBy(
            Map.Entry::getKey,
            LinkedHashMap::new,
            Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

    return types.isEmpty()
        ? ""
        : types.get("type").stream()
            .map(String.class::cast)
            .reduce("", (_, item) -> item);

  }

  private static Map.Entry<String, Object> attributeToMapEntry(Attribute attr) {
    String name = attr.getElementName().getLocalPart();
    Object value = attr.getValue();

    if (value instanceof LangString ls)
      return Map.entry(name, ProvToNodeFactory.getLangStringValue(ls));
    else if (value instanceof QualifiedName qn)
      return Map.entry(name, ProvToNodeFactory.getQualifiedNameValue(qn));
    else
      return Map.entry(name, ProvToNodeFactory.getAttributeValue(attr));
  };

  private static Object getLangStringValue(LangString ls) {
    return ls.getValue() + (ls.getLang() == null ? "" : "@" + ls.getLang());
  }

  private static Object getQualifiedNameValue(QualifiedName qn) {
    return qn.getPrefix() + ":" + qn.getLocalPart();
  }

  private static Object getAttributeValue(Attribute attr) {
    if (attr.getType().equals(provFactory.getName().XSD_INT)
        || attr.getType().equals(provFactory.getName().XSD_INTEGER))
      return Integer.parseInt(attr.getValue().toString());
    else if (attr.getType().equals(provFactory.getName().XSD_LONG))
      return Long.parseLong(attr.getValue().toString());
    else if (attr.getType().equals(provFactory.getName().XSD_BOOLEAN))
      return Boolean.parseBoolean(attr.getValue().toString());
    else if (attr.getType().equals(provFactory.getName().XSD_DOUBLE))
      return Double.parseDouble(attr.getValue().toString());
    else
      return attr.getValue().toString();
  }
}
