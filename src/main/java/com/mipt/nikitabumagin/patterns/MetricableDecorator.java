package com.mipt.nikitabumagin.patterns;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;

public class MetricableDecorator implements DataService {

  private final DataService original;
  private final MetricService metricService = new MetricService();

  public MetricableDecorator(DataService original) {
    this.original = original;
  }

  private <T> T measureExecution(Callable<T> action) throws Exception {
    long startTime = System.nanoTime();
    try {
      return action.call();
    } finally {
      long endTime = System.nanoTime();
      Duration duration = Duration.ofNanos(endTime - startTime);
      metricService.sendMetric(duration);
    }
  }

  @Override
  public Optional<String> findDataByKey(String key) {
    try {
      return measureExecution(() -> original.findDataByKey(key));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void saveData(String key, String data) {
    try {
      measureExecution(() -> {
        original.saveData(key, data);
        return null;
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean deleteData(String key) {
    try {
      return measureExecution(() -> original.deleteData(key));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static class MetricService {

    public void sendMetric(Duration duration) {
      System.out.println("Метод выполнялся: " + duration.toString());
    }
  }
}
