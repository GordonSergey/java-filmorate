package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewDbStorage reviewDbStorage;
    private final UserStorage userStorage;

    public Review postNewReview(Review review) {
        Review createdReview = reviewDbStorage.postNewReview(review);
        userStorage.logEvent(review.getUserId(), createdReview.getReviewId(), "REVIEW", "ADD");
        return createdReview;
    }

    public Review updateReview(Review review) {
        Review updatedReview = reviewDbStorage.updateReview(review);
        userStorage.logEvent(review.getUserId(), updatedReview.getReviewId(), "REVIEW", "UPDATE");
        return updatedReview;
    }

    public void deleteReviewById(int id) {
        Review review = getReviewById(id);
        reviewDbStorage.deleteReviewById(id);
        userStorage.logEvent(review.getUserId(), review.getReviewId(), "REVIEW", "REMOVE");
    }

    public Review getReviewById(int id) {
        return reviewDbStorage.getReviewById(id);
    }

    public List<Review> getAllReviewsByFilmId(int filmId, int count) {
        return reviewDbStorage.getAllReviewsByFilmId(filmId, count);
    }

    public void addLike(int reviewId, int userId) {
        try {
            deleteDislike(reviewId, userId);
        } catch (NoSuchElementException e) {
            System.out.println(" ");
        }

        reviewDbStorage.addLike(reviewId, userId);
        if (!reviewDbStorage.updateUseful(reviewId, 1)) {
            reviewDbStorage.deleteLike(reviewId, userId);

        }
    }

    public void addDislike(int reviewId, int userId) {
        try {
            deleteLike(reviewId, userId);
        } catch (NoSuchElementException e) {
            System.out.println(" ");
        }
        reviewDbStorage.addDislike(reviewId, userId);
        if (!reviewDbStorage.updateUseful(reviewId, -1)) {
            reviewDbStorage.deleteDislike(reviewId, userId);
        }
    }

    public void deleteLike(int reviewId, int userId) {
        reviewDbStorage.deleteLike(reviewId, userId);
        reviewDbStorage.updateUseful(reviewId, -1);
    }

    public void deleteDislike(int reviewId, int userId) {
        reviewDbStorage.deleteDislike(reviewId, userId);
        reviewDbStorage.updateUseful(reviewId, 1);
    }

    private void validateReview(Review review) {
        if (review.getContent() == null || review.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Review content cannot be empty.");
        }
        if (review.getIsPositive() == null) {
            throw new IllegalArgumentException("isPositive must be not null");
        }
        if (review.getUserId() == null) {
            throw new IllegalArgumentException("UserId must be not null");
        }
        if (review.getFilmId() == 0) {
            throw new IllegalArgumentException("FilmId must be not null");
        }
    }
}