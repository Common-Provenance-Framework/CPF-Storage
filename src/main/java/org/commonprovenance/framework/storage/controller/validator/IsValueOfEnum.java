package org.commonprovenance.framework.storage.controller.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IsValueOfEnumValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface IsValueOfEnum {
  String message() default "must be any of the enum values";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  Class<? extends Enum<?>> enumClass();
}