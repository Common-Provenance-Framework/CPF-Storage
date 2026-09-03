package org.openprovenance.prov.core.json;

import java.util.List;

import org.openprovenance.prov.core.json.serialization.deserial.CustomQualifiedNameDeserializer;
import org.openprovenance.prov.model.QualifiedName;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonPropertyOrder({ "prov:collection", "prov:entity" })

public interface JSON_HadMember extends HasKind {

  @JsonProperty("prov:collection")
  @JsonDeserialize(using = CustomQualifiedNameDeserializer.class)
  public QualifiedName getCollection();

  @JsonProperty("prov:entity")
  @JsonDeserialize(contentUsing = CustomQualifiedNameDeserializer.class)
  public List<QualifiedName> getEntity();
}
