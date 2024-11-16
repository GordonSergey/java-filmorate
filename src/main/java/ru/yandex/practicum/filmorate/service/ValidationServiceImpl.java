package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@SuppressWarnings("unused")
@Service
public class ValidationServiceImpl implements ValidationService {
    private static final LocalDate CINEMA_START_DATE = LocalDate.of(1895, 12, 28);

    @Override
    public void validate(Film film) {
        if (film.getName() == null || film.getName().isEmpty()) {
            throw new ValidationException("Film name cannot be empty");
        }
        if (film.getDescription() == null || film.getDescription().isEmpty()) {
            throw new ValidationException("Film description cannot be empty");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Film description cannot exceed 200 characters");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(CINEMA_START_DATE)) {
            throw new ValidationException("Film release date cannot be before 28-12-1895");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Film duration must be a positive number");
        }
    }

    @Override
    public void validate(User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new ValidationException("User email cannot be empty");
        }
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("User email must be valid");
        }
        if (user.getLogin() == null || user.getLogin().isEmpty()) {
            throw new ValidationException("User login cannot be empty");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("User birthday cannot be in the future");
        }
    }
}