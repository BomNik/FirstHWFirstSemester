package com.mipt.nikitabumagin.genericsClasses;

public class Calculator<T extends Number> {

  public double sum(T a, T b) {
    if (a == null || b == null) {
      return Double.NaN;
    }

    return a.doubleValue() + b.doubleValue();
  }

  public double subtract(T a, T b) {
    if (a == null || b == null) {
      return Double.NaN;
    }

    return a.doubleValue() - b.doubleValue();
  }

  public double multiply(T a, T b) {
    if (a == null || b == null) {
      return Double.NaN;
    }

    return a.doubleValue() * b.doubleValue();
  }

  public double divide(T a, T b) {
    if (a == null || b == null || b.doubleValue() == 0.0) {
      return Double.NaN;
    }

    return a.doubleValue() / b.doubleValue();
  }
}
