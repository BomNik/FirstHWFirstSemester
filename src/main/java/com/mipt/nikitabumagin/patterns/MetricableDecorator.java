package com.mipt.nikitabumagin.patterns;

import java.time.Duration;
import java.util.Optional;

// TODO: Декоратор для метрик
//  Должен:
//      1. При findDataByKey - замерять скорость работы метода и отправлять через MetricService (реализация уже есть внутри класса)
//      2. При saveData - замерять скорость работы метода и отправлять через MetricService (реализация уже есть внутри класса)
//      3. При deleteData - замерять скорость работы метода и отправлять через MetricService (реализация уже есть внутри класса)
public class MetricableDecorator implements DataService {

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

  public static class MetricService {

    public void sendMetric(Duration duration) {
      System.out.println("Метод выполнялся: " + duration.toString());
    }
  }
}
