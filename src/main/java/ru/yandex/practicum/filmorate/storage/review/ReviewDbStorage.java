package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Repository
public class ReviewDbStorage extends BaseDbStorage<Review> {

    private static final String INSERT_REVIEW_QUERY = """
            INSERT INTO reviews (content, is_positive, user_id, film_id, useful)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_REVIEW_QUERY = """
            UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?
            """;

    private static final String DELETE_LIKES_QUERY = """
            DELETE FROM review_likes WHERE review_id = ?
            """;

    private static final String DELETE_DISLIKES_QUERY = """
            DELETE FROM review_dislikes WHERE review_id = ?
            """;

    private static final String DELETE_REVIEW_QUERY = """
            DELETE FROM reviews WHERE id = ?
            """;

    private static final String SELECT_REVIEW_BY_ID_QUERY = """
            SELECT id, content, is_positive, user_id, film_id, useful FROM reviews WHERE id = ?
            """;

    private static final String SELECT_REVIEWS_BY_FILM_QUERY = """
            SELECT id, content, is_positive, user_id, film_id, useful
            FROM reviews
            WHERE film_id = ?
            ORDER BY useful DESC
            LIMIT ?
            """;

    private static final String SELECT_ALL_REVIEWS_QUERY = """
            SELECT id, content, is_positive, user_id, film_id, useful
            FROM reviews
            ORDER BY useful DESC
            LIMIT ?
            """;

    private static final String INSERT_LIKE_QUERY = """
            INSERT INTO review_likes (user_id, review_id)
            VALUES (?, ?)
            """;

    private static final String INSERT_DISLIKE_QUERY = """
            INSERT INTO review_dislikes (user_id, review_id)
            VALUES (?, ?)
            """;

    private static final String DELETE_LIKE_QUERY = """
            DELETE FROM review_likes WHERE user_id = ? AND review_id = ?
            """;

    private static final String DELETE_DISLIKE_QUERY = """
            DELETE FROM review_dislikes WHERE user_id = ? AND review_id = ?
            """;

    private static final String UPDATE_USEFUL_QUERY = """
            UPDATE reviews SET useful = useful + ? WHERE id = ?
            """;

    private static final String CHECK_ID_EXISTS_QUERY = """
            SELECT EXISTS(SELECT 1 FROM %s WHERE %s = ?)
            """;

    public ReviewDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Review> mapper) {
        super(jdbcTemplate, mapper);
    }

    public Review postNewReview(Review review) {
        if (review == null) {
            throw new IllegalArgumentException("Review cannot be null");
        }
        if (review.getUserId() == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (review.getFilmId() == null) {
            throw new IllegalArgumentException("Film ID cannot be null");
        }
        if (review.getIsPositive() == null) {
            throw new IllegalArgumentException("Review positivity flag cannot be null");
        }

        int userId = review.getUserId();
        int filmId = review.getFilmId();
        boolean isPositive = review.getIsPositive();

        checkId(filmId, "films", "id");
        checkId(userId, "users", "id");

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_REVIEW_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, isPositive);
            ps.setInt(3, userId);
            ps.setInt(4, filmId);
            ps.setInt(5, 0);
            return ps;
        }, keyHolder);

        review.setReviewId(Optional.ofNullable(keyHolder.getKey())
                .map(Number::intValue)
                .orElseThrow(() -> new RuntimeException("Failed to retrieve generated review ID")));

        return review;
    }

    public Review updateReview(Review updateReview) {
        checkId(updateReview.getReviewId(), "reviews", "id");
        update(UPDATE_REVIEW_QUERY, updateReview.getContent(), updateReview.getIsPositive(), updateReview.getReviewId());
        return getReviewById(updateReview.getReviewId());
    }

    public void deleteReviewById(int id) {
        checkId(id, "reviews", "id");
        delete(DELETE_LIKES_QUERY, id);
        delete(DELETE_DISLIKES_QUERY, id);
        delete(DELETE_REVIEW_QUERY, id);
    }

    public Review getReviewById(int id) {
        checkId(id, "reviews", "id");
        return findOne(SELECT_REVIEW_BY_ID_QUERY, id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
    }

    public List<Review> getAllReviewsByFilmId(int filmId, int count) {
        checkId(filmId, "films", "id");
        return findMany(SELECT_REVIEWS_BY_FILM_QUERY, filmId, count);
    }

    public List<Review> getAllReviews(int count) {
        return findMany(SELECT_ALL_REVIEWS_QUERY, count);
    }

    public void addLike(int reviewId, int userId) {
        checkId(reviewId, "reviews", "id");
        checkId(userId, "users", "id");

        try {
            jdbcTemplate.update(INSERT_LIKE_QUERY, userId, reviewId);
        } catch (DataAccessException e) {
            throw new DuplicateKeyException("Such a like already exists");
        }
    }

    public void addDislike(int reviewId, int userId) {
        checkId(reviewId, "reviews", "id");
        checkId(userId, "users", "id");

        try {
            jdbcTemplate.update(INSERT_DISLIKE_QUERY, userId, reviewId);
        } catch (DataAccessException e) {
            throw new DuplicateKeyException("Such a dislike already exists");
        }
    }

    public void deleteLike(int reviewId, int userId) {
        checkId(reviewId, "reviews", "id");
        checkId(userId, "users", "id");

        if (!delete(DELETE_LIKE_QUERY, userId, reviewId)) {
            throw new NoSuchElementException("Such a like does not exist.");
        }
    }

    public void deleteDislike(int reviewId, int userId) {
        checkId(reviewId, "reviews", "id");
        checkId(userId, "users", "id");

        if (!delete(DELETE_DISLIKE_QUERY, userId, reviewId)) {
            throw new NoSuchElementException("Such a dislike does not exist.");
        }
    }

    public boolean updateUseful(int reviewId, int value) {
        return update(UPDATE_USEFUL_QUERY, value, reviewId) > 0;
    }

    public void checkId(int id, String tableName, String columnName) {
        String checkQuery = String.format(CHECK_ID_EXISTS_QUERY, tableName, columnName);
        int exists = jdbcTemplate.queryForObject(checkQuery, Integer.class, id);
        if (exists == 0) {
            throw new NoSuchElementException("Such an object does not exist");
        }
    }
}