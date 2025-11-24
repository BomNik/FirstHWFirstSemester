package com.mipt.nikitabumagin.multithreading;

import java.util.concurrent.atomic.AtomicLong;

public class BankAccount {

  private static final AtomicLong idGenerator = new AtomicLong(1);

  private final long id;
  private Long balance;

  public BankAccount(Long balance) {
    if (balance == null) {
      throw new IllegalArgumentException("balance cannot be null");
    }
    if (balance < 0) {
      throw new IllegalArgumentException("balance is negative");
    }

    this.id = idGenerator.getAndIncrement();
    this.balance = balance;
  }

  public Long getId() {
    return id;
  }

  public Long getBalance() {
    return balance;
  }

  public void withdraw(Long amount) {
    if (amount == null) {
      throw new IllegalArgumentException("amount cannot be null");
    }
    if (amount <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }
    if (balance < amount) {
      throw new IllegalArgumentException("Balance cannot be negative");
    }

    balance -= amount;
  }

  public void deposit(Long amount) {
    if (amount == null) {
      throw new IllegalArgumentException("amount cannot be null");
    }
    if (amount <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }
    balance += amount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof BankAccount)) {
      return false;
    }
    return id == ((BankAccount) o).id;
  }
}
