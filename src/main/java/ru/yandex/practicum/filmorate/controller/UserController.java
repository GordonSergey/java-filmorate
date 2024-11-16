package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.DataValidation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
@SuppressWarnings("unused")
public class UserController {

    private final List<User> users = new ArrayList<>();
    private final DataValidation validationService;
    private int userIdCounter = 1;

    public UserController(DataValidation validationService) {
        this.validationService = validationService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Fetching all users.");
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<User> addUser(@RequestBody User user) {
        validationService.validate(user);

        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }

        user.setId(userIdCounter++);
        users.add(user);
        log.info("User created successfully with login: {}, name: {}", user.getLogin(), user.getName());
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<?> modifyUser(@RequestBody User user) {
        validationService.validate(user);

        for (User existingUser : users) {
            if (existingUser.getId() == user.getId()) {
                existingUser.setEmail(user.getEmail());
                existingUser.setLogin(user.getLogin());
                existingUser.setName(user.getName() != null ? user.getName() : user.getLogin());
                existingUser.setBirthday(user.getBirthday());
                log.info("User updated successfully with ID: {}", user.getId());
                return ResponseEntity.ok(existingUser);
            }
        }

        log.warn("User with ID {} not found for update.", user.getId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "User with ID " + user.getId() + " not found."));
    }
}