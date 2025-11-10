package com.mipt.nikitabumagin.patterns;

import java.util.Optional;

// TODO: Декоратор для валидации
//  Должен:
//      1. При findDataByKey - влидируются входные данные
//      2. При saveData - влидируются входные данные
//      3. При deleteData - влидируются входные данные
public class ValidationDecorator implements DataService {

  @Override
  public Optional<String> findDataByKey(String key) {
    return Optional.empty();
  }

  @Override
  public void saveData(String key, String data) {

  }

  @Override
  public boolean deleteData(String key) {
    return false;
  }
}
