package org.commonprovenance.framework.store.common.validation;

import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class DTOValidator {

  public Vector<String> validate() {
    return Stream.of(this.getClass().getDeclaredFields())
        .map(field -> {
          try {
            field.setAccessible(true);

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
