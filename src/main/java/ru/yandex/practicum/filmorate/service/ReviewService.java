package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewDbStorage;

import java.util.List;

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

    public void addLike(int id, int userId) {}

    public void addDislike(int id, int userId) {}

    public void deleteLike(int id, int userId) {}

    public void deleteDislike(int id, int userId) {}

}
