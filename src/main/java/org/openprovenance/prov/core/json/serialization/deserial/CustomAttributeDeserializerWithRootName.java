package org.openprovenance.prov.core.json.serialization.deserial;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.openprovenance.prov.core.json.serialization.Constants;
import org.openprovenance.prov.core.json.serialization.serial.CustomTypedValueSerializer;
import org.openprovenance.prov.vanilla.LangString;
import org.openprovenance.prov.vanilla.ProvFactory;
import org.openprovenance.prov.model.Attribute;
import org.openprovenance.prov.model.Namespace;
import org.openprovenance.prov.model.QualifiedName;

import java.io.IOException;

import static org.openprovenance.prov.core.json.serialization.deserial.CustomThreadConfig.JSON_CONTEXT_KEY_NAMESPACE;
import static org.openprovenance.prov.core.json.serialization.deserial.CustomThreadConfig.getAttributes;
import static org.openprovenance.prov.core.json.serialization.deserial.CustomKeyDeserializer.PROV_ATTRIBUTE_CONTEXT_KEY;

public class CustomAttributeDeserializerWithRootName extends StdDeserializer<Attribute> implements Constants {

  static final ProvFactory pf = new ProvFactory();

  public CustomAttributeDeserializerWithRootName() {
    this(Attribute.class);
  }

  public CustomAttributeDeserializerWithRootName(Class<?> vc) {
    super(vc);
  }

  @Override
  public Attribute deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
    JsonNode node = jp.getCodec().readTree(jp);

    return deserialize(node, deserializationContext);

  }

  public Attribute deserialize(JsonNode node, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
    QualifiedName elementName = (QualifiedName) deserializationContext.getAttribute(PROV_ATTRIBUTE_CONTEXT_KEY);

    if (node.isTextual()) {
      // https://www.w3.org/submissions/prov-json/
      // 2.2 JSON Data Typing
      // xsd:string values MAY be represented using the JSON native data type string
      return deserialize(elementName, node.textValue());
    } else if (node.isBoolean()) {
      // https://www.w3.org/submissions/prov-json/
      // 2.2 JSON Data Typing
      // xsd:boolean values MAY be represented using the JSON native data type boolean
      return deserialize(elementName, node.booleanValue());
    } else if (node.isInt()) {
      // https://www.w3.org/submissions/prov-json/
      // 2.2 JSON Data Typing
      // xsd:decimal values MAY be represented using the JSON native data type number
      return deserialize(elementName, node.intValue());
    } else if (node.isDouble()) {
      // https://www.w3.org/submissions/prov-json/
      // 2.2 JSON Data Typing
      // xsd:decimal values MAY be represented using the JSON native data type number
      return deserialize(elementName, node.doubleValue());
    } else if (node.isFloat()) {
      // https://www.w3.org/submissions/prov-json/
      // 2.2 JSON Data Typing
      // xsd:decimal values MAY be represented using the JSON native data type number
      return deserialize(elementName, node.floatValue());
    } else if (node.isObject()) {
      // deserialize attribute if Object as specific type
      return deserialize(elementName, node);
    } else {
      // if array or something else throw an error
      throw new IOException("Unknown attribute format.");
    }
  }

  public Attribute deserialize(QualifiedName elementName, String astring) throws IOException, JsonProcessingException {
    return pf.newAttribute(elementName, astring, CustomTypedValueSerializer.QUALIFIED_NAME_XSD_STRING);
  }

  public Attribute deserialize(QualifiedName elementName, boolean aboolean) throws IOException, JsonProcessingException {
    return pf.newAttribute(elementName, String.valueOf(aboolean), CustomTypedValueSerializer.QUALIFIED_NAME_XSD_BOOLEAN);
  }

  public Attribute deserialize(QualifiedName elementName, int aint) throws IOException, JsonProcessingException {
    return pf.newAttribute(elementName, String.valueOf(aint), CustomTypedValueSerializer.QUALIFIED_NAME_XSD_INTEGER);
  }

  public Attribute deserialize(QualifiedName elementName, double adouble) throws IOException, JsonProcessingException {
    return pf.newAttribute(elementName, String.valueOf(adouble), CustomTypedValueSerializer.QUALIFIED_NAME_XSD_DOUBLE);
  }

  public Attribute deserialize(QualifiedName elementName, float afloat) throws IOException, JsonProcessingException {
    return pf.newAttribute(elementName, String.valueOf(afloat), CustomTypedValueSerializer.QUALIFIED_NAME_XSD_FLOAT);
  }

  public Attribute deserialize(QualifiedName elementName, JsonNode vObj) throws IOException, JsonProcessingException {
    final Namespace ns = getAttributes().get().get(JSON_CONTEXT_KEY_NAMESPACE);

    JsonNode typeRaw = vObj.get(PROPERTY_AT_TYPE);
    String type = (typeRaw == null) ? null : typeRaw.textValue();

    JsonNode langRaw = vObj.get(Constants.PROPERTY_STRING_LANG);
    String lang = (langRaw == null) ? null : langRaw.textValue();

    JsonNode value = vObj.get(PROPERTY_AT_VALUE);
    /*
     *
     * System.out.println("-Found key1 " + key1); System.out.println("-Found key2 " + key2); System.out.println("-Found @value " + value); System.out.println("-Found @type " +
     * type);
     *
     */
    Object valueObject; // TODO: should not be checking qname but uri
    if (type == null) {
      if (lang != null) {
        valueObject = new LangString(value.textValue(), lang);
        type = "prov:InternationalizedString";
      } else {
        throw new IOException("Unknown attribute type or lang.");
      }
    } else if (type.equals("prov:QUALIFIED_NAME")) {
      valueObject = ns.stringToQualifiedName(value.textValue(), pf);
      // } else if (type.equals("xsd:QName")) { // ???
    } else if (type.equals("prov:InternationalizedString")) {
      valueObject = new LangString(value.textValue(), lang);
    } else {
      valueObject = value.textValue();
    }

    QualifiedName typeQN = ns.stringToQualifiedName(type, pf);
    return pf.newAttribute(elementName, valueObject, typeQN);
  }
}
