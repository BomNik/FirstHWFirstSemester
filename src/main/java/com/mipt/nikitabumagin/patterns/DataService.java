package com.mipt.nikitabumagin.patterns;

import java.util.Optional;

/**
 * Базовый интерфейс сервиса данных
 */
interface DataService {

  Optional<String> findDataByKey(String key);

  void saveData(String key, String data);

  boolean deleteData(String key);
}
