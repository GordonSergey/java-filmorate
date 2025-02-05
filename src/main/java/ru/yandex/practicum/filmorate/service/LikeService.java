package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LikeService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikeDbStorage likeDbStorage;
    private final Map<Integer, Set<Integer>> filmLikes = new HashMap<>();

    public LikeService(FilmStorage filmStorage, UserStorage userStorage, LikeDbStorage likeDbStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likeDbStorage = likeDbStorage;
    }

    public void addLike(int filmId, int userId) {
        userStorage.logEvent(userId, filmId, "LIKE", "ADD");

        filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NoSuchElementException("Film not found"));
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        userStorage.logEvent(userId, filmId, "LIKE", "REMOVE");

        filmStorage.removeLike(filmId, userId);
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