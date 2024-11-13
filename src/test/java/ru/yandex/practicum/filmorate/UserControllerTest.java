package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    @DisplayName("Добавление корректного пользователя")
    void addValidUser() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "validemail@example.com",
                                    "login": "validlogin",
                                    "name": "Valid Name",
                                    "birthday": "2000-01-01"
                                }"""))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "email": "validemail@example.com",
                            "login": "validlogin",
                            "name": "Valid Name",
                            "birthday": "2000-01-01"
                        }"""));
    }

    @Test
    @DisplayName("Ошибка при пустой электронной почте")
    void addUserWithEmptyEmailThrowsValidationException() {
        validUser.setEmail("");
        ValidationException exception = assertThrows(ValidationException.class, () -> new UserController().addUser(validUser));
        assert(exception.getMessage().contains("Электронная почта должна содержать символ '@'."));
    }

    @Test
    @DisplayName("Ошибка при неверном формате электронной почты")
    void addUserWithInvalidEmailThrowsValidationException() {
        validUser.setEmail("invalidemail.com");
        ValidationException exception = assertThrows(ValidationException.class, () -> new UserController().addUser(validUser));
        assert(exception.getMessage().contains("Электронная почта должна содержать символ '@'."));
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
    @DisplayName("Ошибка при пустом имени пользователя")
    void addUserWithEmptyNameUsesLoginAsName() throws Exception {
        validUser.setName("");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "validemail@example.com",
                                    "login": "validlogin",
                                    "name": "",
                                    "birthday": "2000-01-01"
                                }"""))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "email": "validemail@example.com",
                            "login": "validlogin",
                            "name": "validlogin",
                            "birthday": "2000-01-01"
                        }"""));
    }

    @Test
    @DisplayName("Ошибка при дате рождения в будущем")
    void addUserWithFutureBirthdayThrowsValidationException() {
        validUser.setBirthday(LocalDate.now().plusDays(1));
        ValidationException exception = assertThrows(ValidationException.class, () -> new UserController().addUser(validUser));
        assert(exception.getMessage().contains("Дата рождения не может быть в будущем."));
    }

    @Test
    @DisplayName("Обновление корректного пользователя")
    void updateValidUser() throws Exception {
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email": "validemail@example.com",
                                    "login": "validlogin",
                                    "name": "Updated Name",
                                    "birthday": "2000-01-01"
                                }"""))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                            "email": "validemail@example.com",
                            "login": "validlogin",
                            "name": "Updated Name",
                            "birthday": "2000-01-01"
                        }"""));
    }
}