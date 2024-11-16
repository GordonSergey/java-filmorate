package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.DataValidation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/films")
public class FilmController {

    private final DataValidation validationService;
    private final List<Film> films = new ArrayList<>();
    private int filmIdCounter = 1;

    @Autowired
    public FilmController(DataValidation validationService) {
        this.validationService = validationService;
    }

    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
        return ResponseEntity.ok(films);
    }

    @PostMapping
    public ResponseEntity<Film> createFilm(@RequestBody Film film) {
        validationService.validate(film);

        film.setId(filmIdCounter++);
        films.add(film);

        return new ResponseEntity<>(film, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<?> updateFilm(@RequestBody Film film) {
        validationService.validate(film);

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