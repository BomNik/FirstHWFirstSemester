package com.mipt.nikitabumagin.patterns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CachingDecoratorTest {

  private DataService mockDataService;
  private CachingDecorator cachingDecorator;

  @BeforeEach
  void setUp() {
    mockDataService = mock(DataService.class);
    cachingDecorator = new CachingDecorator(mockDataService);
  }

  @Test
  @DisplayName("Данные должны кешироваться при первом вызове findDataByKey")
  void testFindDataByKey_CachesResultOnFirstCall() {
    String testKey = "testKey";
    String testData = "testData";

    when(mockDataService.findDataByKey(testKey))
        .thenReturn(Optional.of(testData))
        .thenReturn(Optional.of("differentData"));

    Optional<String> firstResult = cachingDecorator.findDataByKey(testKey);
    Optional<String> secondResult = cachingDecorator.findDataByKey(testKey);

    assertTrue(firstResult.isPresent(), "Первый результат должен существовать");
    assertEquals(testData, firstResult.get(), "Первый результат должен быть 'testData'");

    assertTrue(secondResult.isPresent(), "Второй результат должен существовать");
    assertEquals(testData, secondResult.get(),
        "Второй результат должен быть из кэша (не 'differentData')");

    verify(mockDataService, times(1)).findDataByKey(testKey);
  }

  @Test
  @DisplayName("Пустой результат также должен кешироваться")
  void testFindDataByKey_CachesEmptyResult() {
    String nonExistentKey = "nonExistentKey";

    when(mockDataService.findDataByKey(nonExistentKey))
        .thenReturn(Optional.empty())
        .thenReturn(Optional.of("thisShouldNotBeReturned"));

    Optional<String> firstResult = cachingDecorator.findDataByKey(nonExistentKey);
    Optional<String> secondResult = cachingDecorator.findDataByKey(nonExistentKey);

    assertTrue(firstResult.isEmpty(), "Первый результат должен быть пустым");
    assertTrue(secondResult.isEmpty(), "Второй результат должен быть пустым (из кэша)");

    verify(mockDataService, times(1)).findDataByKey(nonExistentKey);
  }

  @Test
  @DisplayName("saveData должен обновлять данные в кэше")
  void testSaveData_UpdatesCache() {
    String key = "key";
    String initialData = "initialData";
    String updatedData = "updatedData";

    when(mockDataService.findDataByKey(key))
        .thenReturn(Optional.of(initialData))
        .thenReturn(Optional.of(updatedData));

    Optional<String> firstRead = cachingDecorator.findDataByKey(key);
    cachingDecorator.saveData(key, updatedData);
    Optional<String> secondRead = cachingDecorator.findDataByKey(key);

    assertEquals(initialData, firstRead.get(), "Первое чтение должно вернуть initialData");
    assertEquals(updatedData, secondRead.get(), "Второе чтение должно вернуть updatedData из кэша");

    verify(mockDataService).saveData(key, updatedData);
    verify(mockDataService, times(1)).findDataByKey(key);
  }

  @Test
  @DisplayName("deleteData должен удалять данные из кэша")
  void testDeleteData_InvalidatesCache() {
    String key = "keyToDelete";
    String data = "dataToDelete";

    when(mockDataService.findDataByKey(key))
        .thenReturn(Optional.of(data))
        .thenReturn(Optional.empty());
    when(mockDataService.deleteData(key)).thenReturn(true);
    Optional<String> beforeDelete = cachingDecorator.findDataByKey(key);
    boolean deleteResult = cachingDecorator.deleteData(key);
    Optional<String> afterDelete = cachingDecorator.findDataByKey(key);

    assertTrue(beforeDelete.isPresent(), "Данные должны существовать до удаления");
    assertEquals(data, beforeDelete.get());

    assertTrue(deleteResult, "deleteData должен вернуть true");
    assertTrue(afterDelete.isEmpty(), "Данные должны отсутствовать после удаления");

    verify(mockDataService, times(2)).findDataByKey(key);
    verify(mockDataService).deleteData(key);
  }

  @Test
  void testDeleteData_UnsuccessfulDelete_DoesNotInvalidateCache() {
    String key = "key";
    String data = "data";

    when(mockDataService.findDataByKey(key))
        .thenReturn(Optional.of(data));

    when(mockDataService.deleteData(key)).thenReturn(false); // удаление не удалось

    Optional<String> firstRead = cachingDecorator.findDataByKey(key);
    boolean deleteResult = cachingDecorator.deleteData(key);
    Optional<String> secondRead = cachingDecorator.findDataByKey(key);

    assertFalse(deleteResult, "deleteData должен вернуть false");
    assertTrue(secondRead.isPresent(), "Данные должны остаться в кэше");
    assertEquals(data, secondRead.get());

    verify(mockDataService, times(1)).findDataByKey(key);
  }

  @Test
  void testCacheIsolationBetweenDifferentKeys() {
    String key1 = "key1";
    String data1 = "data1";
    String key2 = "key2";
    String data2 = "data2";

    when(mockDataService.findDataByKey(key1)).thenReturn(Optional.of(data1));
    when(mockDataService.findDataByKey(key2)).thenReturn(Optional.of(data2));

    Optional<String> result1 = cachingDecorator.findDataByKey(key1);
    Optional<String> result2 = cachingDecorator.findDataByKey(key2);
    Optional<String> cachedResult1 = cachingDecorator.findDataByKey(key1);
    Optional<String> cachedResult2 = cachingDecorator.findDataByKey(key2);

    assertEquals(data1, result1.get());
    assertEquals(data2, result2.get());
    assertEquals(data1, cachedResult1.get());
    assertEquals(data2, cachedResult2.get());

    verify(mockDataService, times(1)).findDataByKey(key1);
    verify(mockDataService, times(1)).findDataByKey(key2);
  }

  @Test
  void testCacheBehaviorWithNullAndEmptyValues() {
    String key1 = "nullKey";
    String key2 = "emptyKey";

    when(mockDataService.findDataByKey(key1))
        .thenReturn(Optional.of("null"));
    when(mockDataService.findDataByKey(key2))
        .thenReturn(Optional.of(""));

    Optional<String> result1 = cachingDecorator.findDataByKey(key1);
    Optional<String> result2 = cachingDecorator.findDataByKey(key2);

    assertTrue(result1.isPresent());
    assertEquals("null", result1.get());

    assertTrue(result2.isPresent());
    assertEquals("", result2.get());

    Optional<String> cached1 = cachingDecorator.findDataByKey(key1);
    Optional<String> cached2 = cachingDecorator.findDataByKey(key2);

    assertEquals("null", cached1.get());
    assertEquals("", cached2.get());

    verify(mockDataService, times(1)).findDataByKey(key1);
    verify(mockDataService, times(1)).findDataByKey(key2);
  }

  @Test
  void testDecoratorTransparentlyPassesResults() {
    String key = "testKey";
    String expectedData = "expectedData";

    when(mockDataService.findDataByKey(key))
        .thenReturn(Optional.of(expectedData));
    when(mockDataService.deleteData(key))
        .thenReturn(true);

    Optional<String> result = cachingDecorator.findDataByKey(key);
    assertTrue(result.isPresent());
    assertEquals(expectedData, result.get());

    boolean deleteResult = cachingDecorator.deleteData(key);
    assertTrue(deleteResult);

    cachingDecorator.saveData(key, expectedData);
    verify(mockDataService).saveData(key, expectedData);
  }
}
