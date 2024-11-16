package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final List<User> users = new ArrayList<>();
    private int userIdCounter = 1;

    // Создание пользователя
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @Valid User user) {
        log.info("Creating new user: {}", user.getLogin());
        user.setId(userIdCounter++);
        users.add(user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    // Обновление пользователя
    @PutMapping
    public ResponseEntity<User> updateUser(@RequestBody @Valid User user) {
        for (User existingUser : users) {
            if (existingUser.getId() == user.getId()) {
                log.info("Updating user with ID: {}", user.getId());
                existingUser.setEmail(user.getEmail());
                existingUser.setLogin(user.getLogin());
                existingUser.setName(user.getName() != null ? user.getName() : user.getLogin());
                existingUser.setBirthday(user.getBirthday());
                return ResponseEntity.ok(existingUser);
            }
        }
        log.error("User with ID {} not found for update", user.getId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // Получение всех пользователей
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Fetching all users");
        return ResponseEntity.ok(users);
    }

    // Обработка ошибок валидации
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleValidationException(IllegalArgumentException ex) {
        log.error("Validation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}