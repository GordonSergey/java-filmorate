package ru.yandex.practicum.filmorate.controller;

import ru.yandex.practicum.filmorate.model.Film;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final List<Film> films = new ArrayList<>();
    private int filmIdCounter = 1;

    // Добавление фильма
    @PostMapping
    public ResponseEntity<Film> createFilm(@RequestBody @Valid Film film, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder("Validation failed: ");
            for (FieldError error : bindingResult.getFieldErrors()) {
                errorMessage.append(error.getField())
                        .append(" - ")
                        .append(error.getDefaultMessage())
                        .append("; ");
            }
            log.error(errorMessage.toString());

            // Возвращаем ResponseEntity с объектом Film и сообщением об ошибке
            Film errorFilm = new Film();
            errorFilm.setDescription(errorMessage.toString());  // Записываем сообщение об ошибке в поле description
            return new ResponseEntity<>(errorFilm, HttpStatus.BAD_REQUEST);
        }

        log.info("Creating new film: {}", film.getName());
        film.setId(filmIdCounter++);
        films.add(film);
        return new ResponseEntity<>(film, HttpStatus.CREATED);
    }

    // Обновление фильма
    @PutMapping
    public ResponseEntity<Film> updateFilm(@RequestBody @Valid Film film, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder("Validation failed: ");
            for (FieldError error : bindingResult.getFieldErrors()) {
                errorMessage.append(error.getField())
                        .append(" - ")
                        .append(error.getDefaultMessage())
                        .append("; ");
            }
            log.error(errorMessage.toString());

            // Возвращаем ResponseEntity с объектом Film и сообщением об ошибке
            Film errorFilm = new Film();
            errorFilm.setDescription(errorMessage.toString());  // Записываем сообщение об ошибке в поле description
            return new ResponseEntity<>(errorFilm, HttpStatus.BAD_REQUEST);
        }

        for (Film existingFilm : films) {
            if (existingFilm.getId() == film.getId()) {
                log.info("Updating film with ID: {}", film.getId());
                existingFilm.setName(film.getName());
                existingFilm.setDescription(film.getDescription());
                existingFilm.setReleaseDate(film.getReleaseDate());
                existingFilm.setDuration(film.getDuration());
                return ResponseEntity.ok(existingFilm);
            }
        }
        log.error("Film with ID {} not found for update", film.getId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // Получение всех фильмов
    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
        log.info("Fetching all films");
        return ResponseEntity.ok(films);
    }

    // Обработка ошибок валидации
    @ExceptionHandler(javax.validation.ValidationException.class)
    public ResponseEntity<Film> handleValidationException(javax.validation.ValidationException ex) {
        log.error("Validation error: {}", ex.getMessage());

        // Возвращаем ResponseEntity с объектом Film и сообщением об ошибке
        Film errorFilm = new Film();
        errorFilm.setDescription("Validation error: " + ex.getMessage());  // Записываем сообщение об ошибке в поле description
        return new ResponseEntity<>(errorFilm, HttpStatus.BAD_REQUEST);
    }
}