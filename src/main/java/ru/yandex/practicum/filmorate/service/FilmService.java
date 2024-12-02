package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final Map<Integer, Set<Integer>> filmLikes = new ConcurrentHashMap<>();

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        log.info("Adding film: {}", film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        getFilmById(film.getId());
        validateFilm(film);
        log.info("Updating film: {}", film);
        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> {
                    log.warn("Film with ID {} not found.", id);
                    return new NoSuchElementException("Film with ID " + id + " not found.");
                });
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public void addLike(int filmId, int userId) {
        log.info("Adding like to film ID: {} by user ID: {}", filmId, userId);
        getFilmById(filmId);
        userStorage.getUserById(userId)
                .orElseThrow(() -> {
                    log.warn("User with ID {} not found.", userId);
                    return new NoSuchElementException("User with ID " + userId + " not found.");
                });
        filmLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        log.info("Like added successfully to film ID: {} by user ID: {}", filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        log.info("Removing like from film ID: {} by user ID: {}", filmId, userId);
        getFilmById(filmId);
        Set<Integer> likes = filmLikes.getOrDefault(filmId, new HashSet<>());
        if (!likes.remove(userId)) {
            log.warn("User with ID {} has not liked film ID: {}", userId, filmId);
            throw new NoSuchElementException("User with ID " + userId + " has not liked this film.");
        }
        log.info("Like successfully removed from film ID: {} by user ID: {}", filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmLikes.entrySet().stream()
                .sorted(Comparator.<Map.Entry<Integer, Set<Integer>>>comparingInt(entry -> entry.getValue().size())
                        .reversed()
                        .thenComparingInt(Map.Entry::getKey))
                .limit(count)
                .map(entry -> getFilmById(entry.getKey()))
                .collect(Collectors.toList());
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().trim().isEmpty()) {
            log.error("Validation failed: Film name is empty.");
            throw new IllegalArgumentException("Film name cannot be empty.");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.error("Validation failed: Film description exceeds 200 characters.");
            throw new IllegalArgumentException("Description is too long (max 200 characters).");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(LocalDate.of(1900, 1, 1))) {
            log.error("Validation failed: Release date is before 1900.");
            throw new IllegalArgumentException("Release date must not be before 1900.");
        }
        if (film.getDuration() <= 0) {
            log.error("Validation failed: Film duration is non-positive.");
            throw new IllegalArgumentException("Duration must be positive.");
        }
        log.info("Film validation successful for film: {}", film);
    }
}