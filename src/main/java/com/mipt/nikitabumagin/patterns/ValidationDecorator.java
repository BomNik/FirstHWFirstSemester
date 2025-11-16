package com.mipt.nikitabumagin.patterns;

import java.util.Optional;

public class ValidationDecorator implements DataService {

  private final DataService original;

  public ValidationDecorator(DataService dataService) {
    this.original = dataService;
  }

  private void validateKey(String key) {
    if (key == null || key.trim().isEmpty()) {
      throw new IllegalArgumentException("Ключ не может быть пустым или null.");
    }
  }

  private void validateData(String data) {
    if (data == null) {
      throw new IllegalArgumentException("Данные не могут быть null.");
    }
  }

  @Override
  public Optional<String> findDataByKey(String key) {
    validateKey(key);
    return original.findDataByKey(key);
  }

  @Override
  public void saveData(String key, String data) {
    validateKey(key);
    validateData(data);
    original.saveData(key, data);
  }

  @Override
  public boolean deleteData(String key) {
    validateKey(key);
    return original.deleteData(key);
  }
}
