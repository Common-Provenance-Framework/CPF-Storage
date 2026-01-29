package org.commonprovenance.framework.store.controller.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = IsUUIDValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface IsUUID {
  String message() default "must be a valid UUID";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
