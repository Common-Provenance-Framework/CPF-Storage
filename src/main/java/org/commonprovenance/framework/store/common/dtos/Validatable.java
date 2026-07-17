package org.commonprovenance.framework.store.common.dtos;

import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.commonprovenance.framework.store.model.GraphType;
import org.commonprovenance.framework.store.web.trustedParty.dto.form.IssueTokenFormDTO;

public interface Validatable {

  default Vector<String> validate() {
    return Stream.of(this.getClass().getDeclaredFields())
        .map(field -> {
          try {
            field.setAccessible(true);

            if (this instanceof IssueTokenFormDTO itForm)
              if (itForm.graphType() != GraphType.GRAPH)
                if (itForm.signature() == null || itForm.signature().isBlank())
                  return "";

            if (field.get(this) == null)
              return "Field with name '" + field.getName() + "' can not be null!";

            if (field.getType() == String.class && ((String) field.get(this)).isBlank())
              return "Field with name '" + field.getName() + "' can not be blank!";

            return "";
          } catch (Exception e) {
            return "Field '" + field.getName() + "' can not be checked! " + e.getMessage();
          }
        })
        .filter(message -> !message.isBlank())
        .collect(Collectors.toCollection(Vector::new));
  }
}
