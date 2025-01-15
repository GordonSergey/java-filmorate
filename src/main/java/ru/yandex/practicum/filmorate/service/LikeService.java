package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LikeService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final Map<Integer, Set<Integer>> filmLikes = new HashMap<>();

    public LikeService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void addLike(int filmId, int userId) {

        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NoSuchElementException("Film with ID " + filmId + " not found."));

        boolean userExists = userStorage.getUserById(userId).isPresent();
        if (!userExists) {
            throw new NoSuchElementException("User with ID " + userId + " not found.");
        }

        filmLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
    }

    public void removeLike(int filmId, int userId) {

        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NoSuchElementException("Film with ID " + filmId + " not found."));

        Set<Integer> likes = filmLikes.getOrDefault(filmId, new HashSet<>());
        if (!likes.remove(userId)) {
            throw new NoSuchElementException("User with ID " + userId + " has not liked this film.");
        }
    }

    public List<Film> getPopularFilms(int count) {
        return filmLikes.entrySet().stream()
                .sorted((a, b) -> b.getValue().size() - a.getValue().size())
                .limit(count)
                .map(entry -> filmStorage.getFilmById(entry.getKey())
                        .orElseThrow(() -> new NoSuchElementException("Film with ID " + entry.getKey() + " not found.")))
                .collect(Collectors.toList());
    }
}