package ru.yandex.practicum.filmorate.controller;

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
    public ResponseEntity<?> postNewReview(@RequestBody Review review) {
        return new ResponseEntity<>(reviewService.postNewReview(review), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<?> updateReview(@RequestBody Review review) {
        return ResponseEntity.ok(reviewService.updateReview(review));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReviewById(@PathVariable int id) {
        reviewService.deleteReviewById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewById(@PathVariable int id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @GetMapping
    public ResponseEntity<?> getAllReviewsByFilmId(@RequestParam int filmId, @RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(reviewService.getAllReviewsByFilmId(filmId, count));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<?> addLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.addLike(id, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<?> addDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.addDislike(id, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<?> deleteLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.deleteLike(id, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<?> deleteDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.deleteDislike(id, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
