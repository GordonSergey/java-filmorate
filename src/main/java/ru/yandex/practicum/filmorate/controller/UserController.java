package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final List<User> users = new ArrayList<>();
    private int currentId = 1;

    @PostMapping
    public User addUser(@RequestBody @Valid User user) {
        validateUser(user);
        user.setId(currentId++);
        users.add(user);
        log.info("Пользователь добавлен: {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody @Valid User user) {
        validateUser(user);
        Optional<User> existingUser = users.stream()
                .filter(u -> u.getId() == user.getId())
                .findFirst();

        if (existingUser.isPresent()) {
            users.remove(existingUser.get());
            users.add(user);
            log.info("Пользователь обновлён: {}", user);
            return user;
        } else {
            throw new ValidationException("Пользователь с таким ID не найден.");
        }
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта должна содержать символ '@'.");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
    }
}