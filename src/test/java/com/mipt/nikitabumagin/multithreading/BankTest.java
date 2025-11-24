package com.mipt.nikitabumagin.multithreading;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;

public class BankTest {

  @Test
  public void testConcurrentTransfers_noDeadlock() throws Exception {
    BankAccount a = new BankAccount(10_000L);
    BankAccount b = new BankAccount(10_000L);

    int threadsCount = 20;
    List<Thread> threads = new ArrayList<>(threadsCount);
    CountDownLatch startLatch = new CountDownLatch(1);

    for (int i = 0; i < threadsCount; i++) {
      boolean direction = i % 2 == 0;

      Thread t = new Thread(() -> {
        try {
          startLatch.await();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }

        for (int k = 0; k < 50; k++) {
          if (direction) {
            Bank.sendToAccount(a, b, 1);
          } else {
            Bank.sendToAccount(b, a, 1);
          }
        }
      });

      threads.add(t);
      t.start();
    }

    startLatch.countDown();

    for (Thread t : threads) {
      t.join(5000);
    }

    long total = a.getBalance() + b.getBalance();
    assertEquals(20_000L, total);
    assertTrue(a.getBalance() >= 0);
    assertTrue(b.getBalance() >= 0);
  }

  @Test
  public void testDeadlockDetected() throws Exception {
    BankAccount a = new BankAccount(1000L);
    BankAccount b = new BankAccount(1000L);

    Thread t1 = new Thread(() -> {
      for (int i = 0; i < 100; i++) {
        Bank.sendToAccountDeadlock(a, b, 1);
      }
    });

    Thread t2 = new Thread(() -> {
      for (int i = 0; i < 100; i++) {
        Bank.sendToAccountDeadlock(b, a, 1);
      }
    });

    t1.start();
    t2.start();

    t1.join(2000);
    t2.join(2000);

    boolean deadlockOccurred = t1.isAlive() && t2.isAlive();

    if (t1.isAlive()) {
      t1.interrupt();
    }
    if (t2.isAlive()) {
      t2.interrupt();
    }

    assertTrue(deadlockOccurred, "Ожидался дедлок, но потоки завершились");
  }

  @Test
  public void testValidationAndNulls() {
    BankAccount a = new BankAccount(100L);
    BankAccount b = new BankAccount(100L);

    assertThrows(IllegalArgumentException.class, () -> Bank.sendToAccount(null, b, 1));
    assertThrows(IllegalArgumentException.class, () -> Bank.sendToAccount(a, null, 1));
    assertThrows(IllegalArgumentException.class, () -> Bank.sendToAccount(a, b, 0));
    assertThrows(IllegalArgumentException.class, () -> Bank.sendToAccount(a, b, -5));
    assertThrows(IllegalArgumentException.class, () -> Bank.sendToAccount(a, a, 1));
  }

  @Test
  public void testInsufficientFunds() {
    BankAccount a = new BankAccount(50L);
    BankAccount b = new BankAccount(100L);

    assertThrows(IllegalArgumentException.class, () -> Bank.sendToAccount(a, b, 100L));
  }

  @Test
  public void testBankAccountValidation() {
    assertThrows(IllegalArgumentException.class, () -> new BankAccount(null));
    assertThrows(IllegalArgumentException.class, () -> new BankAccount(-1L));

    BankAccount account = new BankAccount(100L);

    assertThrows(IllegalArgumentException.class, () -> account.withdraw(null));
    assertThrows(IllegalArgumentException.class, () -> account.withdraw(0L));
    assertThrows(IllegalArgumentException.class, () -> account.withdraw(-1L));
    assertThrows(IllegalArgumentException.class, () -> account.withdraw(200L));

    assertThrows(IllegalArgumentException.class, () -> account.deposit(null));
    assertThrows(IllegalArgumentException.class, () -> account.deposit(0L));
    assertThrows(IllegalArgumentException.class, () -> account.deposit(-1L));
  }

  @Test
  public void testSingleTransfer() {
    BankAccount a = new BankAccount(100L);
    BankAccount b = new BankAccount(50L);

    Bank.sendToAccount(a, b, 30L);

    assertEquals(70L, a.getBalance());
    assertEquals(80L, b.getBalance());
  }
}