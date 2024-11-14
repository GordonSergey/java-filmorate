package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setEmail("validemail@example.com");
        validUser.setLogin("validlogin");
        validUser.setName("Valid Name");
        validUser.setBirthday(LocalDate.of(2000, 1, 1));
    }

    @Test
    @DisplayName("Ошибка при пустой электронной почте")
    void addUserWithEmptyEmailThrowsValidationException() {
        validUser.setEmail("");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> new UserController().addUser(validUser));
        assert (exception.getMessage().contains("Электронная почта должна содержать символ '@'."));
    }

    @Test
    @DisplayName("Ошибка при неверном формате электронной почты")
    void addUserWithInvalidEmailThrowsValidationException() {
        validUser.setEmail("invalidemail.com");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> new UserController().addUser(validUser));
        assert (exception.getMessage().contains("Электронная почта должна содержать символ '@'."));
    }

    @Test
    @DisplayName("Ошибка при пустом логине")
    void addUserWithEmptyLoginThrowsValidationException() {
        validUser.setLogin("");
        ValidationException exception = assertThrows(ValidationException.class, () -> new UserController().addUser(validUser));
        assert(exception.getMessage().contains("Логин не может быть пустым и содержать пробелы."));
    }

    @Test
    @DisplayName("Ошибка при логине с пробелами")
    void addUserWithLoginContainingSpacesThrowsValidationException() {
        validUser.setLogin("invalid login");
        ValidationException exception = assertThrows(ValidationException.class, () -> new UserController().addUser(validUser));
        assert(exception.getMessage().contains("Логин не может быть пустым и содержать пробелы."));
    }

    @Test
    @DisplayName("Ошибка при дате рождения в будущем")
    void addUserWithFutureBirthdayThrowsValidationException() {
        validUser.setBirthday(LocalDate.now().plusDays(1));
        ValidationException exception = assertThrows(ValidationException.class, () -> new UserController().addUser(validUser));
        assert(exception.getMessage().contains("Дата рождения не может быть в будущем."));
    }
}