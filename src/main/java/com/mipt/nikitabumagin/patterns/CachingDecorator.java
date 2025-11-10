package com.mipt.nikitabumagin.patterns;

import java.util.Optional;

// TODO: Декоратор для кеширования
//  Должен:
//      1. При findDataByKey - кешировать результаты
//      2. При saveData - обновлять данные в кэше
//      3. При deleteData - инвалидировать кэш
class CachingDecorator implements DataService {

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
