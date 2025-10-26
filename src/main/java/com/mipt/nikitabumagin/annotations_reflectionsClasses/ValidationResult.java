package com.mipt.nikitabumagin.annotations_reflectionsClasses;

import java.util.List;
import java.util.ArrayList;

public class ValidationResult {
  private boolean isValid;
  private final List<String> errors;

  public ValidationResult() {
    this.isValid = true;
    this.errors = new ArrayList<>();
  }

  public ValidationResult(boolean isValid, List<String> errors) {
    this.isValid = isValid;
    this.errors = errors;
  }

  public void addError(String errorMessage) {
    this.errors.add(errorMessage);
    this.isValid = false;
  }

  public List<String> getErrors() {
    return errors;
  }

  public boolean isValid() {
    return isValid;
  }
}
