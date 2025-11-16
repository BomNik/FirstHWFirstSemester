package com.mipt.nikitabumagin.patterns;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoggingDecoratorTest {

  private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private DataService mockDataService;
  private LoggingDecorator loggingDecorator;

  @BeforeEach
  void setUp() {
    mockDataService = mock(DataService.class);
    loggingDecorator = new LoggingDecorator(mockDataService);
    System.setOut(new PrintStream(outputStream));
  }

  @Test
  void testFindDataByKey_LogsSearchAndResult() {
    String key = "testKey";
    String data = "testData";
    when(mockDataService.findDataByKey(key)).thenReturn(Optional.of(data));

    Optional<String> result = loggingDecorator.findDataByKey(key);

    String logOutput = outputStream.toString();
    assertTrue(logOutput.contains("Поиск данных по ключу: " + key));
    assertTrue(logOutput.contains("Результат поиска: найден"));
    assertTrue(result.isPresent());
    assertEquals(data, result.get());
    verify(mockDataService).findDataByKey(key);
  }

  @Test
  void testFindDataByKey_LogsNotFound() {
    String key = "nonExistentKey";
    when(mockDataService.findDataByKey(key)).thenReturn(Optional.empty());

    Optional<String> result = loggingDecorator.findDataByKey(key);

    String logOutput = outputStream.toString();
    assertTrue(logOutput.contains("Поиск данных по ключу: " + key));
    assertTrue(logOutput.contains("Результат поиска: не найден"));
    assertTrue(result.isEmpty());
    verify(mockDataService).findDataByKey(key);
  }

  @Test
  void testSaveData_LogsSaveAndSuccess() {
    String key = "saveKey";
    String data = "saveData";

    loggingDecorator.saveData(key, data);

    String logOutput = outputStream.toString();
    assertTrue(logOutput.contains("Сохранение данных с ключом: " + key + " и данными: " + data));
    assertTrue(logOutput.contains("Данные успешно сохранены."));
    verify(mockDataService).saveData(key, data);
  }

  @Test
  void testSaveData_LogsSaveWithEmptyData() {
    String key = "emptyKey";
    String data = "";

    loggingDecorator.saveData(key, data);

    String logOutput = outputStream.toString();
    assertTrue(logOutput.contains("Сохранение данных с ключом: " + key + " и данными: " + data));
    assertTrue(logOutput.contains("Данные успешно сохранены."));
    verify(mockDataService).saveData(key, data);
  }

  @Test
  void testDeleteData_LogsSuccessfulDelete() {
    String key = "deleteKey";
    when(mockDataService.deleteData(key)).thenReturn(true);

    boolean result = loggingDecorator.deleteData(key);

    String logOutput = outputStream.toString();
    assertTrue(logOutput.contains("Попытка удаления данных по ключу: " + key));
    assertTrue(logOutput.contains("Удаление завершено. Статус: успешно"));
    assertTrue(result);
    verify(mockDataService).deleteData(key);
  }

  @Test
  void testDeleteData_LogsUnsuccessfulDelete() {
    String key = "nonExistentKey";
    when(mockDataService.deleteData(key)).thenReturn(false);

    boolean result = loggingDecorator.deleteData(key);

    String logOutput = outputStream.toString();
    assertTrue(logOutput.contains("Попытка удаления данных по ключу: " + key));
    assertTrue(logOutput.contains("Удаление завершено. Статус: ключ не найден"));
    assertFalse(result);
    verify(mockDataService).deleteData(key);
  }

  @Test
  void testLoggingWithNullKey() {
    when(mockDataService.findDataByKey(null)).thenReturn(Optional.empty());

    Optional<String> result = loggingDecorator.findDataByKey(null);

    String logOutput = outputStream.toString();
    assertTrue(logOutput.contains("Поиск данных по ключу: null"));
    assertTrue(logOutput.contains("Результат поиска: не найден"));
    assertTrue(result.isEmpty());
    verify(mockDataService).findDataByKey(null);
  }

  @Test
  void testAllMethodsLogCallsInRussian() {
    String key = "testKey";
    String data = "testData";

    when(mockDataService.findDataByKey(key)).thenReturn(Optional.of(data));
    when(mockDataService.deleteData(key)).thenReturn(true);

    loggingDecorator.saveData(key, data);
    loggingDecorator.findDataByKey(key);
    loggingDecorator.deleteData(key);

    String logOutput = outputStream.toString();
    assertTrue(logOutput.contains("Сохранение данных"));
    assertTrue(logOutput.contains("Данные успешно сохранены."));
    assertTrue(logOutput.contains("Поиск данных по ключу"));
    assertTrue(logOutput.contains("Результат поиска: найден"));
    assertTrue(logOutput.contains("Попытка удаления данных"));
    assertTrue(logOutput.contains("Удаление завершено. Статус: успешно"));

    verify(mockDataService).saveData(key, data);
    verify(mockDataService).findDataByKey(key);
    verify(mockDataService).deleteData(key);
  }

  @Test
  void testLoggingWithLongData() {
    String key = "longKey";
    String longData = "очень длинные данные которые должны быть залогированы полностью";

    loggingDecorator.saveData(key, longData);

    String logOutput = outputStream.toString();
    assertTrue(
        logOutput.contains("Сохранение данных с ключом: " + key + " и данными: " + longData));
    assertTrue(logOutput.contains("Данные успешно сохранены."));
    verify(mockDataService).saveData(key, longData);
  }
}
