package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

@WebMvcTest(FilmController.class)
public class FilmControllerTest {

    private Film validFilm;

    @BeforeEach
    void setUp() {
        validFilm = new Film();
        validFilm.setName("Valid Film");
        validFilm.setDescription("This is a valid description.");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);
    }

    @Test
    @DisplayName("Ошибка при пустом названии фильма")
    void addFilmWithEmptyNameThrowsValidationException() {
        validFilm.setName("");
        ValidationException exception = assertThrows(ValidationException.class, () -> new FilmController().addFilm(validFilm));
        assert (exception.getMessage().contains("Название фильма не может быть пустым."));
    }

    @Test
    @DisplayName("Ошибка при описании длиной более 200 символов")
    void addFilmWithLongDescriptionThrowsValidationException() {
        validFilm.setDescription("A".repeat(201));
        ValidationException exception = assertThrows(ValidationException.class, () -> new FilmController().addFilm(validFilm));
        assert (exception.getMessage().contains("Описание фильма не может превышать 200 символов."));
    }

    @Test
    @DisplayName("Ошибка при дате релиза раньше 28 декабря 1895 года")
    void addFilmWithInvalidReleaseDateThrowsValidationException() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 27));
        ValidationException exception = assertThrows(ValidationException.class, () -> new FilmController().addFilm(validFilm));
        assert (exception.getMessage().contains("Дата релиза не может быть раньше 28 декабря 1895 года."));
    }

    @Test
    @DisplayName("Ошибка при неположительной продолжительности фильма")
    void addFilmWithNegativeDurationThrowsValidationException() {
        validFilm.setDuration(-100);
        ValidationException exception = assertThrows(ValidationException.class, () -> new FilmController().addFilm(validFilm));
        assert (exception.getMessage().contains("Продолжительность фильма должна быть положительным числом."));
    }
}
