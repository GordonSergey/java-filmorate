package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Primary
public class GenreDbStorage extends BaseDbStorage<Genre> {

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    public List<Genre> findAll() {
        String query = """
                SELECT *
                FROM genres""";
        return findMany(query);
    }

    public List<Genre> getGenresByFilmId(long id) {
        String query = """
                SELECT 
                    g.id,
                    g.name
                FROM 
                    film_genres fg
                INNER JOIN genres g ON fg.genre_id = g.id
                WHERE 
                    fg.film_id = ?
                """;
        return findMany(query, id);
    }


    public Optional<Genre> getById(long id) {
        String query = """
                SELECT *
                FROM   genres
                WHERE  id = ?""";
        return findOne(query, id);
    }

    public boolean existsGenresByIds(List<Integer> ids) {
        String placeholders = ids.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String query = String.format("SELECT COUNT(*) FROM genres WHERE id IN (%s)", placeholders);
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, ids.toArray());
        return count != null && count == ids.size();
    }
}
