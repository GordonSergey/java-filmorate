package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class DirectorDbStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Director> directorRowMapper;

    public DirectorDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Director> directorRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.directorRowMapper = directorRowMapper;
    }

    public Director addDirector(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        int id = keyHolder.getKey().intValue();
        director.setId(id);
        return director;
    }

    public Director update(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE id = ?";
        int updatedRows = jdbcTemplate.update(sql, director.getName(), director.getId());

        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update director with ID: " + director.getId());
        }
        return director;
    }

    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM directors WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public List<Director> getAllDirectors() {
        String sql = "SELECT * FROM directors";
        return jdbcTemplate.query(sql, directorRowMapper);
    }

    public Optional<Director> getDirectorById(int id) {
        String sql = "SELECT * FROM directors WHERE id = ?";
        List<Director> directors = jdbcTemplate.query(sql, directorRowMapper, id);
        return directors.stream().findFirst();
    }

    public void deleteDirector(int id) {
        String sql = "DELETE FROM directors WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}