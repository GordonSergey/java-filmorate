package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final List<Film> films = new ArrayList<>();
    private int filmIdCounter = 1;
    private static final LocalDate CINEMA_START_DATE = LocalDate.of(1895, 12, 28);

    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
        return ResponseEntity.ok(films);
    }

    @PostMapping
    public ResponseEntity<Film> createFilm(@Valid @RequestBody Film film) {
        // Проверка даты выхода фильма
        if (film.getReleaseDate().isBefore(CINEMA_START_DATE)) {
            throw new ValidationException("Release date cannot be earlier than December 28, 1895.");
        }

        film.setId(filmIdCounter++);
        films.add(film);
        return new ResponseEntity<>(film, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<?> updateFilm(@Valid @RequestBody Film film) {
        // Проверка даты выхода фильма
        if (film.getReleaseDate().isBefore(CINEMA_START_DATE)) {
            throw new ValidationException("Release date cannot be earlier than December 28, 1895.");
        }

        for (Film existingFilm : films) {
            if (existingFilm.getId() == film.getId()) {
                existingFilm.setName(film.getName());
                existingFilm.setDescription(film.getDescription());
                existingFilm.setReleaseDate(film.getReleaseDate());
                existingFilm.setDuration(film.getDuration());
                return ResponseEntity.ok(existingFilm);
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Film with ID " + film.getId() + " not found."));
    }
}