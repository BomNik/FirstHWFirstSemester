package com.mipt.nikitabumagin.collectionsClasses;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Custom implementation of a resizable array list
 *
 * @param <A> the type of elements in this list
 */
public class CustomArrayList<A> implements CustomList<A> {

  private static final int DEFAULT_CAPACITY = 8;
  private static final double EXPANSION_COEFFICIENT = 1.5;

  private Object[] array;
  private int capacity;
  private int size;

  /**
   * Constructs an empty list with an initial capacity of {@code DEFAULT_CAPACITY}
   */
  public CustomArrayList() {
    this.array = new Object[DEFAULT_CAPACITY];
    this.capacity = DEFAULT_CAPACITY;
    this.size = 0;
  }

  /**
   * Appends the specified element to the end of this list. The list capacity is automatically
   * increased if necessary
   *
   * @param element element to be appended to this list
   * @throws IllegalArgumentException if the specified element is null
   */
  @Override
  public void add(A element) {
    if (element == null) {
      throw new IllegalArgumentException("Element can't be null");
    }

    if (size == capacity) {
      capacity = (int) (capacity * EXPANSION_COEFFICIENT);
      Object[] newArray = new Object[capacity];
      System.arraycopy(array, 0, newArray, 0, size);
      array = newArray;
    }
    array[size] = element;
    ++size;
  }

  /**
   * Returns the element at the specified position in this list
   *
   * @param index index of the element to return
   * @return the element at the specified index in this list
   * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size())
   */
  @Override
  @SuppressWarnings("unchecked")
  public A get(int index) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException(
          "Index is out of range. Index: " + index + ". Size: " + size);
    }

    return (A) array[index];
  }

  /**
   * Removes the element at the specified position in this list. Shifts any subsequent elements to
   * the left (subtracts one from their indices).
   *
   * @param index the position of the element to be removed
   * @return the element that was removed from the list
   * @throws IndexOutOfBoundsException if the index is out of range (index < 0 || index >= size())
   */
  @Override
  public A remove(int index) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException(
          "Index is out of range. Index: " + index + ". Size: " + size);
    }

    @SuppressWarnings("unchecked") A removedElement = (A) array[index];

    for (int i = index; i < size - 1; ++i) {
      array[i] = array[i + 1];
    }

    array[size - 1] = null;
    --size;

    return removedElement;
  }

  /**
   * @return the number of elements in this list
   */
  @Override
  public int size() {
    return size;
  }

  /**
   * Returns {@code true} if this list contains no elements.
   *
   * @return {@code true} if this list contains no elements
   */
  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  /**
   * Returns an iterator over elements of type {@code A}.
   *
   * @return an Iterator.
   */
  @Override
  public Iterator<A> iterator() {
    return new CustomArrayListIterator();
  }

  private class CustomArrayListIterator implements Iterator<A> {

    private int currentIndex = -1;
    private boolean canRemove = false;

    /**
     * Returns {@code true} if the iteration has more elements. (In other words, returns
     * {@code true} if {@link #next} would return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
      return currentIndex < size - 1;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    @SuppressWarnings("unchecked")
    public A next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      canRemove = true;
      ++currentIndex;
      return (A) array[currentIndex];
    }

    /**
     * Removes from the underlying list the last element returned by this iterator. This method can
     * be called only once per call to {@link #next}
     *
     * @throws IllegalStateException if the {@code next} method has not yet been called, or the
     *                               {@code remove} method has already been called after the last
     *                               call to the {@code next} method
     */
    @Override
    public void remove() {
      if (!canRemove) {
        throw new IllegalStateException();
      }

      CustomArrayList.this.remove(currentIndex);
      --currentIndex;
      canRemove = false;
    }
  }
}
