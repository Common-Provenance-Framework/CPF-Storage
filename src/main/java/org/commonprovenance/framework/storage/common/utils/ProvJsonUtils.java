package org.commonprovenance.framework.storage.common.utils;

import java.util.Map;
import java.util.Set;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

public class ProvJsonUtils {

  public static String preprocessJsonForDeserialization(String json) {
    return preprocessJsonForDeserialization(json, true);
  }

  public static String preprocessJsonForDeserialization(String json, boolean prettyPring) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(json);

      root = addExplicitBundleId(root);
      root = putTypedObjectsInArrays(root, mapper);
      root = putStringValuesInArray(root, mapper, false);
      root = stringifyValues(root, mapper);
      root = copyOuterPrefixesIntoBundles(root, mapper);

      return prettyPring
          ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root)
          : mapper.writeValueAsString(root);
    } catch (JacksonException e) {
      throw new RuntimeException("Failed to preprocess JSON for deserialization", e);
    }
  }

  public static String postprocessJsonAfterSerialization(String json) {
    return ProvJsonUtils.postprocessJsonAfterSerialization(json, true);
  }

  public static String postprocessJsonAfterSerialization(String json, boolean prettyPring) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(json);

      root = removeExplicitBundleId(root);

      return prettyPring
          ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root)
          : mapper.writeValueAsString(root);
    } catch (

    JacksonException e) {
      throw new RuntimeException("Failed to preprocess JSON after serialization", e);
    }

  }

  /**
   * Add explicit "@id" property to bundle to comply with provtoolbox
   * deserialization requirements.
   *
   * @param root the original JSON, possibly without "@id" in bundle
   * @return the modified JSON Node with proper "@id" added to bundle
   */
  public static JsonNode addExplicitBundleId(JsonNode root) {
    JsonNode bundleNode = root.path("bundle");
    if (bundleNode.isObject()) {
      bundleNode.propertyStream()
          .forEach((Map.Entry<String, JsonNode> entry) -> {
            String bundleId = entry.getKey();
            JsonNode bundle = entry.getValue();
            if (bundle.isObject() && !bundle.hasNonNull(bundleId)) {
              ((ObjectNode) bundle).put("@id", bundleId);
            }
          });
    }
    return root;
  }

  /**
   * Removes "@id" property in bundle.
   *
   * @param json the original JSON, possibly with "@id" in bundle
   * @return the modified JSON string without "@id"
   */
  public static JsonNode removeExplicitBundleId(JsonNode root) {
    JsonNode bundleNode = root.path("bundle");
    if (bundleNode.isObject()) {
      bundleNode.propertyStream()
          .forEach((Map.Entry<String, JsonNode> entry) -> {
            JsonNode bundle = entry.getValue();
            if (bundle.isObject() && bundle.has("@id")) {
              ((ObjectNode) bundle).remove("@id");
            }
          });
    }

    return root;
  }

  private static JsonNode stringifyValues(JsonNode node, ObjectMapper mapper) {
    if (node.isObject()) {
      ObjectNode obj = mapper.createObjectNode();
      node.propertyStream()
          .forEach((Map.Entry<String, JsonNode> entry) -> {
            String property = entry.getKey();
            obj.set(property, stringifyValues(node.get(property), mapper));
          });
      return obj;
    } else if (node.isArray()) {
      ArrayNode arr = mapper.createArrayNode();
      node.forEach((JsonNode item) -> arr.add(stringifyValues(item, mapper)));
      return arr;
    } else {
      return mapper.getNodeFactory().stringNode(node.asString());
    }
  }

  public static JsonNode copyOuterPrefixesIntoBundles(JsonNode root, ObjectMapper mapper) {
    JsonNode outerPrefix = root.path("prefix");
    JsonNode bundleNode = root.path("bundle");
    if (outerPrefix.isObject() && bundleNode.isObject()) {
      bundleNode.propertyStream()
          .forEach((Map.Entry<String, JsonNode> bundleEntry) -> {
            JsonNode bundle = bundleEntry.getValue();

            ObjectNode bundlePrefix = bundle.isObject()
                && bundle.has("prefix")
                && bundle.get("prefix").isObject()
                    ? (ObjectNode) bundle.get("prefix")
                    : mapper.createObjectNode();

            outerPrefix
                .propertyStream()
                .forEach((Map.Entry<String, JsonNode> prefixEntry) -> bundlePrefix.set(
                    prefixEntry.getKey(),
                    prefixEntry.getValue()));

            ((ObjectNode) bundle).set("prefix", bundlePrefix);
          });
    }

    return root;
  }

  private static JsonNode putTypedObjectsInArrays(JsonNode node, ObjectMapper mapper) {
    if (node.isObject()) {
      ObjectNode obj = (ObjectNode) node;

      boolean hasDollar = obj.has("$");
      boolean hasType = obj.has("type");

      // If object matches {"$", "type"} → wrap in array
      if (hasDollar && hasType) {
        ArrayNode arr = mapper.createArrayNode();
        arr.add(obj);
        return arr;
      }

      // Otherwise recurse through fields
      ObjectNode newObj = mapper.createObjectNode();
      obj
          .propertyStream()
          .forEach((Map.Entry<String, JsonNode> entry) -> newObj.set(
              entry.getKey(),
              putTypedObjectsInArrays(entry.getValue(), mapper)));
      return newObj;
    }

    return node;
  }

  private static JsonNode putStringValuesInArray(JsonNode node, ObjectMapper mapper, boolean insideTarget) {

    if (node.isObject()) {
      ObjectNode obj = (ObjectNode) node;

      if (obj.has("$")) {
        return node;
      }

      obj.propertyStream()
          .forEach((Map.Entry<String, JsonNode> entry) -> {
            String property = entry.getKey();
            JsonNode value = entry.getValue();

            boolean nowInsideTarget = insideTarget
                || Set.of("entity", "activity", "agent").contains(property);

            if (nowInsideTarget) {
              if (value.isString()
                  && !property.equals("prov:startTime")
                  && !property.equals("prov:endTime")) {
                ArrayNode arr = mapper.getNodeFactory().arrayNode();
                arr.add(value);
                obj.set(property, arr);
              }
            }
            putStringValuesInArray(value, mapper, nowInsideTarget);
          });
    }

    return node;
  }
}