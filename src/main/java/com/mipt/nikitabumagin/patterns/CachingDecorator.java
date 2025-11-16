package com.mipt.nikitabumagin.patterns;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class CachingDecorator implements DataService {

  private final DataService original;
  private final Map<String, String> cache = new HashMap<>();

  public CachingDecorator(DataService dataService) {
    this.original = dataService;
  }

  @Override
  public Optional<String> findDataByKey(String key) {
    if (cache.containsKey(key)) {
      return Optional.of(cache.get(key));
    }

    Optional<String> result = original.findDataByKey(key);
    result.ifPresent(data -> cache.put(key, data));
    return result;
  }

  @Override
  public void saveData(String key, String data) {
    original.saveData(key, data);
    cache.put(key, data);
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
