package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<Review> postNewReview(@Valid @RequestBody Review review) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.postNewReview(review));
    }

    @PutMapping
    public ResponseEntity<?> updateReview(@RequestBody Review review) {
        return ResponseEntity.ok(reviewService.updateReview(review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReviewById(@PathVariable int id) {
        reviewService.deleteReviewById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewById(@PathVariable int id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @GetMapping
    public ResponseEntity<?> getAllReviewsByFilmId(@RequestParam(required = false) Integer filmId,
                                                   @RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(reviewService.getAllReviewsByFilmId(filmId, count));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.addLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> addDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.addDislike(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> deleteLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.deleteLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Void> deleteDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.deleteDislike(id, userId);
        return ResponseEntity.ok().build();
    }
}