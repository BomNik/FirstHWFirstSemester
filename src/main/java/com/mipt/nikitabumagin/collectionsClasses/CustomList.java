package com.mipt.nikitabumagin.collectionsClasses;

/**
 * Interface for custom list
 *
 * @param <A> type of elements in list
 */
public interface CustomList<A> extends Iterable<A> {

  /**
   * Add the element to the end of the list
   *
   * @param element the element to add
   * @throws IllegalArgumentException if element is null
   */
  void add(A element);

  /**
   * Get the element by the index
   *
   * @param index the index of element in the list
   * @return the element at the specified index
   * @throws IndexOutOfBoundsException if the index goes beyond the boundaries of the list
   */
  A get(int index);

  /**
   * Remove the element at the index. The indexes of the elements after this are reduced by one
   *
   * @param index the index of the element to delete
   * @throws IndexOutOfBoundsException if the index goes beyond the boundaries of the list
   */
  A remove(int index);

  /**
   * @return the number of elements in the list
   */
  int size();

  /**
   * Checks if the list is empty
   *
   * @return {@code true} if the list is empty, otherwise {@code false}
   */
  boolean isEmpty();
}
