package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewRowMapper implements RowMapper<Review> {

    @Override
    public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Review.builder()
                .id(rs.getInt(1))
                .content(rs.getString(2))
                .isPositive(rs.getBoolean(3))
                .userId(rs.getInt(4))
                .filmId(rs.getInt(5))
                .useful(rs.getInt(6))
                .build();
    }
}
