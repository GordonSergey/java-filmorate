package ru.yandex.practicum.filmorate.storage.film;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.FilmWithDirectorsExtractor;
import ru.yandex.practicum.filmorate.mapper.FilmWithGenresExtractor;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@Transactional
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

    private static final Logger log = LoggerFactory.getLogger(FilmDbStorage.class);

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

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            String insertDirectorsSql = """
                    INSERT INTO film_directors (film_id, director_id)
                    VALUES (?, ?)
                    """;

            List<Object[]> batchParams = film.getDirectors().stream()
                    .map(director -> new Object[]{filmId, director.getId()})
                    .toList();

            jdbcTemplate.batchUpdate(insertDirectorsSql, batchParams);
        }

        return film;
    }

    public Film updateFilm(Film film) {
        String query = """
                UPDATE films
                SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ?
                WHERE id = ?""";

        int rowsAffected = update(query, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa() != null ? film.getMpa().getId() : null, film.getId());

        if (rowsAffected == 0) {
            throw new NoSuchElementException("Film with ID " + film.getId() + " not found.");
        }

        String deleteDirectorsQuery = "DELETE FROM film_directors WHERE film_id = ?";
        update(deleteDirectorsQuery, film.getId());

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            String insertDirectorQuery = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
            for (Director director : film.getDirectors()) {
                update(insertDirectorQuery, film.getId(), director.getId());
            }
        }

        return getFilmById(film.getId())
                .orElseThrow(() -> new NoSuchElementException("Film not found after update, ID: " + film.getId()));
    }

    public Optional<Film> getFilmById(int id) {
        String sql = """
                SELECT f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
                       r.id AS rating_id, r.name AS rating_name,
                       g.id AS genre_id, g.name AS genre_name
                FROM films f
                LEFT JOIN ratings r ON f.rating_id = r.id
                LEFT JOIN film_genres fg ON f.id = fg.film_id
                LEFT JOIN genres g ON fg.genre_id = g.id
                WHERE f.id = ?
                """;

        Optional<Film> film = jdbcTemplate.query(sql, new FilmWithGenresExtractor(), id)
                .stream().findFirst();

        film.ifPresent(f -> f.getDirectors().addAll(getFilmsDirectors().getOrDefault(f.getId(), List.of())));

        return film;
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
        Map<Integer, List<Director>> filmsDirectors = getFilmsDirectors();

        for (Film film : films) {
            film.getGenres().addAll(filmsGenres.getOrDefault(film.getId(), List.of()));
            film.getLikes().addAll(filmsLikes.getOrDefault(film.getId(), List.of()));
            film.getDirectors().addAll(filmsDirectors.getOrDefault(film.getId(), List.of()));
        }

        return films;
    }

    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        String orderBy = switch (sortBy) {
            case "year" -> "f.release_date";
            case "likes" -> "(SELECT COUNT(*) FROM likes WHERE film_id = f.id) DESC";
            default ->
                    throw new IllegalArgumentException("Некорректный параметр sortBy. Используйте 'year' или 'likes'.");
        };

        String query = """
                    SELECT f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
                           r.id AS rating_id, r.name AS rating_name,
                           d.id AS director_id, d.name AS director_name
                    FROM films f
                    LEFT JOIN ratings r ON f.rating_id = r.id
                    LEFT JOIN film_directors fd ON f.id = fd.film_id
                    LEFT JOIN directors d ON fd.director_id = d.id
                    WHERE fd.director_id = ?
                    ORDER BY %s
                """.formatted(orderBy);

        return jdbcTemplate.query(query, new FilmWithDirectorsExtractor(), directorId);
    }

    private Map<Integer, List<Director>> getFilmsDirectors() {
        String sql = """
                    SELECT fd.film_id, d.id, d.name
                    FROM film_directors fd
                    JOIN directors d ON fd.director_id = d.id
                """;

        Map<Integer, List<Director>> filmsDirectors = new HashMap<>();

        jdbcTemplate.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            Director director = new Director(rs.getInt("id"), rs.getString("name"));
            filmsDirectors.computeIfAbsent(filmId, k -> new ArrayList<>()).add(director);
        });

        return filmsDirectors;
    }

    private void loadGenres(List<Film> films) {
        if (films.isEmpty()) return;

        String sql = """
                    SELECT fg.film_id, g.id, g.name
                    FROM film_genres fg
                    JOIN genres g ON fg.genre_id = g.id
                    WHERE fg.film_id IN (%s)
                """.formatted(films.stream().map(f -> "?").collect(Collectors.joining(",")));

        Map<Integer, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, f -> f));

        jdbcTemplate.query(sql, rs -> {
            int filmId = rs.getInt("film_id");
            Film film = filmMap.get(filmId);
            if (film != null) {
                film.getGenres().add(new Genre(rs.getInt("id"), rs.getString("name")));
            }
        }, films.stream().map(Film::getId).toArray());
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


    @Override
    public List<Film> getPopularsFilms(long genreId, int year) {
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
                """;
        List<Film> films;
        if (genreId != 0 && year != 0) {
            query = String.format(query + "JOIN film_genres fg ON fg.film_id = f.id " +
                    "WHERE gf.genre_id = ? " +
                    "AND YEAR(release_date) = ?;");
            films = findMany(query, genreId, year);
        } else if (genreId != 0 && year == 0) {
            query = String.format(query + "JOIN film_genres fg ON fg.film_id = f.id " +
                    "WHERE gf.genre_id = ?;");
            films = findMany(query, genreId);
        } else if (genreId == 0 && year != 0) {
            query = String.format(query + "WHERE YEAR(release_date) = ?;");
            films = findMany(query, year);
        } else {
            films = findMany(query);
        }

        Map<Integer, List<Genre>> filmsGenres = getFilmsGenres();
        Map<Integer, List<Integer>> filmsLikes = getFilmsLikes();
        for (Film film : films) {
            film.getGenres().addAll(filmsGenres.getOrDefault(film.getId(), List.of()));
            film.getLikes().addAll(filmsLikes.getOrDefault(film.getId(), List.of()));
        }
        return films;
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