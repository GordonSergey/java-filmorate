package ru.yandex.practicum.filmorate.storage.like;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.Map;

@Slf4j
@Repository
@Primary
public class LikeDbStorage extends BaseDbStorage<Map<Long, Integer>> {

    public LikeDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Map<Long, Integer>> mapper) {
        super(jdbcTemplate, mapper);
    }

    public boolean existsLike(int filmId, int userId) {
        String query = "SELECT COUNT(*) FROM likes WHERE user_id = ? AND film_id = ?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, userId, filmId);
        return count != null && count > 0;
    }

}
