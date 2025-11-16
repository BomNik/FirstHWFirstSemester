package com.mipt.nikitabumagin.patterns;

import java.util.Optional;

public class LoggingDecorator implements DataService {

  private final DataService original;

  public LoggingDecorator(DataService original) {
    this.original = original;
  }

  @Override
  public Optional<String> findDataByKey(String key) {
    System.out.println("Поиск данных по ключу: " + key);
    Optional<String> result = original.findDataByKey(key);
    System.out.println("Результат поиска: " + (result.isPresent() ? "найден" : "не найден"));
    return result;
  }

  @Override
  public void saveData(String key, String data) {
    System.out.println("Сохранение данных с ключом: " + key + " и данными: " + data);
    original.saveData(key, data);
    System.out.println("Данные успешно сохранены.");
  }

  @Override
  public boolean deleteData(String key) {
    System.out.println("Попытка удаления данных по ключу: " + key);
    boolean deleted = original.deleteData(key);
    System.out.println("Удаление завершено. Статус: " + (deleted ? "успешно" : "ключ не найден"));
    return deleted;
  }
}