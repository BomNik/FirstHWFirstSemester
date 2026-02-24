package com.mipt.nikitabumagin.model;

public class Human {

  private String name;
  private String surname;
  private int age;
  private boolean works;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public boolean isWorks() {
    return works;
  }

  public void setWorks(boolean works) {
    this.works = works;
  }
}