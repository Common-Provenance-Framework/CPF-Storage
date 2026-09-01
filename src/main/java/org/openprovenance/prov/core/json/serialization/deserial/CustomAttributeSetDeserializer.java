package org.openprovenance.prov.core.json.serialization.deserial;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.openprovenance.prov.model.Attribute;
import org.openprovenance.prov.vanilla.ProvFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class CustomAttributeSetDeserializer extends StdDeserializer<Set<Attribute>> {

  static final ProvFactory pf = new ProvFactory();

  public CustomAttributeSetDeserializer(JavaType vc) {
    super(vc);
  }

  @Override
  public Set<Attribute> deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {

    JsonNode node = jp.getCodec().readTree(jp);

    // QualifiedName context = (QualifiedName) deserializationContext.getAttribute(PROV_ATTRIBUTE_CONTEXT_KEY);

    Set<Attribute> set = new HashSet<>();
    // *
    // fixed issue attributes has to be array
    // *
    if (node.isArray()) {
      for (JsonNode element : node) {
        set.add(new CustomAttributeDeserializerWithRootName().deserialize(element, deserializationContext));
      }
    } else {
      set.add(new CustomAttributeDeserializerWithRootName().deserialize(node, deserializationContext));
    }
    return set;
  }

}
