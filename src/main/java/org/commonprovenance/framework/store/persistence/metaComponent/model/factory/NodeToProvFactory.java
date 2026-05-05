package org.commonprovenance.framework.store.persistence.metaComponent.model.factory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BaseProvClassNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Attribute;
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Element;
import org.openprovenance.prov.model.Entity;
import org.openprovenance.prov.model.Namespace;
import org.openprovenance.prov.model.Other;
import org.openprovenance.prov.model.ProvFactory;
import org.openprovenance.prov.model.QualifiedName;
import org.openprovenance.prov.model.Statement;
import org.openprovenance.prov.model.Type;

import cz.muni.fi.cpm.constants.CpmNamespaceConstants;
import reactor.core.publisher.Mono;

public class NodeToProvFactory {
  private static final ProvFactory provFactory = new org.openprovenance.prov.vanilla.ProvFactory();

  public static Function<EntityNode, Mono<Entity>> entityToProv(AppConfiguration config) {
    Namespace namespace = provFactory.newNamespace();
    namespace.addKnownNamespaces();
    namespace.register(CpmNamespaceConstants.CPM_PREFIX, CpmNamespaceConstants.CPM_NS);
    namespace.register("pav", "http://purl.org/pav/");
    namespace.register("meta", config.getFqdn() + "documents/meta/");
    namespace.register("storage", config.getFqdn() + "documents/");

    return (EntityNode entity) -> Mono.justOrEmpty(entity)
        .map(NodeToProvFactory.toProvenance(namespace))
        .filter(Entity.class::isInstance)
        .map(Entity.class::cast);
  }

  public static Function<BundleNode, Mono<Document>> bundleToProv(AppConfiguration config) {
    return (BundleNode node) -> {
      Document provDocument = NodeToProvFactory.provFactory.newDocument();
      provDocument.getNamespace().addKnownNamespaces();
      provDocument.getNamespace().register(CpmNamespaceConstants.CPM_PREFIX, CpmNamespaceConstants.CPM_NS);
      provDocument.getNamespace().register("pav", "http://purl.org/pav/");
      provDocument.getNamespace().register("meta", config.getFqdn() + "documents/meta/");
      provDocument.getNamespace().register("storage", config.getFqdn() + "documents/");

      QualifiedName bundleId = NodeToProvFactory.provFactory.newQualifiedName(
          config.getFqdn() + "documents/meta/",
          node.getIdentifier(),
          "mata");

      Stream<Statement> provNodeStatements = node.getAllNodes().stream()
          .map(NodeToProvFactory.toProvenance(provDocument.getNamespace()));

      Stream<Statement> provRelatioStatements = node.getAllNodes().stream()
          .flatMap(n -> {
            if (n instanceof ActivityNode activityNode) {
              Stream<Statement> usedStream = activityNode.getUsed().stream()
                  .map(used -> (Statement) provFactory.newUsed(
                      NodeToProvFactory.getIdentifier(activityNode.getIdentifier(), provDocument.getNamespace()),
                      NodeToProvFactory.getIdentifier(used.getEntity().getIdentifier(), provDocument.getNamespace())));

              Stream<Statement> wasAssociatedWithStream = activityNode.getWasAssociatedWith().stream()
                  .map(waw -> (Statement) provFactory.newWasAssociatedWith(
                      null,
                      NodeToProvFactory.getIdentifier(activityNode.getIdentifier(), provDocument.getNamespace()),
                      NodeToProvFactory.getIdentifier(waw.getAgent().getIdentifier(), provDocument.getNamespace())));
              return Stream.concat(usedStream, wasAssociatedWithStream);
            } else if (n instanceof EntityNode entityNode) {
              Stream<Statement> wdfStream = entityNode.getRevisionOf().stream()
                  .map(rev -> provFactory.newWasDerivedFrom(
                      NodeToProvFactory.getIdentifier(entityNode.getIdentifier(), provDocument.getNamespace()),
                      NodeToProvFactory.getIdentifier(rev.getEntity().getIdentifier(), provDocument.getNamespace())))
                  .map(wdf -> {
                    wdf.getType().add(provFactory.newType(
                        provFactory.getName().PROV_REVISION,
                        provFactory.getName().PROV_QUALIFIED_NAME));
                    return (Statement) wdf;
                  });

              Stream<Statement> soStream = entityNode.getSpecializationOf().stream()
                  .map(so -> (Statement) provFactory.newSpecializationOf(
                      NodeToProvFactory.getIdentifier(entityNode.getIdentifier(), provDocument.getNamespace()),
                      NodeToProvFactory.getIdentifier(so.getEntity().getIdentifier(), provDocument.getNamespace())));

              Stream<Statement> watStream = entityNode.getWasAttributedTo().stream()
                  .map(wat -> (Statement) provFactory.newWasAttributedTo(
                      null,
                      NodeToProvFactory.getIdentifier(entityNode.getIdentifier(), provDocument.getNamespace()),
                      NodeToProvFactory.getIdentifier(wat.getAgent().getIdentifier(), provDocument.getNamespace())));

              Stream<Statement> wgbStream = entityNode.getWasGeneratedBy().stream()
                  .map(wgb -> (Statement) provFactory.newWasAttributedTo(
                      null,
                      NodeToProvFactory.getIdentifier(entityNode.getIdentifier(), provDocument.getNamespace()),
                      NodeToProvFactory.getIdentifier(wgb.getActivity().getIdentifier(), provDocument.getNamespace())));

              return Stream.of(wdfStream, soStream, watStream, wgbStream)
                  .flatMap(Function.identity());
            }
            return List.<Statement> of().stream();
          });

      List<Statement> statements = Stream.concat(provNodeStatements, provRelatioStatements)
          .toList();
      Bundle bundle = NodeToProvFactory.provFactory.newNamedBundle(bundleId, statements);
      provDocument.getStatementOrBundle().add(bundle);

      return Mono.just(provDocument);
    };
  }

  private static QualifiedName getIdentifier(String localPart, Namespace ns) {
    return NodeToProvFactory.provFactory.newQualifiedName(
        ns.getPrefixes().get("storage"),
        localPart,
        "storage");
  }

  private static Function<BaseProvClassNode, Statement> toProvenance(Namespace ns) {
    return (BaseProvClassNode node) -> {
      QualifiedName elementIdentifier = NodeToProvFactory.getIdentifier(node.getIdentifier(), ns);

      Element element;
      if (node instanceof EntityNode e) {
        element = NodeToProvFactory.provFactory.newEntity(elementIdentifier);
        NodeToProvFactory.applyPavAttributesToElement(element, e, ns);
      } else if (node instanceof ActivityNode ag) {
        element = NodeToProvFactory.provFactory.newActivity(elementIdentifier);
        try {
          DatatypeFactory dtf = DatatypeFactory.newInstance();
          ((Activity) element).setStartTime(dtf.newXMLGregorianCalendar(ag.getStartTime()));
          ((Activity) element).setEndTime(dtf.newXMLGregorianCalendar(ag.getEndTime()));
        } catch (DatatypeConfigurationException e) {
          throw new RuntimeException(e);
        }
      } else {
        element = NodeToProvFactory.provFactory.newAgent(elementIdentifier);
      }

      element.getType().add(NodeToProvFactory.getTypeFromString(node.getProvType(), ns));

      NodeToProvFactory.applyCpmAttributesToElement(element, node, ns);
      return element;
    };
  }

  private static void applyCpmAttributesToElement(
      Element element,
      BaseProvClassNode node,
      Namespace ns) {

    node.getCpm().entrySet().stream()
        .forEach(entry -> {
          Map<String, String> prefixes = ns.getPrefixes();

          element.getOther().add(((Other) guessAttribute(
              NodeToProvFactory.provFactory.newQualifiedName(prefixes.get("cpm"), entry.getKey(), "cpm"),
              entry.getValue())));

        });
  }

  private static void applyPavAttributesToElement(
      Element element,
      EntityNode node,
      Namespace ns) {

    node.getPav().entrySet().stream()
        .forEach(entry -> {
          Map<String, String> prefixes = ns.getPrefixes();

          element.getOther().add(((Other) guessAttribute(
              NodeToProvFactory.provFactory.newQualifiedName(prefixes.get("pav"), entry.getKey(), "pav"),
              entry.getValue())));

        });
  }

  private static Type getTypeFromString(String value, Namespace ns) {
    String[] partsQN = value.split(":", 2);
    String[] partsLS = value.split("@", 2);
    Map<String, String> prefixes = ns.getPrefixes();

    if (partsQN.length == 2 && prefixes.containsKey(partsQN[0]))
      return NodeToProvFactory.provFactory.newType(
          NodeToProvFactory.provFactory.newQualifiedName(prefixes.get(partsQN[0]), partsQN[1], partsQN[0]),
          NodeToProvFactory.provFactory.getName().PROV_QUALIFIED_NAME);
    else if (partsLS.length == 2)
      return NodeToProvFactory.provFactory.newType(
          NodeToProvFactory.provFactory.newInternationalizedString(partsLS[0], partsLS[1]),
          NodeToProvFactory.provFactory.getName().PROV_LANG_STRING);
    else if (partsQN.length == 2 || partsLS.length == 2)
      return NodeToProvFactory.provFactory.newType(
          value,
          NodeToProvFactory.provFactory.getName().XSD_STRING);
    else
      return ((Type) NodeToProvFactory.guessAttribute(
          NodeToProvFactory.provFactory.newQualifiedName(prefixes.get("prov"), "type", "prov"),
          value));
  }

  private static Attribute guessAttribute(QualifiedName elementName, Object value) {
    if (value instanceof Integer)
      return NodeToProvFactory.provFactory.newAttribute(elementName, value,
          NodeToProvFactory.provFactory.getName().XSD_INT);
    else if (value instanceof Long)
      return NodeToProvFactory.provFactory.newAttribute(elementName, value,
          NodeToProvFactory.provFactory.getName().XSD_LONG);
    else if (value instanceof Boolean)
      return NodeToProvFactory.provFactory.newAttribute(elementName, value,
          NodeToProvFactory.provFactory.getName().XSD_BOOLEAN);
    else if (value instanceof Double)
      return NodeToProvFactory.provFactory.newAttribute(elementName, value,
          NodeToProvFactory.provFactory.getName().XSD_DOUBLE);
    else
      return NodeToProvFactory.provFactory.newAttribute(elementName, value,
          NodeToProvFactory.provFactory.getName().XSD_STRING);
  }

}
