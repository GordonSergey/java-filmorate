package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    private final ReviewDbStorage reviewDbStorage;

    public Review postNewReview(Review review) {
        return reviewDbStorage.postNewReview(review);
    }

    public Review updateReview(Review updateReview) {
        return reviewDbStorage.updateReview(updateReview);
    }

    public void deleteReviewById(int id) {
        reviewDbStorage.deleteReviewById(id);
    }

    public Review getReviewById(int id) {
        return reviewDbStorage.getReviewById(id);
    }

    public List<Review> getAllReviewsByFilmId(int filmId, int count) {
        return  reviewDbStorage.getAllReviewsByFilmId(filmId, count);
    }

    public void addLike(int reviewId, int userId) {
        try {
            deleteDislike(reviewId, userId);
        } catch (NoSuchElementException e) {}
            reviewDbStorage.addLike(reviewId, userId);
            if (!reviewDbStorage.updateUseful(reviewId, 1)) {
                reviewDbStorage.deleteLike(reviewId, userId);
            }
    }

    public void addDislike(int reviewId, int userId) {
        try {
            deleteLike(reviewId, userId);
        } catch (NoSuchElementException e) {}
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


}
