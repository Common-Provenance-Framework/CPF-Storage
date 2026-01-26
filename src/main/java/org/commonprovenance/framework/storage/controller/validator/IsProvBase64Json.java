package org.commonprovenance.framework.storage.controller.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IsProvBase64JsonValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface IsProvBase64Json {
  String message() default "must be a valid Provenance Json";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
