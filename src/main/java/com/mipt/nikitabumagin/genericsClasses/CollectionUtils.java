package com.mipt.nikitabumagin.genericsClasses;

import java.util.ArrayList;
import java.util.List;

public class CollectionUtils {

  public static <T> void addAll(List<? super T> destination, List<? extends T> source) {
    if (destination == null || source == null) {
      return;
    }

    for (T element : source) {
      destination.add(element);
    }
  }

  public static <T> List<T> mergeLists(List<? extends T> list1, List<? extends T> list2) {
    final List<T> merged = new ArrayList<>();

    if (list1 != null) {
      CollectionUtils.addAll(merged, list1);
    }

    if (list2 != null) {
      CollectionUtils.addAll(merged, list2);
    }

    return merged;
  }
}
