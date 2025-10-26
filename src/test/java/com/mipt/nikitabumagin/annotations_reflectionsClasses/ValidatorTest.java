package com.mipt.nikitabumagin.annotations_reflectionsClasses;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mipt.nikitabumagin.annotations_reflectionsClasses.entities.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidatorTest {

  private TestUser user;

  @BeforeEach
  void setUp() {
    user = new TestUser();
  }

  @Test
  void testPerfectUserIsValid() {
    user.setName("Nikita");
    user.setEmail("bumagin@mail.ru");
    user.setAge(18);
    user.setPassword("123456789");

    ValidationResult result = Validator.validate(user);
    assertTrue(result.isValid());
    assertEquals(0, result.getErrors().size());
  }

  @Test
  void testNullNameIsNotValid() {
    user.setName(null);
    ValidationResult result = Validator.validate(user);
    assertFalse(result.isValid());
    assertTrue(result.getErrors().contains("Имя не может быть null"));
    assertTrue(result.getErrors().contains("Имя должно быть от 2 до 50 символов"));
  }

  @Test
  void testLongNameIsNotValid() {
    user.setName("A".repeat(52));
    ValidationResult result = Validator.validate(user);
    assertFalse(result.isValid());
    assertTrue(result.getErrors().contains("Имя должно быть от 2 до 50 символов"));
  }

  @Test
  void testShortNameIsNotValid() {
    user.setName("A");
    ValidationResult result = Validator.validate(user);
    assertFalse(result.isValid());
    assertTrue(result.getErrors().contains("Имя должно быть от 2 до 50 символов"));
  }

  @Test
  void testNullEmailIsNotValid() {
    user.setEmail(null);
    ValidationResult result = Validator.validate(user);
    assertFalse(result.isValid());
    assertTrue(result.getErrors().contains("Email не может быть null"));
    assertTrue(result.getErrors().contains("Некорректный формат email"));
  }

  @Test
  void testIncorrectEmailIsNotValid() {
    user.setEmail("bumagin@mail");
    ValidationResult result = Validator.validate(user);
    assertTrue(result.getErrors().contains("Некорректный формат email"));

    user.setEmail("");
    result = Validator.validate(user);
    assertTrue(result.getErrors().contains("Некорректный формат email"));

    user.setEmail("@mail.ru");
    result = Validator.validate(user);
    assertTrue(result.getErrors().contains("Некорректный формат email"));

    user.setEmail("bumagin");
    result = Validator.validate(user);
    assertTrue(result.getErrors().contains("Некорректный формат email"));

    user.setEmail("bumagin.ns");
    result = Validator.validate(user);
    assertTrue(result.getErrors().contains("Некорректный формат email"));

    user.setEmail("bumag@.ru");
    result = Validator.validate(user);
    assertTrue(result.getErrors().contains("Некорректный формат email"));
  }

  @Test
  void testNullAgeIsNotValid() {
    user.setAge(null);
    ValidationResult result = Validator.validate(user);
    assertTrue(result.getErrors().contains("Возраст должен быть от 0 до 150"));

    user.setAge(-1000);
    result = Validator.validate(user);
    assertTrue(result.getErrors().contains("Возраст должен быть от 0 до 150"));

    user.setAge(1000);
    result = Validator.validate(user);
    assertTrue(result.getErrors().contains("Возраст должен быть от 0 до 150"));

    user.setAge(-1);
    result = Validator.validate(user);
    assertTrue(result.getErrors().contains("Возраст должен быть от 0 до 150"));

    user.setAge(151);
    result = Validator.validate(user);
    assertTrue(result.getErrors().contains("Возраст должен быть от 0 до 150"));
  }

  @Test
  void testPasswordIsNotValid() {
    user.setPassword(null);
    ValidationResult result = Validator.validate(user);
    assertTrue(result.getErrors().contains("Пароль должен быть от 6 до 20 символов"));

    user.setPassword("");
    result = Validator.validate(user);
    assertTrue(result.getErrors().contains("Пароль должен быть от 6 до 20 символов"));

    user.setPassword("12345");
    result = Validator.validate(user);
    assertTrue(result.getErrors().contains("Пароль должен быть от 6 до 20 символов"));

    user.setPassword("0".repeat(100));
    result = Validator.validate(user);
    assertTrue(result.getErrors().contains("Пароль должен быть от 6 до 20 символов"));

    user.setPassword("0".repeat(21));
    result = Validator.validate(user);
    assertTrue(result.getErrors().contains("Пароль должен быть от 6 до 20 символов"));
  }

  @Test
  void testMultipleErrors() {
    user = new TestUser("A", "invalid", 1000, "123");
    ValidationResult result = Validator.validate(user);

    assertFalse(result.isValid());
    assertEquals(4, result.getErrors().size());
    assertTrue(result.getErrors().contains("Имя должно быть от 2 до 50 символов"));
    assertTrue(result.getErrors().contains("Некорректный формат email"));
    assertTrue(result.getErrors().contains("Возраст должен быть от 0 до 150"));
    assertTrue(result.getErrors().contains("Пароль должен быть от 6 до 20 символов"));
  }

  @Test
  void nullObject_ShouldThrowException() {
    assertThrows(NullPointerException.class, () -> {
      Validator.validate(null);
    });
  }
}
