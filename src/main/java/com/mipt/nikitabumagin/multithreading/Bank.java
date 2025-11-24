package com.mipt.nikitabumagin.multithreading;

import java.util.concurrent.TimeUnit;

public class Bank {

  public static void sendToAccountDeadlock(BankAccount from, BankAccount to, long amount) {
    synchronized (from) {

      try {
        TimeUnit.MILLISECONDS.sleep(1);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }

      synchronized (to) {
        validateTransaction(from, to, amount);
        from.withdraw(amount);
        to.deposit(amount);
      }
    }
  }

  public static void sendToAccount(BankAccount from, BankAccount to, long amount) {
    validateTransaction(from, to, amount);
    BankAccount first = from.getId() < to.getId() ? from : to;
    BankAccount second = from.getId() > to.getId() ? from : to;
    synchronized (first) {
      synchronized (second) {
        from.withdraw(amount);
        to.deposit(amount);
      }
    }
  }

  private static void validateTransaction(BankAccount from, BankAccount to, long amount) {
    if (from == null) {
      throw new IllegalArgumentException("from is null");
    }
    if (to == null) {
      throw new IllegalArgumentException("to is null");
    }
    if (from.equals(to)) {
      throw new IllegalArgumentException("from and to cannot be the same");
    }
    if (amount < 0) {
      throw new IllegalArgumentException("amount is negative");
    }
    if (from.getBalance() < amount) {
      throw new IllegalArgumentException("not enough balance");
    }
  }
}
