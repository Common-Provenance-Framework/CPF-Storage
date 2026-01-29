package org.commonprovenance.framework.store.controller.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IsJsonBase64Validator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface IsJsonBase64 {
  String message() default "must be a valid Json Base64 String";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
