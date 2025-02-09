package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.*;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        return ResponseEntity.status(HttpStatus.CREATED).body(filmService.addFilm(film));
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) {
        return ResponseEntity.ok(filmService.updateFilm(film));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFilm(@PathVariable int id) {
        filmService.deleteFilm(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilmById(@PathVariable int id) {
        return ResponseEntity.ok(filmService.getFilmById(id));
    }

    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
        List<Film> films = filmService.getAllFilms();
        return ResponseEntity.ok(films);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable int filmId, @PathVariable int userId) {
        filmService.addLike(filmId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable int id, @PathVariable int userId) {
        filmService.removeLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/common")
    public ResponseEntity<List<Film>> getCommonFilms(
            @RequestParam int userId,
            @RequestParam int friendId) {
        List<Film> commonFilms = filmService.getCommonFilms(userId, friendId);
        return ResponseEntity.ok(commonFilms);
    }

    @GetMapping("popular")
    public ResponseEntity<Collection<Film>> getPopularFilmsByParam(@RequestParam(defaultValue = "0") int count,
                                                                   @RequestParam(defaultValue = "0") Long genreId,
                                                                   @RequestParam(defaultValue = "0") int year) {
        Collection<Film> popularFilms = filmService.getPopularFilms(count, genreId, year);
        return ResponseEntity.ok(popularFilms);
    }

    @GetMapping("/director/{directorId}")
    public ResponseEntity<List<Film>> getFilmsByDirector(@PathVariable int directorId, @RequestParam String sortBy) {
        return ResponseEntity.ok(filmService.getFilmsByDirector(directorId, sortBy));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Film>> searchFilms(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String by) {
        return ResponseEntity.ok(filmService.searchFilms(query, by));
    }
}