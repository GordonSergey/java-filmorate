package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.List;
import java.util.Optional;

@Repository
public class ReviewDbStorage extends BaseDbStorage<Review> {

    public ReviewDbStorage(JdbcTemplate jdbcTemplate, RowMapper<Review> mapper) {
        super(jdbcTemplate, mapper);
    }

    public Review postNewReview(Review review) {

            String postReviewQuery = """
                    INSERT INTO reviews (content, is_positive, user_id, film_id, useful) 
                    VALUES (?, ?, ?, ?, ?)
                    """;

            int reviewId = insert(postReviewQuery, review.getContent(), review.isPositive(),
                    review.getUserId(), review.getFilmId(), 0);
            review.setId(reviewId);

            return review;
    }

    public Review updateReview(Review updateReview) {

        String updateReviewQuery = """
                INSERT INTO reviews (content, is_positive, user_id, film_id) 
                VALUES (?, ?, ?, ?)
                """;

        update(updateReviewQuery, updateReview.getContent(), updateReview.isPositive(), updateReview.getUserId(),
                updateReview.getFilmId());

        return updateReview;
    }

    public void deleteReviewById(int id) {
        String deleteReviewQuery = """
                DELETE FROM clients
                WHERE id = ?
                """;

        delete(deleteReviewQuery, id);
    }

    public Review getReviewById(int id) {
        String getReviewQuery = """
                SELECT id, content, is_positive, user_id, film_id, useful 
                FROM reviews 
                """;

        Optional<Review> reviewOptional = findOne(getReviewQuery, id);

        if (reviewOptional.isEmpty()) {
            throw new IllegalArgumentException("not found");
        }

        return reviewOptional.get();
    }

    public List<Review> getAllReviewsByFilmId(int filmId, int count) {

        String getAllReviewsQuery = """
                SELECT id, content, is_positive, user_id, film_id, useful
                FROM reviews
                WHERE film_id = ?
                GROUP BY useful
                LIMIT ?
                """;

        List<Review> reviewList = findMany(getAllReviewsQuery, filmId, count);
        return  null;
    }

    public void addLike(int id, int userId) {
        String addLikeQuery = """
                INSERT INTO reviews 
                VALUES (?)
                WHERE id = ?
                """;


    }

    public void addDislike(int id, int userId) {}

    public void deleteLike(int id, int userId) {}

    public void deleteDislike(int id, int userId) {}
}
