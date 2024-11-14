package ru.yandex.practicum.filmorate;

import ru.yandex.practicum.filmorate.model.Film;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilmValidationTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void shouldPassValidationForValidFilm() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("Description within 200 characters");
        film.setReleaseDate(LocalDate.of(1994, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Valid film should pass validation");
    }

    @Test
    public void shouldFailValidationWhenNameIsEmpty() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Description within 200 characters");
        film.setReleaseDate(LocalDate.of(1994, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Empty name should fail validation");
    }

    @Test
    public void shouldFailValidationWhenDescriptionExceedsMaxLength() {
        Film film = new Film();
        film.setName("Valid Film");
        film.setDescription("D".repeat(201)); // 201 characters long
        film.setReleaseDate(LocalDate.of(1994, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Description exceeding 200 characters should fail validation");
    }

    @Test
    public void shouldFailValidationWhenReleaseDateIsBeforeCinema() {
        Film film = new Film();
        film.setName("Historic Film");
        film.setDescription("A film with a release date before cinema.");
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // One day before the allowed date
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Release date before 1895-12-28 should fail validation");
    }

    @Test
    public void shouldFailValidationWhenDurationIsNegative() {
        Film film = new Film();
        film.setName("Negative Duration Film");
        film.setDescription("A film with invalid negative duration.");
        film.setReleaseDate(LocalDate.of(1994, 1, 1));
        film.setDuration(-90);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Negative duration should fail validation");
    }
}