package org.commonprovenance.framework.store.support;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.annotation.JsonCreator;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

public record ProvGraph(JsonNode content) {

  private static final JsonMapper MAPPER = JsonMapper.builder().build();
  private static final List<String> NODE_KINDS = List.of("entity", "activity", "agent");

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static ProvGraph fromBase64(String base64) {
    return of(new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8));
  }

  public static ProvGraph of(String json) {
    JsonNode graph = MAPPER.readTree(json);
    if (graph.get("bundle") instanceof ObjectNode bundles)
      bundles.propertyNames().forEach(name -> bundles.set(name, withGeneratedIdentifiersNamedByRole((ObjectNode) bundles.get(name))));
    return new ProvGraph(graph);
  }

  private static ObjectNode withGeneratedIdentifiersNamedByRole(ObjectNode bundle) {
    ObjectNode renamed = (ObjectNode) renamed(bundle, generatedIdentifierRoles(bundle));
    renamed.propertyNames().stream()
        .filter(kind -> !NODE_KINDS.contains(kind))
        .filter(kind -> renamed.get(kind) instanceof ObjectNode)
        .toList()
        .forEach(kind -> renamed.set(kind, keyedByContent((ObjectNode) renamed.get(kind))));
    return renamed;
  }

  private static Map<String, String> generatedIdentifierRoles(ObjectNode bundle) {
    Map<String, String> roles = new HashMap<>();
    ObjectNode entities = objectOrEmpty(bundle, "entity");
    ObjectNode activities = objectOrEmpty(bundle, "activity");
    entities.propertyNames().stream()
        .filter(ProvGraph::isGenerated)
        .forEach(id -> {
          JsonNode entity = entities.get(id);
          if (entity.has("cpm:jwt"))
            roles.put(id, prefixOf(id) + ":token-of-version-" + versionOfBundleBehindToken(bundle, id));
          else if (hasType(entity, "prov:Bundle") && !entity.has("pav:version"))
            roles.put(id, prefixOf(id) + ":general-version");
        });
    activities.propertyNames().stream()
        .filter(ProvGraph::isGenerated)
        .filter(id -> hasType(activities.get(id), "cpm:TokenGeneration"))
        .forEach(id -> roles.put(id, prefixOf(id) + ":token-generation-of-version-"
            + versionOfBundleBehindToken(bundle, relatedValue(bundle, "wasGeneratedBy", "prov:activity", id, "prov:entity"))));
    return roles;
  }

  private static String versionOfBundleBehindToken(ObjectNode bundle, String tokenId) {
    String bundleId = relatedValue(bundle, "wasDerivedFrom", "prov:generatedEntity", tokenId, "prov:usedEntity");
    JsonNode version = objectOrEmpty(bundle, "entity").path(bundleId == null ? "" : bundleId).path("pav:version");
    return version.isMissingNode() ? "unknown" : version.asString();
  }

  private static String relatedValue(ObjectNode bundle, String relation, String fromRole, String fromId, String toRole) {
    return StreamSupport.stream(objectOrEmpty(bundle, relation).values().spliterator(), false)
        .filter(statement -> fromId != null && fromId.equals(statement.path(fromRole).asString(null)))
        .map(statement -> statement.path(toRole).asString(null))
        .findFirst()
        .orElse(null);
  }

  private static JsonNode renamed(JsonNode node, Map<String, String> roles) {
    if (node instanceof ObjectNode object) {
      ObjectNode copy = JsonNodeFactory.instance.objectNode();
      object.propertyNames().forEach(name -> copy.set(roles.getOrDefault(name, name), renamed(object.get(name), roles)));
      return copy;
    }
    if (node instanceof ArrayNode array) {
      ArrayNode copy = JsonNodeFactory.instance.arrayNode();
      array.forEach(element -> copy.add(renamed(element, roles)));
      return copy;
    }
    if (node.isString() && roles.containsKey(node.asString()))
      return StringNode.valueOf(roles.get(node.asString()));
    return node;
  }

  private static ObjectNode keyedByContent(ObjectNode statements) {
    ObjectNode keyed = JsonNodeFactory.instance.objectNode();
    statements.values().forEach(statement -> keyed.set("_:" + sortedText(statement), statement));
    return keyed;
  }

  private static String sortedText(JsonNode node) {
    if (node instanceof ObjectNode object) {
      Map<String, String> sorted = new TreeMap<>();
      object.propertyNames().forEach(name -> sorted.put(name, sortedText(object.get(name))));
      return sorted.toString();
    }
    if (node instanceof ArrayNode array)
      return StreamSupport.stream(array.spliterator(), false).map(ProvGraph::sortedText).collect(Collectors.joining(",", "[", "]"));
    return node.asString();
  }

  private static boolean hasType(JsonNode node, String type) {
    JsonNode types = node.path("prov:type");
    if (types.isArray())
      return StreamSupport.stream(types.spliterator(), false).anyMatch(value -> type.equals(value.path("$").asString(null)));
    return type.equals(types.path("$").asString(null));
  }

  private static boolean isGenerated(String id) {
    String localPart = id.substring(id.indexOf(':') + 1);
    try {
      return UUID.fromString(localPart).toString().equals(localPart);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private static String prefixOf(String id) {
    return id.substring(0, id.indexOf(':'));
  }

  private static ObjectNode objectOrEmpty(ObjectNode parent, String name) {
    return parent.get(name) instanceof ObjectNode object ? object : JsonNodeFactory.instance.objectNode();
  }
}
