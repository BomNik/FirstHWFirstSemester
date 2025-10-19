package com.mipt.nikitabumagin.collectionsClasses;

import java.util.Objects;

public class Student {
  private final int id;
  private final String name;
  private final double grade;

  public Student(int id, String name, double grade) {
    this.id = id;

    if (name == null) {
      throw new IllegalArgumentException("Name can't be null");
    }
    this.name = name;

    if (grade < 0) {
      throw new IllegalArgumentException("Grade can't be negative. Grade: " + grade);
    }
    this.grade = grade;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public double getGrade() {
    return grade;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Student s)) {
      return false;
    }

    return id == s.id && Double.compare(grade, s.grade) == 0 && Objects.equals(name, s.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, grade);
  }

}
