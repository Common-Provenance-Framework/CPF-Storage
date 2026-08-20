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
import org.openprovenance.prov.model.Bundle;
import org.openprovenance.prov.model.Document;
import org.openprovenance.prov.model.Element;
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

  public static Function<BundleNode, Mono<Document>> bundleToProv(AppConfiguration config) {
    return (BundleNode node) -> {
      Document provDocument = NodeToProvFactory.provFactory.newDocument();
      provDocument.getNamespace().addKnownNamespaces();
      provDocument.getNamespace().register(CpmNamespaceConstants.CPM_PREFIX, CpmNamespaceConstants.CPM_NS);
      provDocument.getNamespace().register("pav", "http://purl.org/pav/");
      provDocument.getNamespace().register("meta", config.getFqdn() + "documents/meta/");
      provDocument.getNamespace().register("storage", config.getFqdn() + "documents/");

      QualifiedName bundleId = NodeToProvFactory.provFactory.newQualifiedName(
          provDocument.getNamespace().getPrefixes().get("meta"),
          node.getIdentifier(),
          "meta");

      Stream<Statement> provNodeStatements = node.getAllNodes().stream()
          .map(NodeToProvFactory.toProvenance(provDocument.getNamespace()));

      Stream<Statement> provRelatioStatements = node.getAllNodes().stream()
          .flatMap(n -> {
            if (n instanceof ActivityNode activityNode) {
              Stream<Statement> usedStream = activityNode.getUsed().stream()
                  .map(used -> (Statement) provFactory.newUsed(
                      NodeToProvFactory.getStorageQN(activityNode.getIdentifier(), provDocument.getNamespace()),
                      NodeToProvFactory.getStorageQN(used.getEntity().getIdentifier(), provDocument.getNamespace())));

              Stream<Statement> wasAssociatedWithStream = activityNode.getWasAssociatedWith().stream()
                  .map(waw -> (Statement) provFactory.newWasAssociatedWith(
                      null,
                      NodeToProvFactory.getStorageQN(activityNode.getIdentifier(), provDocument.getNamespace()),
                      NodeToProvFactory.getStorageQN(waw.getAgent().getIdentifier(), provDocument.getNamespace())));
              return Stream.concat(usedStream, wasAssociatedWithStream);
            } else if (n instanceof EntityNode entityNode) {
              Stream<Statement> wdfStream = entityNode.getRevisionOf().stream()
                  .map(rev -> provFactory.newWasDerivedFrom(
                      NodeToProvFactory.getStorageQN(entityNode.getIdentifier(), provDocument.getNamespace()),
                      NodeToProvFactory.getStorageQN(rev.getEntity().getIdentifier(), provDocument.getNamespace())))
                  .map(wdf -> {
                    wdf.getType().add(provFactory.newType(
                        provFactory.getName().PROV_REVISION,
                        provFactory.getName().PROV_QUALIFIED_NAME));
                    return (Statement) wdf;
                  });

              Stream<Statement> soStream = entityNode.getSpecializationOf().stream()
                  .map(so -> (Statement) provFactory.newSpecializationOf(
                      NodeToProvFactory.getStorageQN(entityNode.getIdentifier(), provDocument.getNamespace()),
                      NodeToProvFactory.getStorageQN(so.getEntity().getIdentifier(), provDocument.getNamespace())));

              Stream<Statement> watStream = entityNode.getWasAttributedTo().stream()
                  .map(wat -> (Statement) provFactory.newWasAttributedTo(
                      null,
                      NodeToProvFactory.getStorageQN(entityNode.getIdentifier(), provDocument.getNamespace()),
                      NodeToProvFactory.getStorageQN(wat.getAgent().getIdentifier(), provDocument.getNamespace())));

              Stream<Statement> wgbStream = entityNode.getWasGeneratedBy().stream()
                  .map(wgb -> (Statement) provFactory.newWasAttributedTo(
                      null,
                      NodeToProvFactory.getStorageQN(entityNode.getIdentifier(), provDocument.getNamespace()),
                      NodeToProvFactory.getStorageQN(wgb.getActivity().getIdentifier(), provDocument.getNamespace())));

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

  private static Function<BaseProvClassNode, Statement> toProvenance(Namespace ns) {
    return (BaseProvClassNode node) -> {
      QualifiedName elementIdentifier = NodeToProvFactory.getStorageQN(node.getIdentifier(), ns);

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

  private static QualifiedName getStorageQN(String localPart, Namespace ns) {
    return NodeToProvFactory.provFactory.newQualifiedName(
        ns.getPrefixes().get("storage"),
        localPart,
        "storage");
  }

  private static QualifiedName getPavQN(String localPart, Namespace ns) {
    return NodeToProvFactory.provFactory.newQualifiedName(
        ns.getPrefixes().get("pav"),
        localPart,
        "pav");
  }

  private static QualifiedName getCpmQN(String localPart, Namespace ns) {
    return NodeToProvFactory.provFactory.newQualifiedName(
        ns.getPrefixes().get("cpm"),
        localPart,
        "cpm");
  }

  private static void applyCpmAttributesToElement(
      Element element,
      BaseProvClassNode node,
      Namespace ns) {

    node.getCpm().entrySet().stream()
        .map(entry -> (NodeToProvFactory.provFactory.newAttribute(
            NodeToProvFactory.getCpmQN(entry.getKey(), ns),
            NodeToProvFactory.asPlainString(entry.getValue()),
            NodeToProvFactory.provFactory.getName().XSD_STRING)))
        .map(Other.class::cast)
        .forEach(element.getOther()::add);

  }

  private static String asPlainString(Object value) {
    if (value == null) {
      return "";
    }

    String stringValue = value.toString();
    if (stringValue.length() >= 2 && stringValue.startsWith("\"") && stringValue.endsWith("\"")) {
      return stringValue.substring(1, stringValue.length() - 1);
    }

    return stringValue;
  }

  private static void applyPavAttributesToElement(
      Element element,
      EntityNode node,
      Namespace ns) {

    node.getPav().entrySet().stream()
        .forEach(entry -> element.getOther().add(((Other) NodeToProvFactory.provFactory.newAttribute(
            NodeToProvFactory.getPavQN(entry.getKey(), ns),
            entry.getValue().toString(),
            NodeToProvFactory.provFactory.getName().XSD_INTEGER))));
  }

  private static Type getTypeFromString(String value, Namespace ns) {
    String[] partsQN = value.split(":", 2);
    Map<String, String> prefixes = ns.getPrefixes();

    if (partsQN.length == 2 && prefixes.containsKey(partsQN[0]))
      return NodeToProvFactory.provFactory.newType(
          NodeToProvFactory.provFactory.newQualifiedName(prefixes.get(partsQN[0]), partsQN[1], partsQN[0]),
          NodeToProvFactory.provFactory.getName().PROV_QUALIFIED_NAME);
    else
      return NodeToProvFactory.provFactory.newType(
          value.toString(),
          NodeToProvFactory.provFactory.getName().XSD_STRING);

  }

}
