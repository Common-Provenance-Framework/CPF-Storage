package org.commonprovenance.framework.store.common.utils;

import static org.commonprovenance.framework.store.common.composition.EitherUtils.EITHER;

import java.util.Map;

import org.commonprovenance.framework.store.exceptions.ApplicationException;
import org.commonprovenance.framework.store.exceptions.InternalApplicationException;

import io.vavr.Function1;
import io.vavr.control.Either;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

public interface ProvJsonUtils {
  ProvJsonFunctionalUtils FUNCTIONAL = new ProvJsonFunctionalUtils();
  ProvJsonImperativeUtils IMPERATIVE = new ProvJsonImperativeUtils();

  class ProvJsonFunctionalUtils {
    public Function1<String, Either<ApplicationException, String>> postprocessJsonAfterSerialization = this
        .postprocessJsonAfterSerialization(true);

    public Function1<String, Either<ApplicationException, String>> postprocessJsonAfterSerialization(
        Boolean prettyPrint) {
      return EITHER.<String, String> liftEither(
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
  }
}
