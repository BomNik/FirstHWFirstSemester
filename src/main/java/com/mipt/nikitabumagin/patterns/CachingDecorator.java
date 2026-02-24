package com.mipt.nikitabumagin.patterns;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class CachingDecorator implements DataService {

  private final DataService original;
  private final Map<String, Optional<String>> cache = new HashMap<>();

  public CachingDecorator(DataService dataService) {
    this.original = dataService;
  }

  @Override
  public Optional<String> findDataByKey(String key) {
    if (cache.containsKey(key)) {
      return cache.get(key);
    }

    Optional<String> result = original.findDataByKey(key);
    cache.put(key, result);
    return result;
  }

  @Override
  public void saveData(String key, String data) {
    original.saveData(key, data);
    cache.put(key, Optional.of(data));
  }

  @Override
  public boolean deleteData(String key) {
    boolean isDeleted = original.deleteData(key);
    if (isDeleted) {
      cache.remove(key);
    }
    return isDeleted;
  }
}
