package ru.yandex.practicum.filmorate.storage.film;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
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
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Transactional
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

    private final GenreDbStorage genreDbStorage;
    private final JdbcTemplate jdbcTemplate;
    private final UserStorage userStorage;
    private final RowMapper<Film> filmRowMapper;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Film> filmRowMapper, GenreDbStorage genreDbStorage, UserStorage userStorage) {
        super(jdbcTemplate, filmRowMapper);
        this.jdbcTemplate = jdbcTemplate;
        this.filmRowMapper = filmRowMapper;
        this.genreDbStorage = genreDbStorage;
        this.userStorage = userStorage;
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

    @Override
    public void deleteFilm(int id) {
        String deleteLikesQuery = "DELETE FROM likes WHERE film_id = ?";
        String deleteGenresQuery = "DELETE FROM film_genres WHERE film_id = ?";
        String deleteDirectorsQuery = "DELETE FROM film_directors WHERE film_id = ?";

        jdbcTemplate.update(deleteLikesQuery, id);
        jdbcTemplate.update(deleteGenresQuery, id);
        jdbcTemplate.update(deleteDirectorsQuery, id);

        String deleteFilmQuery = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(deleteFilmQuery, id);
    }

    @Override
    public boolean existsById(int id) {
        String query = "SELECT COUNT(*) FROM films WHERE id=?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, id);
        return count != null && count > 0;
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

    public List<Film> getCommonFilms(int userId, int friendId) {
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NoSuchElementException("User with ID " + userId + " not found."));
        userStorage.getUserById(friendId)
                .orElseThrow(() -> new NoSuchElementException("User with ID " + friendId + " not found."));

        String query = """
                SELECT f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
                       r.id AS rating_id, r.name AS rating_name,
                       (SELECT COUNT(*) FROM likes l WHERE l.film_id = f.id) AS likes_count
                FROM films f
                JOIN likes l1 ON f.id = l1.film_id
                JOIN likes l2 ON f.id = l2.film_id AND l1.user_id != l2.user_id
                LEFT JOIN ratings r ON f.rating_id = r.id
                WHERE l1.user_id = ? AND l2.user_id = ?
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, r.id, r.name
                ORDER BY likes_count DESC;
                """;

        List<Film> films = jdbcTemplate.query(query, new FilmWithGenresExtractor(), userId, friendId);

        if (films.isEmpty()) {
            throw new NoSuchElementException("No common films found for users " + userId + " and " + friendId);
        }

        return films;
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

    @Override
    public List<Film> searchFilms(String query, String by) {
        if (by == null || by.trim().isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> allowedParams = Set.of("title", "director");

        Set<String> requestParams = Arrays.stream(by.split(","))
                .map(String::trim)
                .filter(param -> !param.isEmpty())
                .collect(Collectors.toSet());

        if (!allowedParams.containsAll(requestParams)) {
            throw new IllegalArgumentException("Invalid search parameter: " + by);
        }

        if (requestParams.isEmpty()) {
            return Collections.emptyList();
        }

        StringBuilder sql = new StringBuilder("""
                    SELECT DISTINCT f.id, f.name, f.description, f.release_date, f.duration,
                                    r.id AS rating_id, r.name AS rating_name
                    FROM films f
                    LEFT JOIN film_directors fd ON f.id = fd.film_id
                    LEFT JOIN directors d ON fd.director_id = d.id
                    LEFT JOIN ratings r ON f.rating_id = r.id
                    WHERE
                """);

        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();
        String searchPattern = "%" + query.toLowerCase() + "%";

        if (requestParams.contains("title")) {
            conditions.add("LOWER(f.name) LIKE LOWER(?)");
            params.add(searchPattern);
        }
        if (requestParams.contains("director")) {
            conditions.add("LOWER(d.name) LIKE LOWER(?)");
            params.add(searchPattern);
        }

        sql.append(String.join(" OR ", conditions));

        List<Film> films = jdbcTemplate.query(sql.toString(), filmRowMapper, params.toArray());

        Map<Integer, List<Genre>> filmsGenres = getFilmsGenres();
        Map<Integer, List<Director>> filmsDirectors = getFilmsDirectors();

        for (Film film : films) {
            film.getGenres().addAll(filmsGenres.getOrDefault(film.getId(), List.of()));
            film.getDirectors().addAll(filmsDirectors.getOrDefault(film.getId(), List.of()));
        }

        return films;
    }

    public List<Film> getLikesUser(int id) {
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
                JOIN likes l ON f.id = l.film_id
                LEFT JOIN ratings r ON f.rating_id = r.id
                WHERE l.user_id = ?;
                """;

        List<Film> films = findMany(query, id);

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