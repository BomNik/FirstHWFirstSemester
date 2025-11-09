package com.mipt.nikitabumagin.collectionsClasses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StudentUtils {

  public static void main(String[] args) {
    Map<Integer, Student> hashMap = new HashMap<>();
    Map<Integer, Student> treeMap = new TreeMap<>(Collections.reverseOrder());
  }

  public List<Student> findStudentsByGradeRange(Map<Integer, Student> map, double minGrade, double maxGrade) {
    List<Student> result = new ArrayList<>();

    for (Student s : map.values()) {
      if (s.getGrade() >= minGrade && s.getGrade() <= maxGrade) {
        result.add(s);
      }
    }

    return result;
  }

  public List<Student> getTopNStudents(TreeMap<Integer, Student> map, int n) {
    List<Student> result = new ArrayList<>();
    int count = 0;

    for (Student s : map.values()) {
      if (count >= n) {
        break;
      }

      result.add(s);
      ++count;
    }

    return result;
  }
}
