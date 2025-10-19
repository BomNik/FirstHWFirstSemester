package com.mipt.nikitabumagin.collectionsClasses;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CollectionPerformanceTester {

  private static final int TOTAL_ELEMENTS = 10_000;

  @Test
  public void testCollectionPerformance() {
    System.out.println("ArrayList and LinkedList Performance Comparison");
    System.out.println("=====================================================");
    System.out.printf("%-21s %-15s %-15s%n", "Operation", "ArrayList (ms)", "LinkedList (ms)");
    System.out.println("-----------------------------------------------------");

    testAddToEnd();
    testAddToBeginning();
    testInsertInMiddle();
    testAccessByIndex();
    testRemoveFromBeginning();
    testRemoveFromEnd();

    System.out.println("=====================================================");
  }

  private void testAddToEnd() {

    long startTime = System.nanoTime();
    List<Integer> arrayList = new ArrayList<>();
    for (int i = 0; i < TOTAL_ELEMENTS; i++) {
      arrayList.add(i);
    }
    long arrayListTime = (System.nanoTime() - startTime) / 1_000_000;


    startTime = System.nanoTime();
    List<Integer> linkedList = new LinkedList<>();
    for (int i = 0; i < TOTAL_ELEMENTS; i++) {
      linkedList.add(i);
    }
    long linkedListTime = (System.nanoTime() - startTime) / 1_000_000;

    System.out.printf("%-21s %-15d %-15d%n", "Add to End", arrayListTime, linkedListTime);
  }

  private void testAddToBeginning() {

    long startTime = System.nanoTime();
    List<Integer> arrayList = new ArrayList<>();
    for (int i = 0; i < TOTAL_ELEMENTS; i++) {
      arrayList.add(0, i);
    }
    long arrayListTime = (System.nanoTime() - startTime) / 1_000_000;


    startTime = System.nanoTime();
    List<Integer> linkedList = new LinkedList<>();
    for (int i = 0; i < TOTAL_ELEMENTS; i++) {
      linkedList.addFirst(i);
    }
    long linkedListTime = (System.nanoTime() - startTime) / 1_000_000;

    System.out.printf("%-21s %-15d %-15d%n", "Add to Beginning", arrayListTime, linkedListTime);
  }

  private void testInsertInMiddle() {

    long startTime = System.nanoTime();
    List<Integer> arrayList = new ArrayList<>();
    for (int i = 0; i < TOTAL_ELEMENTS; i++) {
      int middleIndex = arrayList.size() / 2;
      arrayList.add(middleIndex, i);
    }
    long arrayListTime = (System.nanoTime() - startTime) / 1_000_000;


    startTime = System.nanoTime();
    List<Integer> linkedList = new LinkedList<>();
    for (int i = 0; i < TOTAL_ELEMENTS; i++) {
      int middleIndex = linkedList.size() / 2;
      linkedList.add(middleIndex, i);
    }
    long linkedListTime = (System.nanoTime() - startTime) / 1_000_000;

    System.out.printf("%-21s %-15d %-15d%n", "Insert in Middle", arrayListTime, linkedListTime);
  }

  private void testAccessByIndex() {

    List<Integer> arrayList = new ArrayList<>();
    List<Integer> linkedList = new LinkedList<>();
    for (int i = 0; i < TOTAL_ELEMENTS; i++) {
      arrayList.add(i);
      linkedList.add(i);
    }


    long startTime = System.nanoTime();
    for (int i = 0; i < TOTAL_ELEMENTS; i++) {
      arrayList.get(i);
    }
    long arrayListTime = (System.nanoTime() - startTime) / 1_000_000;


    startTime = System.nanoTime();
    for (int i = 0; i < TOTAL_ELEMENTS; i++) {
      linkedList.get(i);
    }
    long linkedListTime = (System.nanoTime() - startTime) / 1_000_000;

    System.out.printf("%-21s %-15d %-15d%n", "Access by Index", arrayListTime, linkedListTime);
  }

  private void testRemoveFromBeginning() {

    List<Integer> arrayList = new ArrayList<>();
    List<Integer> linkedList = new LinkedList<>();
    for (int i = 0; i < TOTAL_ELEMENTS; i++) {
      arrayList.add(i);
      linkedList.add(i);
    }


    long startTime = System.nanoTime();
    while (!arrayList.isEmpty()) {
      arrayList.removeFirst();
    }
    long arrayListTime = (System.nanoTime() - startTime) / 1_000_000;


    startTime = System.nanoTime();
    while (!linkedList.isEmpty()) {
      linkedList.removeFirst();
    }
    long linkedListTime = (System.nanoTime() - startTime) / 1_000_000;

    System.out.printf("%-21s %-15d %-15d%n", "Remove from Beginning", arrayListTime, linkedListTime);
  }

  private void testRemoveFromEnd() {

    List<Integer> arrayList = new ArrayList<>();
    List<Integer> linkedList = new LinkedList<>();
    for (int i = 0; i < TOTAL_ELEMENTS; i++) {
      arrayList.add(i);
      linkedList.add(i);
    }


    long startTime = System.nanoTime();
    while (!arrayList.isEmpty()) {
      arrayList.removeLast();
    }
    long arrayListTime = (System.nanoTime() - startTime) / 1_000_000;


    startTime = System.nanoTime();
    while (!linkedList.isEmpty()) {
      linkedList.removeLast();
    }
    long linkedListTime = (System.nanoTime() - startTime) / 1_000_000;

    System.out.printf("%-21s %-15d %-15d%n", "Remove from End", arrayListTime, linkedListTime);
  }
}
