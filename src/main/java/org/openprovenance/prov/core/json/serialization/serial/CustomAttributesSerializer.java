package org.openprovenance.prov.core.json.serialization.serial;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.openprovenance.prov.vanilla.TypedValue;

import java.io.IOException;
import java.util.Set;

import static org.openprovenance.prov.core.json.serialization.serial.CustomMapSerializer2.CONTEXT_KEY_FOR_MAP;

public class CustomAttributesSerializer extends StdSerializer<Object> {

  protected CustomAttributesSerializer() {
    super(Object.class);

  }

  protected CustomAttributesSerializer(Class<Object> t) {
    super(t);
  }

  @Override
  public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    String newKey = (String) serializerProvider.getAttribute(CONTEXT_KEY_FOR_MAP);

    Set<TypedValue> set = (Set<TypedValue>) o;
    if (!(set.isEmpty())) {
      jsonGenerator.writeFieldName(newKey);
      // do not serialize attribute into array, if single value in it
      if (set.size() != 1)
        jsonGenerator.writeStartArray();

      for (TypedValue a : set) {
        new CustomTypedValueSerializer().serialize(a, jsonGenerator, serializerProvider);
      }

      if (set.size() != 1)
        jsonGenerator.writeEndArray();
    }
  }

}
