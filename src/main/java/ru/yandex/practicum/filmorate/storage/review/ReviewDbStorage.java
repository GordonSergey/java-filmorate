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

    public ReviewDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Review> mapper) {
        super(jdbcTemplate, mapper);
    }

    public Review postNewReview(Review review) {
        checkId(review.getFilmId(), "films", "id");
        checkId(review.getUserId(), "users", "id");
        String postReviewQuery = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(postReviewQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, review.getUserId());
            ps.setInt(4, review.getFilmId());
            ps.setInt(5, 0);
            return ps;
        }, keyHolder);
        review.setReviewId(keyHolder.getKey().intValue());
        return review;
    }

    public Review updateReview(Review updateReview) {
        checkId(updateReview.getReviewId(), "reviews", "id");

        String updateReviewQuery = "UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?";

        update(updateReviewQuery, updateReview.getContent(), updateReview.getIsPositive(), updateReview.getReviewId());

        return getReviewById(updateReview.getReviewId());
    }

    public void deleteReviewById(int id) {
        checkId(id, "reviews", "id");

        String deleteLikesReviewQuery = """
                DELETE FROM review_likes
                WHERE review_id = ?
                """;
        delete(deleteLikesReviewQuery, id);

        String deleteDislikeReviewQuery = """
                DELETE FROM review_dislikes
                WHERE review_id = ?
                """;
        delete(deleteDislikeReviewQuery, id);

        String deleteReviewQuery = """
                DELETE FROM reviews
                WHERE id = ?
                """;
        delete(deleteReviewQuery, id);
    }

    public Review getReviewById(int id) {

        checkId(id, "reviews", "id");

        String getReviewQuery = "SELECT id, content, is_positive, user_id, film_id, useful FROM reviews WHERE id = ?";

        Optional<Review> reviewOptional = findOne(getReviewQuery, id);

        if (reviewOptional.isEmpty()) {
            throw new IllegalArgumentException("not found");
        }

        return reviewOptional.get();
    }

    public List<Review> getAllReviewsByFilmId(int filmId, int count) {
        checkId(filmId, "films", "id");

        String getAllReviewsQuery = """
                    SELECT id, content, is_positive, user_id, film_id, useful
                    FROM reviews
                    WHERE film_id = ?
                    ORDER BY useful DESC
                    LIMIT ?
                """;

        return findMany(getAllReviewsQuery, filmId, count);
    }


    public List<Review> getAllReviews(int count) {
        String getAllReviewsQuery = """
                    SELECT id, content, is_positive, user_id, film_id, useful
                    FROM reviews
                    ORDER BY useful DESC
                    LIMIT ?
                """;

        return findMany(getAllReviewsQuery, count);
    }


    public void addLike(int reviewId, int userId) {

        checkId(reviewId, "reviews", "id");
        checkId(userId, "users", "id");

        String addLikeQuery = """
                INSERT INTO review_likes (user_id, review_id)
                VALUES (?, ?)
                """;
        try {
            jdbcTemplate.update(addLikeQuery, userId, reviewId);
        } catch (DataAccessException e) {
            throw new DuplicateKeyException("Such a like already exists");
        }
    }

    public void addDislike(int reviewId, int userId) {
        checkId(reviewId, "reviews", "id");
        checkId(userId, "users", "id");

        String addDislikeQuery = """
                INSERT INTO review_dislikes (user_id, review_id)
                VALUES (?, ?)
                """;

        try {
            jdbcTemplate.update(addDislikeQuery, userId, reviewId);
        } catch (DataAccessException e) {
            throw new DuplicateKeyException("Such a dislike already exists");
        }
    }

    public void deleteLike(int reviewId, int userId) {

        checkId(reviewId, "reviews", "id");
        checkId(userId, "users", "id");

        String deleteLikeQuery = """
                DELETE FROM review_likes
                WHERE user_id = ? AND review_id = ?
                """;

        if (!delete(deleteLikeQuery, userId, reviewId)) {
            throw new NoSuchElementException("Such a like does not exist.");
        }
    }

    public void deleteDislike(int reviewId, int userId) {

        checkId(reviewId, "reviews", "id");
        checkId(userId, "users", "id");

        String deleteLikeQuery = """
                DELETE FROM review_dislikes
                WHERE user_id = ? AND review_id = ?
                """;

        if (!delete(deleteLikeQuery, userId, reviewId)) {
            throw new NoSuchElementException("Such a dislike does not exist.");
        }
    }

    public boolean updateUseful(int reviewId, int value) {
        String updateUsefulQuery = "UPDATE reviews SET useful = useful + ? WHERE id = ?";
        return update(updateUsefulQuery, value, reviewId) > 0;
    }

    public void checkId(int id, String tableName, String columnName) {
        String checkQuery = String.format("SELECT EXISTS(SELECT 1 FROM %s WHERE %s = ?)", tableName, columnName);
        int i = jdbcTemplate.queryForObject(checkQuery, Integer.class, id);
        if (i == 0) {
            throw new NoSuchElementException("Such an object does not exist");
        }
    }
}