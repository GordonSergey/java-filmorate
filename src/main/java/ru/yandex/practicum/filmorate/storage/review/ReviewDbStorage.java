package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

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

        int reviewId = insert(postReviewQuery, review.getContent(), review.getIsPositive(),
                review.getUserId(), review.getFilmId(), 0);
        review.setReviewId(reviewId);

        return review;
    }

    public Review updateReview(Review updateReview) {

        checkId(updateReview.getReviewId(), "reviews", "id");
        checkId(updateReview.getUserId(), "users", "id");
        checkId(updateReview.getFilmId(), "films", "id");

        String updateReviewQuery = "UPDATE reviews SET content = ?, is_positive = ?, user_id = ?, film_id = ? WHERE id = ?";

        update(updateReviewQuery, updateReview.getContent(), updateReview.getIsPositive(), updateReview.getUserId(),
                updateReview.getFilmId(), updateReview.getReviewId());

        return updateReview;
    }

    public void deleteReviewById(int id) {
        checkId(id, "reviews", "id");
        String deleteReviewQuery = """
                DELETE FROM reviews
                WHERE id = ?
                """;
        delete(deleteReviewQuery, id);

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
                    GROUP BY useful
                    ORDER BY useful DESC
                    LIMIT ?
                    """;

        return findMany(getAllReviewsQuery, filmId, count);
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
            throw new DuplicateKeyException("Такой дизлайк уже существует");
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
            throw new DuplicateKeyException("Такой дизлайк уже существует");
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
            throw new NoSuchElementException("Такого лайка не существует.");
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
            throw new NoSuchElementException("Такого дизлайка не существует.");
        }
    }

    public boolean updateUseful(int reviewId, int value) {
        String updateUsefulQuery = "UPDATE reviews SET useful = useful + ? WHERE id = ?";
        if (update(updateUsefulQuery, value, reviewId) > 0) {
            return true;
        }
        return false;
    }

    public void checkId(int id, String tableName, String columnName) {
        String checkQuery = String.format("SELECT EXISTS(SELECT 1 FROM %s WHERE %s = ?)", tableName, columnName);
        int i = jdbcTemplate.queryForObject(checkQuery, Integer.class, id);
        if (i == 0) {
            throw new NoSuchElementException("Такого объекта не существует");
        }
    }

}
