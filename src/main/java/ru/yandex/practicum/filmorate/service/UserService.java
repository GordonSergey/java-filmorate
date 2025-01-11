package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        log.info("Adding new user: {}", user);
        validateUser(user);
        return userStorage.addUser(user);
    }

    public List<User> getAllUsers() {
        log.info("Fetching all users.");
        return userStorage.getAllUsers();
    }

    public User updateUser(User user) {
        log.info("Updating user: {}", user);
        validateUser(user);

        Optional<User> existingUser = userStorage.getUserById(user.getId());
        if (existingUser.isEmpty()) {
            log.warn("User with ID {} not found.", user.getId());
            throw new NoSuchElementException("User with ID " + user.getId() + " not found.");
        }

        return userStorage.updateUser(user);
    }

    public Optional<User> getUserById(int id) {
        return userStorage.getUserById(id);
    }

    private void validateUser(User user) {
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Login cannot contain spaces");
        }
        if (user.getEmail() != null && !user.getEmail().contains("@")) {
            throw new ValidationException("Email must be valid");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Birthday cannot be in the future");
        }
    }
}