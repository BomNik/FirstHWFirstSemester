package com.mipt.nikitabumagin.collectionsClasses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CustomArrayListTest {

  private CustomList<String> list;

  @BeforeEach
  void setUp() {
    list = new CustomArrayList<>();
  }

  @Test
  void testAddAndGet() {
    list.add("Hello, ");
    list.add("World");
    list.add("!");

    assertEquals("Hello, ", list.get(0));
    assertEquals("World", list.get(1));
    assertEquals(3, list.size());
  }

  @Test
  void testAddNullThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> list.add(null));
  }

  @Test
  void testGetInvalidIndexThrowsException() {
    list.add("Test");
    assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
    assertThrows(IndexOutOfBoundsException.class, () -> list.get(1));
  }

  @Test
  void testRemove() {
    list.add("A");
    list.add("B");
    list.add("C");

    String removed = list.remove(1);
    assertEquals("B", removed);
    assertEquals(2, list.size());
    assertEquals("A", list.get(0));
    assertEquals("C", list.get(1));
  }

  @Test
  void testRemoveInvalidIndexThrowsException() {
    assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
    list.add("Test");
    assertThrows(IndexOutOfBoundsException.class, () -> list.remove(1));
  }

  @Test
  void testIsEmpty() {
    assertTrue(list.isEmpty());
    list.add("Item");
    assertFalse(list.isEmpty());
  }

  @Test
  void testDynamicExpansion() {
    for (int i = 0; i < 34; ++i) {
      list.add(String.valueOf(i));
    }

    assertEquals(34, list.size());
    for (int i = 0; i < 34; ++i) {
      assertEquals(String.valueOf(i), list.get(i));
    }
  }

  @Test
  void testIterator() {
    list.add("A");
    list.add("B");
    list.add("C");

    Iterator<String> iterator = list.iterator();
    assertTrue(iterator.hasNext());
    assertEquals("A", iterator.next());
    assertEquals("B", iterator.next());
    assertEquals("C", iterator.next());
    assertFalse(iterator.hasNext());
  }

  @Test
  void testIteratorRemove() {
    list.add("A");
    list.add("B");
    list.add("C");

    Iterator<String> iterator = list.iterator();
    iterator.next(); // A
    iterator.next(); // B
    iterator.remove(); // Remove B

    assertEquals(2, list.size());
    assertEquals("A", list.get(0));
    assertEquals("C", list.get(1));
  }

  @Test
  void testIteratorRemoveBeforeNextThrowsException() {
    list.add("A");
    Iterator<String> iterator = list.iterator();

    assertThrows(IllegalStateException.class, () -> iterator.remove());
  }

  /**
   * Tests that iterator next throws exception when no more elements exist
   */
  @Test
  void testIteratorNextThrowsExceptionWhenNoElements() {
    Iterator<String> iterator = list.iterator();
    assertThrows(NoSuchElementException.class, () -> iterator.next());
  }
}
