package org.commonprovenance.framework.store.persistence.metaComponent.model.factory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BaseProvClassNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.relation.Contains;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Element;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.LangString;
import org.openprovenance.prov.model.Namespace;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.SpecializationOf;
import org.openprovenance.prov.model.Statement;
import org.openprovenance.prov.model.Used;
import org.openprovenance.prov.model.WasAssociatedWith;
import org.openprovenance.prov.model.WasAttributedTo;
import org.openprovenance.prov.model.WasDerivedFrom;

import cz.muni.fi.cpm.constants.CpmNamespaceConstants;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

public class ProvenanceFactory {
  private static final ProvFactory provFactory = new org.openprovenance.prov.vanilla.ProvFactory();
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static Function<BundleNode, Mono<Document>> toProv(AppConfiguration config) {
    return (BundleNode node) -> {
      Document provDocument = ProvenanceFactory.provFactory.newDocument();
      provDocument.getNamespace().addKnownNamespaces();
      provDocument.getNamespace().register(CpmNamespaceConstants.CPM_PREFIX, CpmNamespaceConstants.CPM_NS);
      provDocument.getNamespace().register("pav", "http://purl.org/pav/");
      provDocument.getNamespace().register("meta", config.getFqdn() + "documents/meta/");
      provDocument.getNamespace().register("storage", config.getFqdn() + "documents/");

      QualifiedName bundleId = ProvenanceFactory.provFactory.newQualifiedName(
          config.getFqdn() + "documents/meta/",
          node.getId(),
          "mata");

      Stream<Statement> provNodeStatements = node.getContains().stream()
          .map(Contains::getNode)
          .map(ProvenanceFactory.toProvenance(provDocument.getNamespace()));

      Stream<Statement> provRelatioStatements = node.getContains().stream()
          .map(Contains::getNode)
          .flatMap(n -> {
            if (n instanceof ActivityNode activityNode) {
              Stream<Used> usedStream = activityNode.getUsed().stream()
                  .map(used -> provFactory.newUsed(
                      ProvenanceFactory.getId(activityNode.getId(), provDocument.getNamespace()),
                      ProvenanceFactory.getId(used.getEntity().getId(), provDocument.getNamespace())));

              Stream<WasAssociatedWith> wasAssociatedWithStream = activityNode.getWasAssociatedWith().stream()
                  .map(waw -> provFactory.newWasAssociatedWith(
                      null,
                      ProvenanceFactory.getId(activityNode.getId(), provDocument.getNamespace()),
                      ProvenanceFactory.getId(waw.getAgent().getId(), provDocument.getNamespace())));
              return Stream.concat(usedStream, wasAssociatedWithStream);
            } else if (n instanceof EntityNode entityNode) {
              Stream<WasDerivedFrom> wdfStream = entityNode.getRevisionOf().stream()
                  .map(rev -> provFactory.newWasDerivedFrom(
                      ProvenanceFactory.getId(entityNode.getId(), provDocument.getNamespace()),
                      ProvenanceFactory.getId(rev.getEntity().getId(), provDocument.getNamespace())))
                  .map(wdf -> {
                    wdf.getType().add(provFactory.newType(provFactory.getName().PROV_REVISION,
                        provFactory.getName().PROV_QUALIFIED_NAME));
                    return wdf;
                  });

              Stream<SpecializationOf> soStream = entityNode.getSpecializationOf().stream()
                  .map(so -> provFactory.newSpecializationOf(
                      ProvenanceFactory.getId(entityNode.getId(), provDocument.getNamespace()),
                      ProvenanceFactory.getId(so.getEntity().getId(), provDocument.getNamespace())));

              Stream<WasAttributedTo> watStream = entityNode.getWasAttributedTo().stream()
                  .map(wat -> provFactory.newWasAttributedTo(
                      null,
                      ProvenanceFactory.getId(entityNode.getId(), provDocument.getNamespace()),
                      ProvenanceFactory.getId(wat.getAgent().getId(), provDocument.getNamespace())));

              Stream<WasAttributedTo> wgbStream = entityNode.getWasGeneratedBy().stream()
                  .map(wgb -> provFactory.newWasAttributedTo(
                      null,
                      ProvenanceFactory.getId(entityNode.getId(), provDocument.getNamespace()),
                      ProvenanceFactory.getId(wgb.getActivity().getId(), provDocument.getNamespace())));

              return Stream.of(wdfStream, soStream, watStream, wgbStream)
                  .flatMap(Function.identity());
            }
            return List.<Statement>of().stream();
          });

      List<Statement> statements = Stream.concat(provNodeStatements, provRelatioStatements)
          .toList();
      Bundle bundle = ProvenanceFactory.provFactory.newNamedBundle(bundleId, statements);
      provDocument.getStatementOrBundle().add(bundle);

      return Mono.just(provDocument);
    };
  }

  private static QualifiedName getId(String id, Namespace ns) {
    return ProvenanceFactory.provFactory.newQualifiedName(
        ns.getNamespaces().get("storage"),
        id,
        "storage");
  }

  private static Function<BaseProvClassNode, Statement> toProvenance(Namespace ns) {
    return (BaseProvClassNode node) -> {
      QualifiedName elementId = ProvenanceFactory.getId(node.getId(), ns);

      Element element;
      if (node instanceof EntityNode _) {
        element = ProvenanceFactory.provFactory.newEntity(elementId);
      } else if (node instanceof ActivityNode _) {
        element = ProvenanceFactory.provFactory.newActivity(elementId);
      } else {
        element = ProvenanceFactory.provFactory.newAgent(elementId);
      }
      ProvenanceFactory.applyAttributesJsonToEntity(element, node.getAttributes(), ns);
      return element;
    };
  }

  private static void applyAttributesJsonToEntity(
      Element element,
      String attributesJson,
      Namespace ns) {
    try {
      Map<String, Object> attrs = ProvenanceFactory.OBJECT_MAPPER.readValue(
          attributesJson, new tools.jackson.core.type.TypeReference<Map<String, Object>>() {
          });

      attrs.entrySet().stream()
          .forEach(entry -> {
            String key = entry.getKey();
            Object rawValue = entry.getValue();
            List<Object> values = (rawValue instanceof List<?>)
                ? ((List<?>) rawValue).stream().map(Object.class::cast).toList()
                : List.of(rawValue);

            QualifiedName attrName = ProvenanceFactory.toQualifiedName(key, ns);

            if (attrName.equals(provFactory.getName().PROV_TYPE)) {
              values.forEach(value -> {
                element.getType()
                    .add(provFactory.newType(ProvenanceFactory.toValue(String.valueOf(value), ns), attrName));
              });
            } else if (attrName.equals(provFactory.getName().PROV_LABEL)) {
              values.forEach(value -> {
                element.getLabel().add(ProvenanceFactory.toLangString(String.valueOf(value)));
              });
            } else if (attrName.equals(provFactory.getName().PROV_LOCATION)) {
              values.forEach(value -> {
                element.getLocation()
                    .add(provFactory.newLocation(ProvenanceFactory.toValue(String.valueOf(value), ns), attrName));
              });
            } else if (attrName.equals(provFactory.getName().PROV_VALUE) && element instanceof Entity entity) {
              values.forEach(value -> {
                String stringValue = String.valueOf(value);
                if (isInteger(stringValue)) {
                  entity.setValue(provFactory.newValue(Integer.parseInt(stringValue)));
                } else {
                  entity.setValue(provFactory.newValue(stringValue));
                }
              });
            } else {
              values.forEach(value -> {
                element.getOther().add(provFactory.newOther(
                    attrName,
                    value,
                    ProvenanceFactory.guessDatatype(value, provFactory)));
              });
            }
          });
    } catch (Exception e) {
      throw new RuntimeException("Cannot parse entity attributes JSON", e);
    }
  }

  private static boolean isInteger(String value) {
    if (value == null || value.isBlank())
      return false;
    try {
      Integer.parseInt(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  private static QualifiedName guessDatatype(Object value, ProvFactory provFactory) {
    if (value instanceof Integer)
      return provFactory.getName().XSD_INT;
    if (value instanceof Long)
      return provFactory.getName().XSD_LONG;
    if (value instanceof Boolean)
      return provFactory.getName().XSD_BOOLEAN;
    if (value instanceof Number)
      return provFactory.getName().XSD_DOUBLE;
    return provFactory.getName().XSD_STRING;
  }

  private static QualifiedName toQualifiedName(String value, Namespace ns) {
    String[] parts = value.split(":", 2);
    Map<String, String> prefixes = ns.getPrefixes();
    if (parts.length == 2 && prefixes.containsKey(parts[0])) {
      return ProvenanceFactory.provFactory
          .newQualifiedName(prefixes.get(parts[0]), parts[1], parts[0]);
    }
    return ProvenanceFactory.provFactory
        .newQualifiedName(ProvenanceFactory.provFactory.getName().XSD_STRING.getNamespaceURI(),
            "string",
            "xsd");
  }

  private static LangString toLangString(String value) {
    String[] parts = value.split("@", 2);
    return parts[1].isEmpty()
        ? ProvenanceFactory.provFactory.newInternationalizedString(parts[0])
        : ProvenanceFactory.provFactory.newInternationalizedString(parts[0], parts[1]);

  }

  private static Object toValue(String value, Namespace ns) {
    if (value.contains(":")) {
      return ProvenanceFactory.toQualifiedName(value, ns);
    } else if (value.contains("@")) {
      return ProvenanceFactory.toLangString(value);
    } else {
      return (String) value;
    }
  }
}
