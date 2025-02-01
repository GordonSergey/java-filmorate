package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.mapper.FilmWithGenresExtractor;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final JdbcTemplate jdbcTemplate;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreDbStorage genreDbStorage;
    private final LikeDbStorage likeDbStorage;
    private final MpaDbStorage mpaDbStorage;

    public Film addFilm(Film film) {
        validateFilm(film);
        film.setGenres(removeDuplicateGenres(film.getGenres()));
        List<Integer> ids = film.getGenres()
                .stream()
                .map(Genre::getId)
                .toList();
        if (!genreDbStorage.existsGenresByIds(ids)) {
            throw new NoSuchElementException("Genre id not exists");
        }
        if (!mpaDbStorage.existsMpaById(film.getMpa().getId())) {
            throw new NoSuchElementException("Mpa id not exists");
        }
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        Film existingFilm = getFilmById(film.getId());

        validateFilm(film);

        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            film.setDirectors(existingFilm.getDirectors());
        }

        return filmStorage.updateFilm(film);
    }

    public void deleteFilm(int id) {
        if (!filmStorage.existsById(id)) {
            throw new NoSuchElementException("Film with ID " + id + " not found.");
        }
        filmStorage.deleteFilm(id);
    }

    public Film getFilmById(int id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new NoSuchElementException("Film with ID " + id + " not found."));
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public void addLike(int filmId, int userId) {
        getFilmById(filmId);
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NoSuchElementException("User with ID " + userId + " not found."));
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        getFilmById(filmId);
        if (!likeDbStorage.existsLike(filmId, userId)) {
            throw new NoSuchElementException("User with ID " + userId + " has not liked this film.");
        }
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        return filmStorage.getFilmsByDirector(directorId, sortBy);
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NoSuchElementException("User with ID " + userId + " not found."));
        userStorage.getUserById(friendId)
                .orElseThrow(() -> new NoSuchElementException("User with ID " + friendId + " not found."));

        String query = """
    SELECT f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
           r.id AS rating_id, r.name AS rating_name,
           g.id AS genre_id, g.name AS genre_name,
           (SELECT COUNT(*) FROM likes l WHERE l.film_id = f.id) AS likes_count
    FROM films f
    JOIN likes l1 ON f.id = l1.film_id
    JOIN likes l2 ON f.id = l2.film_id
    LEFT JOIN ratings r ON f.rating_id = r.id
    LEFT JOIN film_genres fg ON f.id = fg.film_id
    LEFT JOIN genres g ON fg.genre_id = g.id
    WHERE l1.user_id = ? AND l2.user_id = ?
    ORDER BY likes_count DESC;
    """;

        try {
            return jdbcTemplate.query(query, new FilmWithGenresExtractor(), userId, friendId);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected database error occurred while fetching common films.");
        }
    }

    public List<Film> getPopularFilms(int limit, long genreId, int year) {
        List<Film> films = filmStorage.getPopularsFilms(genreId, year);

        return films.stream()
                .filter(f -> filmStorage.existsById(f.getId()))
                .sorted()
                .limit(limit > 0 ? limit : films.size())
                .toList();
    }

    public List<Film> searchFilms(String query, String by) {
        if (!by.equals("title") && !by.equals("director") && !by.equals("title,director")) {
            throw new IllegalArgumentException("Invalid search parameter: " + by);
        }
        return filmStorage.searchFilms(query, by);
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Film name cannot be empty.");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new IllegalArgumentException("Description is too long (max 200 characters).");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(LocalDate.of(1900, 1, 1))) {
            throw new IllegalArgumentException("Release date must not be before 1900.");
        }
        if (film.getDuration() <= 0) {
            throw new IllegalArgumentException("Duration must be positive.");
        }
    }

    private List<Genre> removeDuplicateGenres(List<Genre> genres) {
        return genres.stream()
                .distinct()
                .toList();
    }
}