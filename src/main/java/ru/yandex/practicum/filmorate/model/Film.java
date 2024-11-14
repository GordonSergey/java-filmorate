package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Data
public class Film {
    private int id;

    @NotEmpty(message = "Name cannot be empty")
    private String name;

    @NotEmpty(message = "Description cannot be empty")
    @Size(max = 200, message = "Description should not exceed 200 characters")
    private String description;

    @PastOrPresent(message = "Release date should not be before 28-12-1895")
    private LocalDate releaseDate;

    @Positive(message = "Duration must be a positive number")
    private int duration;

    private String errorMessage;  // Опциональное поле для сообщений об ошибках
}
