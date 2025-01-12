package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

    private final GenreDbStorage genreDbStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Film> mapper, GenreDbStorage genreDbStorage) {
        super(jdbcTemplate, mapper);
        this.genreDbStorage = genreDbStorage;
    }

    @Override
    public Film addFilm(Film film) {
        String query = """
                 INSERT INTO films (name, description, release_date, duration, rating_id)
                            VALUES (?, ?, ?, ?, ?)
                """;

        int filmId = insert(query, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getMpa().getId());
        film.setId(filmId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String insertGenresSql = """
                    INSERT INTO film_genres (film_id, genre_id)
                    VALUES (?, ?)
                    """;

            List<Object[]> batchParams = film.getGenres().stream()
                    .map(genre -> new Object[]{filmId, genre.getId()})
                    .toList();

            jdbcTemplate.batchUpdate(insertGenresSql, batchParams);
        }
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String query = """
                UPDATE films
                SET    name = ?,
                       description = ?,
                       release_date = ?,
                       rating_id = ?
                WHERE  id = ?""";
        update(query, film.getName(), film.getDescription(), film.getReleaseDate(), film.getMpa().getId(), film.getId());
        return film;
    }

    @Override
    public Optional<Film> getFilmById(int id) {
        String query = """
                SELECT 
                    f.id AS film_id,
                    f.name AS film_name,
                    f.description,
                    f.release_date,
                    f.duration,
                    r.id AS rating_id,
                    r.name AS rating_name 
                FROM 
                    films f
                LEFT JOIN ratings r ON f.rating_id = r.id
                WHERE 
                    f.id = ?
                """;

        Optional<Film> optionalFilm = findOne(query, id);
        if (optionalFilm.isEmpty()) {
            return Optional.empty();
        }

        Film film = optionalFilm.get();
        List<Genre> genres = genreDbStorage.getGenresByFilmId(id);
        film.setGenres(genres);

        return optionalFilm;
    }

    @Override
    public List<Film> getAllFilms() {
        String query = """
                 SELECT
                       f.id AS film_id,
                       f.name AS film_name,
                       f.description,
                       f.release_date,
                       f.duration,
                       r.id AS rating_id,
                       r.name AS rating_name
                FROM
                       films f
                LEFT JOIN ratings r ON f.rating_id = r.id;
                """;
        List<Film> films = findMany(query);
        Map<Integer, List<Genre>> filmsGenres = getFilmsGenres();
        Map<Integer, List<Integer>> filmsLikes = getFilmsLikes();
        for (Film film : films) {
            film.getGenres().addAll(filmsGenres.getOrDefault(film.getId(), List.of()));
            film.getLikes().addAll(filmsLikes.getOrDefault(film.getId(), List.of()));
        }
        return films;
    }

    @Override
    public void addLike(int filmId, int userId) {
        String query = """
                INSERT INTO likes (user_id, film_id)
                VALUES (?, ?)""";
        insert(query, userId, filmId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String query = """
                DELETE FROM likes
                WHERE user_id = ? AND film_id = ?""";
        delete(query, userId, filmId);
    }

    private Map<Integer, List<Genre>> getFilmsGenres() {
        Map<Integer, List<Genre>> filmsGenres = new HashMap<>();
        String query = "SELECT fg.film_id, g.id, g.name FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id";
        jdbcTemplate.query(query, (rs) -> {
            int filmId = rs.getInt("film_id");
            Genre genre = new Genre(rs.getInt("id"), rs.getString("name"));
            filmsGenres.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
        });
        return filmsGenres;
    }

    private Map<Integer, List<Integer>> getFilmsLikes() {
        Map<Integer, List<Integer>> filmsLikes = new HashMap<>();
        String query = "SELECT user_id, film_id FROM likes";
        jdbcTemplate.query(query, (rs) -> {
            int filmId = rs.getInt("film_id");
            int userId = rs.getInt("user_id");
            filmsLikes.computeIfAbsent(filmId, k -> new ArrayList<>()).add(userId);
        });
        return filmsLikes;
    }
}