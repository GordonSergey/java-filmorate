package ru.yandex.practicum.filmorate;

import ru.yandex.practicum.filmorate.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.ConstraintViolation;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserValidationTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void shouldPassValidationForValidUser() {
        User user = new User();
        user.setEmail("validuser@example.com");
        user.setLogin("validLogin");
        user.setName("Valid User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Valid user should pass validation");
    }

    @Test
    public void shouldFailValidationWhenEmailIsInvalid() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("userLogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Invalid email should fail validation");
    }

    @Test
    public void shouldFailValidationWhenLoginIsBlank() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Blank login should fail validation");
    }

    @Test
    public void shouldFailValidationWhenBirthdayIsInFuture() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("userLogin");
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Future birthday should fail validation");
    }
}