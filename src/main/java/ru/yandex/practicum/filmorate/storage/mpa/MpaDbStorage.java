package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
@Primary
public class MpaDbStorage extends BaseDbStorage<Mpa> {

    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper);
    }

    public Collection<Mpa> findAll() {
        String query = """
                SELECT *
                FROM ratings""";
        return findMany(query);
    }

    public Optional<Mpa> getById(long id) {
        String query = """
                SELECT *
                FROM   ratings
                WHERE  id = ?""";
        return findOne(query, id);
    }

    public boolean existsMpaById(int id) {
        String query = "SELECT COUNT(*) FROM ratings WHERE id=?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, id);
        return count != null && count > 0;
    }

}
