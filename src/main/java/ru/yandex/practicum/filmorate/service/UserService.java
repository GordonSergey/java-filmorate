package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public UserService(UserStorage userStorage, FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
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

    public List<Film> findRecommendedFilms(int id) {
        if (!userStorage.existsUserById(id)) {
            throw new NoSuchElementException("User with ID " + id + " not found.");
        }

        Optional<Integer> maxCoincidence = userStorage.findUserWithSharedFilms(id);

        if (maxCoincidence.isEmpty()) {
            return List.of();
        }

        List<Film> filmsUser1 = Optional.ofNullable(filmStorage.getLikesUser(maxCoincidence.get()))
                .orElse(Collections.emptyList());

        List<Film> filmsUser2 = Optional.ofNullable(filmStorage.getLikesUser(id))
                .orElse(Collections.emptyList());

        return filmsUser1.stream()
                .filter(film -> !filmsUser2.contains(film))
                .collect(Collectors.toList());
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