package org.commonprovenance.framework.storage.controller.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValueOfBase64StringValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValueOfBase64String {
  String message() default "must be a valid Base64 encoded string";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  /** Use URL-safe Base64 decoder if true */
  boolean url() default false;
}
