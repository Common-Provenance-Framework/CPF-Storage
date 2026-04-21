package org.commonprovenance.framework.store.common.utils;

import static org.commonprovenance.framework.store.common.utils.EitherUtils.EITHER;

import java.util.Map;
import java.util.Set;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

import io.vavr.Function1;
import io.vavr.control.Either;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

public interface ProvJsonUtils {
  ProvJsonFunctionalUtils FUNCTIONAL = new ProvJsonFunctionalUtils();
  ProvJsonImperativeUtils IMPERATIVE = new ProvJsonImperativeUtils();

  class ProvJsonFunctionalUtils {
    public Function1<String, Either<ApplicationException, String>> preprocessJsonForDeserialization = this
        .preprocessJsonForDeserialization(true);

    public Function1<String, Either<ApplicationException, String>> preprocessJsonForDeserialization(
        Boolean prettyPrint) {
      return EITHER.<String, ApplicationException, String>liftEither(
          (String value) -> IMPERATIVE.preprocessJsonForDeserialization(value, prettyPrint),
          this::handleThrowable);
    }

    public Function1<String, Either<ApplicationException, String>> preprocessIncompatibleJsonForDeserialization = this
        .preprocessIncompatibleJsonForDeserialization(true);

    public Function1<String, Either<ApplicationException, String>> preprocessIncompatibleJsonForDeserialization(
        Boolean prettyPrint) {
      return EITHER.<String, ApplicationException, String>liftEither(
          (String value) -> IMPERATIVE.preprocessIncompatibleJsonForDeserialization(value, prettyPrint),
          this::handleThrowable);
    }

    public Function1<String, Either<ApplicationException, String>> postprocessJsonAfterSerialization = this
        .postprocessJsonAfterSerialization(true);

    public Function1<String, Either<ApplicationException, String>> postprocessJsonAfterSerialization(
        Boolean prettyPrint) {
      return EITHER.<String, ApplicationException, String>liftEither(
          (String value) -> IMPERATIVE.postprocessJsonAfterSerialization(value, prettyPrint),
          this::handleThrowable);
    }

    private ApplicationException handleThrowable(Throwable throwable) {
      return (throwable instanceof ApplicationException applicationException)
          ? applicationException
          : new InternalApplicationException("Prov Document has not been deserialized: " + throwable.getMessage());
    }
  }

  // --

  class ProvJsonImperativeUtils {
    public String preprocessJsonForDeserialization(String json) throws ApplicationException {
      return this.preprocessJsonForDeserialization(json, true);
    }

    public String preprocessJsonForDeserialization(String json, boolean prettyPrint) throws ApplicationException {
      try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        root = this.addExplicitBundleId(root);

        return prettyPrint
            ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root)
            : mapper.writeValueAsString(root);
      } catch (Throwable throwable) {
        throw new InternalApplicationException("Failed to preprocess JSON for deserialization", throwable);
      }
    }

    public String preprocessIncompatibleJsonForDeserialization(String json) throws ApplicationException {
      return this.preprocessIncompatibleJsonForDeserialization(json, true);
    }

    public String preprocessIncompatibleJsonForDeserialization(String json, boolean prettyPrint)
        throws ApplicationException {
      try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        root = this.addExplicitBundleId(root);
        root = this.putTypedObjectsInArrays(root, mapper);
        root = this.putStringValuesInArray(root, mapper, false);
        root = this.stringifyValues(root, mapper);
        root = this.copyOuterPrefixesIntoBundles(root, mapper);

        return prettyPrint
            ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root)
            : mapper.writeValueAsString(root);
      } catch (Throwable throwable) {
        throw new InternalApplicationException("Failed to preprocess JSON for deserialization", throwable);
      }
    }

    public String postprocessJsonAfterSerialization(String json) throws ApplicationException {
      return this.postprocessJsonAfterSerialization(json, true);
    }

    public String postprocessJsonAfterSerialization(String json, boolean prettyPrint) throws ApplicationException {
      try {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        root = this.removeExplicitBundleId(root);

        return prettyPrint
            ? mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root)
            : mapper.writeValueAsString(root);
      } catch (Throwable throwable) {
        throw new InternalApplicationException("Failed to preprocess JSON after serialization", throwable);
      }

    }

    /**
     * Add explicit "@id" property to bundle to comply with provtoolbox
     * deserialization requirements.
     *
     * @param root the original JSON, possibly without "@id" in bundle
     * @return the modified JSON Node with proper "@id" added to bundle
     */
    JsonNode addExplicitBundleId(JsonNode root) {
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
    JsonNode removeExplicitBundleId(JsonNode root) {
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

    JsonNode stringifyValues(JsonNode node, ObjectMapper mapper) {
      if (node.isObject()) {
        ObjectNode obj = mapper.createObjectNode();
        node.propertyStream()
            .forEach((Map.Entry<String, JsonNode> entry) -> {
              String property = entry.getKey();
              obj.set(property, this.stringifyValues(node.get(property), mapper));
            });
        return obj;
      } else if (node.isArray()) {
        ArrayNode arr = mapper.createArrayNode();
        node.forEach((JsonNode item) -> arr.add(this.stringifyValues(item, mapper)));
        return arr;
      } else {
        return mapper.getNodeFactory().stringNode(node.asString());
      }
    }

    JsonNode copyOuterPrefixesIntoBundles(JsonNode root, ObjectMapper mapper) {
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

    JsonNode putTypedObjectsInArrays(JsonNode node, ObjectMapper mapper) {
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
                this.putTypedObjectsInArrays(entry.getValue(), mapper)));
        return newObj;
      }

      return node;
    }

    JsonNode putStringValuesInArray(JsonNode node, ObjectMapper mapper, boolean insideTarget) {

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
              this.putStringValuesInArray(value, mapper, nowInsideTarget);
            });
      }

      return node;
    }

  }
}