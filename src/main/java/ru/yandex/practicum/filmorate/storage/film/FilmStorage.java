package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    Optional<Film> getFilmById(int id);

    List<Film> getAllFilms();

    void deleteFilm(int id);

    boolean existsById(int id);

    List<Film> getFilmsByDirector(int directorId, String sortBy);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    List<Film> getPopularsFilms(long genreId, int year);

    List<Film> searchFilms(String query, String by);

    List<Film> getCommonFilms(int userId, int friendId);

    List<Film> getLikesUser(int id);
}