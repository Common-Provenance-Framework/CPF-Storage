package org.commonprovenance.framework.store.persistence.metaComponent.model.factory;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.commonprovenance.framework.store.common.utils.ProvDocumentUtils;
import org.commonprovenance.framework.store.config.AppConfiguration;
import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InvalidValueException;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.ActivityNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.AgentNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BaseProvClassNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.BundleNode;
import org.commonprovenance.framework.store.persistence.metaComponent.model.node.EntityNode;
import org.openprovenance.prov.model.Activity;
import org.openprovenance.prov.model.Agent;
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
import org.openprovenance.prov.model.ValueConverter;

import cz.muni.fi.cpm.constants.CpmNamespaceConstants;
import io.vavr.control.Either;
import reactor.core.publisher.Mono;

public class NodeToProvFactory {
  private static final ProvFactory provFactory = new org.openprovenance.prov.vanilla.ProvFactory();

  public static Function<EntityNode, Either<ApplicationException, Entity>> entityToProv(AppConfiguration config) {
    // Namespace namespace = provFactory.newNamespace();
    // namespace.addKnownNamespaces();
    // namespace.register(CpmNamespaceConstants.CPM_PREFIX, CpmNamespaceConstants.CPM_NS);
    // namespace.register("pav", "http://purl.org/pav/");
    // namespace.register("meta", config.getFqdn() + "documents/meta/");
    // namespace.register("storage", config.getFqdn() + "documents/");

    return (EntityNode entityNode) -> Either.<ApplicationException, EntityNode> right(entityNode)
        .map(NodeToProvFactory.getIdentifier(config))
        .map(NodeToProvFactory.provFactory::newEntity)
        .flatMap(NodeToProvFactory.setTypeAttribute(entityNode))
        .map(NodeToProvFactory.setPavAttributes(entityNode))
        .map(NodeToProvFactory.setCpmAttributes(entityNode))
        .flatMap(EITHER.makeSure(
            Entity.class::isInstance,
            element -> new InvalidValueException("Element with id '" + element.getId().getLocalPart() + "' is not Entity element!")))
        .map(Entity.class::cast);

  }

  public static Function<AgentNode, Either<ApplicationException, Agent>> agentToProv(AppConfiguration config) {
    return (AgentNode agentNode) -> Either.<ApplicationException, AgentNode> right(agentNode)
        .map(NodeToProvFactory.getIdentifier(config))
        .map(NodeToProvFactory.provFactory::newAgent)
        .flatMap(NodeToProvFactory.setTypeAttribute(agentNode))
        .map(NodeToProvFactory.setCpmAttributes(agentNode))
        .flatMap(EITHER.makeSure(
            Agent.class::isInstance,
            element -> new InvalidValueException("Element with id '" + element.getId().getLocalPart() + "' is not Agent element!")))
        .map(Agent.class::cast);

  }

  public static Function<ActivityNode, Either<ApplicationException, Activity>> activityToProv(AppConfiguration config) {
    return (ActivityNode activityNode) -> Either.<ApplicationException, ActivityNode> right(activityNode)
        .map(NodeToProvFactory.getIdentifier(config))
        .map(NodeToProvFactory.provFactory::newActivity)
        .flatMap(NodeToProvFactory.setTime(activityNode))
        .flatMap(NodeToProvFactory.setTypeAttribute(activityNode))
        .flatMap(EITHER.makeSure(
            Activity.class::isInstance,
            element -> new InvalidValueException("Element with id '" + element.getId().getLocalPart() + "' is not Activity element!")))
        .map(Activity.class::cast);

  }

  private static Function<BaseProvClassNode, QualifiedName> getIdentifier(AppConfiguration config) {
    return node -> NodeToProvFactory.provFactory.newQualifiedName(
        config.getFqdn() + "documents/",
        node.getIdentifier(),
        "storage");
  }

  private static Function<Element, Either<ApplicationException, Element>> setTypeAttribute(BaseProvClassNode node) {
    return element -> {

      return ProvDocumentUtils.getTypeAsQN(node.getProvType())
          .map(elementType -> {
            element.getType().add(provFactory.newType(elementType, provFactory.getName().PROV_QUALIFIED_NAME));
            return element;
          });
    };
  }

  private static Function<Element, Element> setPavAttributes(EntityNode node) {
    return element -> {
      ValueConverter converter = new ValueConverter(NodeToProvFactory.provFactory);

      node.getPav().entrySet().stream()
          .map(entry -> provFactory.newOther(
              provFactory.newQualifiedName("http://purl.org/pav/", entry.getKey(), "pav"),
              entry.getValue(),
              converter.getXsdType(entry.getValue())))
          .forEach(element.getOther()::add);

      return element;
    };
  }

  private static Function<Element, Element> setCpmAttributes(BaseProvClassNode node) {
    return element -> {
      ValueConverter converter = new ValueConverter(NodeToProvFactory.provFactory);

      node.getCpm().entrySet().stream()
          .map(entry -> provFactory.newOther(
              provFactory.newQualifiedName(CpmNamespaceConstants.CPM_NS, entry.getKey(), CpmNamespaceConstants.CPM_PREFIX),
              entry.getValue(),
              converter.getXsdType(entry.getValue())))
          .forEach(element.getOther()::add);

      return element;
    };
  }

  private static Function<Activity, Either<ApplicationException, Activity>> setTime(ActivityNode node) {
    return activity -> {
      return EITHER.combine(
          ProvDocumentUtils.toXMLGregorianCalendar(node.getStartTime()),
          ProvDocumentUtils.toXMLGregorianCalendar(node.getEndTime()),
          (startTime, endTime) -> {
            activity.setStartTime(startTime);
            activity.setEndTime(endTime);
            return activity;
          });
    };
  }

  // ---

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
          "mata");

      Stream<Statement> provNodeStatements = node.getAllNodes().stream()
          .map(NodeToProvFactory.toProvenance(provDocument.getNamespace()));

      Stream<Statement> provRelatioStatements = node.getAllNodes().stream()
          .flatMap(n -> {
            if (n instanceof ActivityNode activityNode) {
              // Stream<Statement> activityStream = Stream.of(NodeToProvFactory.activityToProv(config).apply(activityNode));
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
        .forEach(entry -> element.getOther().add(((Other) guessAttribute(
            NodeToProvFactory.getCpmQN(entry.getKey(), ns),
            entry.getValue()))));
  }

  private static void applyPavAttributesToElement(
      Element element,
      EntityNode node,
      Namespace ns) {

    node.getPav().entrySet().stream()
        .forEach(entry -> element.getOther().add(((Other) guessAttribute(
            NodeToProvFactory.getPavQN(entry.getKey(), ns),
            entry.getValue()))));
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
    else if (partsQN.length == 2)
      return NodeToProvFactory.provFactory.newType(
          value,
          NodeToProvFactory.provFactory.getName().XSD_STRING);
    else
      return ((Type) NodeToProvFactory.guessAttribute(
          NodeToProvFactory.provFactory.newQualifiedName(prefixes.get("prov"), "type", "prov"),
          value));
  }

  private static Attribute guessAttribute(QualifiedName elementName, Object value) {
    ValueConverter converter = new ValueConverter(NodeToProvFactory.provFactory);
    return NodeToProvFactory.provFactory.newAttribute(elementName, value, converter.getXsdType(value));
  }

}
