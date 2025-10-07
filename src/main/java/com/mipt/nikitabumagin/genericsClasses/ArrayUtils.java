package com.mipt.nikitabumagin.genericsClasses;

public class ArrayUtils {

  public static <T> int findFirst(T[] array, T element) {
    for (int i = 0; i < array.length; ++i) {
      if (array[i].equals(element)) {
        return i;
      }
    }
    return -1;
  }
}
