package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Film {

    private int id;

    @NotEmpty(message = "Name cannot be empty")
    private String name;

    @NotEmpty(message = "Description cannot be empty")
    @Size(max = 200, message = "Description should not exceed 200 characters")
    private String description;

    @NotNull(message = "Release date cannot be null")
    @PastOrPresent(message = "Release date must be in the past or present")
    private LocalDate releaseDate;

    @Positive(message = "Duration must be a positive number")
    private int duration;
}