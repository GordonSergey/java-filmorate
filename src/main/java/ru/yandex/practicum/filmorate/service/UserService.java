package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        validateUser(user);
        return userStorage.addUser(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User updateUser(User user) {
        validateUser(user);

        Optional<User> existingUser = userStorage.getUserById(user.getId());
        if (existingUser.isEmpty()) {
            throw new NoSuchElementException("User with ID " + user.getId() + " not found.");
        }

        return userStorage.updateUser(user);
    }

    public void deleteUser(int id) {
        if (!userStorage.existsUserById(id)) {
            throw new NoSuchElementException("User with ID " + id + " not found.");
        }

        userStorage.removeAllFriends(id);
        userStorage.deleteUser(id);
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