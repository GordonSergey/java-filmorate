package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Component
public class LikesRowMapper implements RowMapper<Map<Long, Integer>> {

    @Override
    public Map<Long, Integer> mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Map.of(rs.getLong("film_id"), rs.getInt("count"));
    }
}
