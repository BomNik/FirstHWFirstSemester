package com.mipt.nikitabumagin.patterns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidationDecoratorTest {

  private DataService mockDataService;
  private ValidationDecorator validationDecorator;

  @BeforeEach
  void setUp() {
    mockDataService = mock(DataService.class);
    validationDecorator = new ValidationDecorator(mockDataService);
  }

  @Test
  void testFindDataByKey_ValidKey() {
    String key = "validKey";
    String data = "testData";
    when(mockDataService.findDataByKey(key)).thenReturn(Optional.of(data));

    Optional<String> result = validationDecorator.findDataByKey(key);

    assertTrue(result.isPresent());
    assertEquals(data, result.get());
    verify(mockDataService).findDataByKey(key);
  }

  @Test
  void testFindDataByKey_NullKey() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> validationDecorator.findDataByKey(null));

    assertEquals("Ключ не может быть пустым или null.", exception.getMessage());
    verify(mockDataService, never()).findDataByKey(any());
  }

  @Test
  void testFindDataByKey_EmptyKey() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> validationDecorator.findDataByKey(""));

    assertEquals("Ключ не может быть пустым или null.", exception.getMessage());
    verify(mockDataService, never()).findDataByKey(any());
  }

  @Test
  void testFindDataByKey_BlankKey() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> validationDecorator.findDataByKey("   "));

    assertEquals("Ключ не может быть пустым или null.", exception.getMessage());
    verify(mockDataService, never()).findDataByKey(any());
  }

  @Test
  void testSaveData_ValidKeyAndData() {
    String key = "validKey";
    String data = "validData";

    validationDecorator.saveData(key, data);

    verify(mockDataService).saveData(key, data);
  }

  @Test
  void testSaveData_NullKey() {
    String data = "validData";

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> validationDecorator.saveData(null, data));

    assertEquals("Ключ не может быть пустым или null.", exception.getMessage());
    verify(mockDataService, never()).saveData(any(), any());
  }

  @Test
  void testSaveData_NullData() {
    String key = "validKey";

    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      validationDecorator.saveData(key, null);
    });

    assertEquals("Данные не могут быть null.", exception.getMessage());
    verify(mockDataService, never()).saveData(any(), any());
  }

  @Test
  void testSaveData_EmptyKeyAndNullData() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> validationDecorator.saveData("", null));

    assertEquals("Ключ не может быть пустым или null.", exception.getMessage());
    verify(mockDataService, never()).saveData(any(), any());
  }

  @Test
  void testSaveData_EmptyDataString() {
    String key = "validKey";
    String data = "";

    validationDecorator.saveData(key, data);

    verify(mockDataService).saveData(key, data);
  }

  @Test
  void testDeleteData_ValidKey() {
    String key = "validKey";
    when(mockDataService.deleteData(key)).thenReturn(true);

    boolean result = validationDecorator.deleteData(key);

    assertTrue(result);
    verify(mockDataService).deleteData(key);
  }

  @Test
  void testDeleteData_NullKey() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> validationDecorator.deleteData(null));

    assertEquals("Ключ не может быть пустым или null.", exception.getMessage());
    verify(mockDataService, never()).deleteData(any());
  }

  @Test
  void testDeleteData_EmptyKey() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      validationDecorator.deleteData("");
    });

    assertEquals("Ключ не может быть пустым или null.", exception.getMessage());
    verify(mockDataService, never()).deleteData(any());
  }

  @Test
  void testAllMethodsWithValidData() {
    String key = "testKey";
    String data = "testData";

    when(mockDataService.findDataByKey(key)).thenReturn(Optional.of(data));
    when(mockDataService.deleteData(key)).thenReturn(true);

    validationDecorator.saveData(key, data);
    Optional<String> foundData = validationDecorator.findDataByKey(key);
    boolean deleted = validationDecorator.deleteData(key);

    assertTrue(foundData.isPresent());
    assertEquals(data, foundData.get());
    assertTrue(deleted);

    verify(mockDataService).saveData(key, data);
    verify(mockDataService).findDataByKey(key);
    verify(mockDataService).deleteData(key);
  }

  @Test
  void testValidationWithSpecialCharacters() {
    String key = "key-with-special-chars@123";
    String data = "data";

    when(mockDataService.findDataByKey(key)).thenReturn(Optional.of(data));

    Optional<String> result = validationDecorator.findDataByKey(key);

    assertTrue(result.isPresent());
    assertEquals(data, result.get());
    verify(mockDataService).findDataByKey(key);
  }
}