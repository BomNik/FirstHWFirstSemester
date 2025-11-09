package com.mipt.nikitabumagin.annotations_reflectionsClasses;

import com.mipt.nikitabumagin.annotations_reflectionsClasses.annotations.Email;
import com.mipt.nikitabumagin.annotations_reflectionsClasses.annotations.NotNull;
import com.mipt.nikitabumagin.annotations_reflectionsClasses.annotations.Range;
import com.mipt.nikitabumagin.annotations_reflectionsClasses.annotations.Size;
import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class Validator {

  private static final String EMAIL_REGEX = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
  private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

  public static ValidationResult validate(Object object) {
    if (object == null) {
      throw new NullPointerException("Validated object cannot be null");
    }

    ValidationResult result = new ValidationResult();

    for (Field field : object.getClass().getDeclaredFields()) {
      field.setAccessible(true);

      try {
        Object value = field.get(object);

        if (field.isAnnotationPresent(NotNull.class)) {
          validateNotNull(field, value, result);
        }

        if (field.isAnnotationPresent(Size.class)) {
          validateSize(field, value, result);
        }

        if (field.isAnnotationPresent(Range.class)) {
          validateRange(field, value, result);
        }

        if (field.isAnnotationPresent(Email.class)) {
          validateEmail(field, value, result);
        }

      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }

    return result;
  }

  private static void validateNotNull(Field field, Object value, ValidationResult result) {
    NotNull annotation = field.getAnnotation(NotNull.class);

    if (value == null) {
      result.addError(annotation.message());
    }
  }

  private static void validateSize(Field field, Object value, ValidationResult result) {
    Size annotation = field.getAnnotation(Size.class);

    if (value == null) {
      result.addError(annotation.message());
      return;
    }

    if (!(value instanceof String stringValue)) {
      return;
    }

    int length = stringValue.length();
    if (length < annotation.min() || length > annotation.max()) {
      result.addError(annotation.message());
    }
  }

  private static void validateRange(Field field, Object value, ValidationResult result) {
    Range annotation = field.getAnnotation(Range.class);

    if (value == null) {
      result.addError(annotation.message());
      return;
    }

    if (!(value instanceof Number numberValue)) {
      return;
    }

    if (numberValue.longValue() < annotation.min() || numberValue.longValue() > annotation.max()) {
      result.addError(annotation.message());
    }
  }

  private static void validateEmail(Field field, Object value, ValidationResult result) {
    Email annotation = field.getAnnotation(Email.class);

    if (value == null) {
      result.addError(annotation.message());
      return;
    }

    if (!(value instanceof String stringValue)) {
      return;
    }

    if (!EMAIL_PATTERN.matcher(stringValue).matches()) {
      result.addError(annotation.message());
    }
  }
}
