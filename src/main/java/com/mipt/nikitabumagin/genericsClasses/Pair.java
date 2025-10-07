package com.mipt.nikitabumagin.genericsClasses;

public class Pair<K, V> {

  K key;
  V value;

  public Pair(K key, V value) {
    this.key = key;
    this.value = value;
  }

  public K getKey() {
    return key;
  }

  public void setKey(K key) {
    this.key = key;
  }

  public V getValue() {
    return value;
  }

  public void setValue(V value) {
    this.value = value;
  }

  public Pair<V, K> swap() {
    return new Pair<>(value, key);
  }

  @Override
  public String toString() {
    return "Pair{key=" +
        (key == null ? "null" : key.toString()) +
        ", value=" +
        (value == null ? "null" : value.toString()) +
        "}";
  }
}
