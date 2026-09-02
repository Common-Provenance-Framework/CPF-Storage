package org.openprovenance.prov.core.json.serialization.serial;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.openprovenance.prov.core.json.serialization.Constants;
import org.openprovenance.prov.vanilla.LangString;
import org.openprovenance.prov.vanilla.ProvFactory;
import org.openprovenance.prov.vanilla.TypedValue;
import org.openprovenance.prov.model.QualifiedName;

import java.io.IOException;

public class CustomTypedValueSerializer extends StdSerializer<TypedValue> implements Constants {

  static final public QualifiedName QUALIFIED_NAME_XSD_STRING = ProvFactory.getFactory().getName().XSD_STRING;
  static final public QualifiedName QUALIFIED_NAME_XSD_BOOLEAN = ProvFactory.getFactory().getName().XSD_BOOLEAN;
  static final public QualifiedName QUALIFIED_NAME_XSD_INTEGER = ProvFactory.getFactory().getName().XSD_INTEGER;
  static final public QualifiedName QUALIFIED_NAME_XSD_DOUBLE = ProvFactory.getFactory().getName().XSD_DOUBLE;
  static final public QualifiedName QUALIFIED_NAME_XSD_FLOAT = ProvFactory.getFactory().getName().XSD_FLOAT;

  protected CustomTypedValueSerializer() {
    super(TypedValue.class);
  }

  protected CustomTypedValueSerializer(Class<TypedValue> t) {
    super(t);
  }

  @Override
  public void serialize(TypedValue attr, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    if ((attr.getValue() instanceof LangString)
        && ((LangString) attr.getValue()).getLang() == null
        && (QUALIFIED_NAME_XSD_STRING.equals(attr.getType())))
      jsonGenerator.writeString(((LangString) attr.getValue()).getValue().toString());
    else if (attr.getValue() instanceof LangString)
      jsonGenerator.writeObject(attr.getValue());

    else if (QUALIFIED_NAME_XSD_STRING.equals(attr.getType()))
      jsonGenerator.writeString((String) attr.getValue());
    else if (QUALIFIED_NAME_XSD_BOOLEAN.equals(attr.getType()))
      jsonGenerator.writeBoolean(Boolean.valueOf(((String) attr.getValue())));
    else if (QUALIFIED_NAME_XSD_INTEGER.equals(attr.getType()))
      jsonGenerator.writeNumber(Integer.valueOf(((String) attr.getValue())));
    // Maybe better to keep as typedLiteral
    // else if (QUALIFIED_NAME_XSD_FLOAT.equals(attr.getType()))
    // jsonGenerator.writeNumber(Float.valueOf(((String) attr.getValue())));
    // else if (QUALIFIED_NAME_XSD_DOUBLE.equals(attr.getType()))
    // jsonGenerator.writeNumber(Double.valueOf(((String) attr.getValue())));
    else {
      jsonGenerator.writeStartObject();
      jsonGenerator.writeStringField(PROPERTY_AT_TYPE, prnt(attr.getType()));
      serializeValue(PROPERTY_AT_VALUE, attr.getValue(), jsonGenerator, serializerProvider);
      jsonGenerator.writeEndObject();
    }
  }

  private void serializeValue(String fieldName, Object value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
    if (value instanceof String)
      jsonGenerator.writeStringField(fieldName, (String) value);
    else if (value instanceof QualifiedName)
      jsonGenerator.writeStringField(fieldName, prnt((QualifiedName) value));
    else
      throw new UnsupportedOperationException("unknown value type " + value);
  }

  private String prnt(QualifiedName qn) {
    return qn.getPrefix() + ":" + qn.getLocalPart();
  }
}
