package com.mipt.nikitabumagin.patterns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetricableDecoratorTest {

  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private DataService mockDataService;
  private MetricableDecorator metricableDecorator;

  @BeforeEach
  void setUp() {
    mockDataService = mock(DataService.class);
    metricableDecorator = new MetricableDecorator(mockDataService);
    System.setOut(new PrintStream(outputStream));
  }

  @Test
  void testFindDataByKey_MeasuresExecutionTime() {
    String key = "testKey";
    String data = "testData";
    when(mockDataService.findDataByKey(key)).thenReturn(Optional.of(data));

    Optional<String> result = metricableDecorator.findDataByKey(key);

    String logOutput = outputStream.toString();
    assertTrue(logOutput.contains("Метод выполнялся: PT"));
    assertTrue(result.isPresent());
    assertEquals(data, result.get());
    verify(mockDataService).findDataByKey(key);
  }

  @Test
  void testFindDataByKey_MeasuresTimeForEmptyResult() {
    String key = "nonExistentKey";
    when(mockDataService.findDataByKey(key)).thenReturn(Optional.empty());

    Optional<String> result = metricableDecorator.findDataByKey(key);

    String logOutput = outputStream.toString();
    assertTrue(logOutput.contains("Метод выполнялся: PT"));
    assertTrue(result.isEmpty());
    verify(mockDataService).findDataByKey(key);
  }

  @Test
  void testSaveData_MeasuresExecutionTime() {
    String key = "saveKey";
    String data = "saveData";

    metricableDecorator.saveData(key, data);

    String logOutput = outputStream.toString();
    assertTrue(logOutput.contains("Метод выполнялся: PT"));
    verify(mockDataService).saveData(key, data);
  }

  @Test
  void testDeleteData_MeasuresExecutionTime() {
    String key = "deleteKey";
    when(mockDataService.deleteData(key)).thenReturn(true);

    boolean result = metricableDecorator.deleteData(key);

    String logOutput = outputStream.toString();
    assertTrue(logOutput.contains("Метод выполнялся: PT"));
    assertTrue(result);
    verify(mockDataService).deleteData(key);
  }

  @Test
  void testDeleteData_MeasuresTimeForUnsuccessfulDelete() {
    String key = "nonExistentKey";
    when(mockDataService.deleteData(key)).thenReturn(false);

    boolean result = metricableDecorator.deleteData(key);

    String logOutput = outputStream.toString();
    assertTrue(logOutput.contains("Метод выполнялся: PT"));
    assertFalse(result);
    verify(mockDataService).deleteData(key);
  }

  @Test
  void testAllMethodsMeasureExecutionTime() {
    String key = "testKey";
    String data = "testData";

    when(mockDataService.findDataByKey(key)).thenReturn(Optional.of(data));
    when(mockDataService.deleteData(key)).thenReturn(true);

    metricableDecorator.saveData(key, data);
    metricableDecorator.findDataByKey(key);
    metricableDecorator.deleteData(key);

    String logOutput = outputStream.toString();
    String[] lines = logOutput.split(System.lineSeparator());
    assertEquals(3, lines.length);

    for (String line : lines) {
      assertTrue(line.contains("Метод выполнялся: PT"));
    }

    verify(mockDataService).saveData(key, data);
    verify(mockDataService).findDataByKey(key);
    verify(mockDataService).deleteData(key);
  }

  @Test
  void testMetricsWithNullKey() {
    when(mockDataService.findDataByKey(null)).thenReturn(Optional.empty());

    Optional<String> result = metricableDecorator.findDataByKey(null);

    String logOutput = outputStream.toString();
    assertTrue(logOutput.contains("Метод выполнялся: PT"));
    assertTrue(result.isEmpty());
    verify(mockDataService).findDataByKey(null);
  }

  @Test
  void testMetricsShowDifferentDurations() {
    String fastKey = "fastKey";
    String slowKey = "slowKey";

    when(mockDataService.findDataByKey(fastKey)).thenReturn(Optional.of("fastData"));
    when(mockDataService.findDataByKey(slowKey)).thenAnswer(invocation -> {
      Thread.sleep(10);
      return Optional.of("slowData");
    });

    metricableDecorator.findDataByKey(fastKey);
    String fastLog = outputStream.toString();
    outputStream.reset();

    metricableDecorator.findDataByKey(slowKey);
    String slowLog = outputStream.toString();

    assertTrue(fastLog.contains("Метод выполнялся: PT"));
    assertTrue(slowLog.contains("Метод выполнялся: PT"));

    verify(mockDataService).findDataByKey(fastKey);
    verify(mockDataService).findDataByKey(slowKey);
  }

  @Test
  void testMetricsWithException() {
    String key = "errorKey";
    when(mockDataService.findDataByKey(key)).thenThrow(new RuntimeException("Test error"));

    assertThrows(RuntimeException.class, () -> metricableDecorator.findDataByKey(key));

    String logOutput = outputStream.toString();
    assertTrue(logOutput.contains("Метод выполнялся: PT"));
    verify(mockDataService).findDataByKey(key);
  }
}