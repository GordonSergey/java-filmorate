package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.logging.Logger;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController (ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<?> postNewReview(@Valid @RequestBody Review review) {
        return new ResponseEntity<>(reviewService.postNewReview(review), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<?> updateReview(@RequestBody Review review) {
        return ResponseEntity.ok(reviewService.updateReview(review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReviewById(@PathVariable int id) {
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getReviewById(@PathVariable int id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @GetMapping("?filmId={filmId}&count={count}")
    public ResponseEntity<?> getAllReviewsByFilmId(@PathVariable int filmId, @PathVariable int count) {
        return null;
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<?> addLike(@PathVariable int id, @PathVariable int userId) {
        return null;
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<?> addDislike(@PathVariable int id, @PathVariable int userId) {
        return null;
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<?> deleteLike(@PathVariable int id, @PathVariable int userId) {
        return null;
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<?> deleteDislike(@PathVariable int id, @PathVariable int userId) {
        return null;
    }

}
