package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FilmValidationTest {

    private Validator validator;

    @BeforeEach
    public void initializeValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    public void shouldValidateCorrectFilm() {
        Film film = new Film();
        film.setName("Perfect Film");
        film.setDescription("Description within allowed length.");
        film.setReleaseDate(LocalDate.of(2000, 6, 15));
        film.setDuration(100);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty(), "A correctly configured film should pass validation.");
    }

    @Test
    public void shouldDetectEmptyNameAsInvalid() {
        Film film = new Film();
        film.setName(""); // Пустое название
        film.setDescription("Valid description.");
        film.setReleaseDate(LocalDate.of(2010, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "An empty name must fail validation.");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Name cannot be empty")),
                "Expected validation message for empty name.");
    }

    @Test
    public void shouldRejectDescriptionOverMaxLength() {
        Film film = new Film();
        film.setName("Another Film");
        film.setDescription("X".repeat(201)); // Описание длиной 201 символ
        film.setReleaseDate(LocalDate.of(1995, 12, 25));
        film.setDuration(150);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "A description exceeding 200 characters should fail validation.");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Description should not exceed 200 characters")),
                "Expected validation message for overly long description.");
    }

    @Test
    public void shouldInvalidateNegativeDuration() {
        Film film = new Film();
        film.setName("Short Film");
        film.setDescription("A short film with incorrect duration.");
        film.setReleaseDate(LocalDate.of(1985, 8, 1));
        film.setDuration(-50);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty(), "Negative duration should be flagged as invalid.");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Duration must be a positive number")),
                "Expected validation message for negative duration.");
    }
}