package com.mipt.nikitabumagin;

public class MainClass {

  private int someInt;
  private String someString;

  protected static double someDouble;

  public final long someLong = 1;

  public static void main(String[] args) {
    for (int i = 0; i < 16; i++) {
      System.out.println("Iter: " + i);
    }
  }
}