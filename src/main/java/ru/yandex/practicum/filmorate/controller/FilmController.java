package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/films")
public class FilmController {

    private static final Logger log = LoggerFactory.getLogger(FilmController.class);
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public ResponseEntity<?> addFilm(@Valid @RequestBody Film film) {
        try {
            Film createdFilm = filmService.addFilm(film);
            return new ResponseEntity<>(createdFilm, HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Validation error", ex.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<?> updateFilm(@Valid @RequestBody Film film) {
        try {
            Film updatedFilm = filmService.updateFilm(film);
            return ResponseEntity.ok(updatedFilm);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Validation error", ex.getMessage()));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Not found", ex.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFilmById(@PathVariable int id) {
        try {
            Film film = filmService.getFilmById(id);
            return ResponseEntity.ok(film);
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Not found", ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
        List<Film> films = filmService.getAllFilms();
        if (films.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(films);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public ResponseEntity<?> addLike(@PathVariable int filmId, @PathVariable int userId) {
        log.info("Adding like to film ID: {} by user ID: {}", filmId, userId);
        try {
            filmService.addLike(filmId, userId);
            log.info("Like added successfully to film ID: {} by user ID: {}", filmId, userId);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException ex) {
            log.warn("Error adding like: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error while adding like", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Unexpected error occurred."));
        }
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<?> removeLike(@PathVariable int id, @PathVariable int userId) {
        try {
            filmService.removeLike(id, userId);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Not found", ex.getMessage()));
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        List<Film> popularFilms = filmService.getPopularFilms(count);
        return ResponseEntity.ok(popularFilms);
    }

    static class ErrorResponse {
        private final String error;
        private final String message;

        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }

        public String getError() {
            return error;
        }

        public String getMessage() {
            return message;
        }
    }
}