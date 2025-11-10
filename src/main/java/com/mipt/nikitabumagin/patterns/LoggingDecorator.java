package com.mipt.nikitabumagin.patterns;

import java.util.Optional;

// TODO: Декоратор для логирования
//  Должен:
//      1. При findDataByKey - логировать действие через System.out.println
//      2. При saveData - логировать действие через System.out.println
//      3. При deleteData - логировать действие через System.out.println
public class LoggingDecorator implements DataService {

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
