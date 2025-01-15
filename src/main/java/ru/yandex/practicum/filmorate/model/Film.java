package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Film implements Comparable<Film> {

    private int id;

    @NotEmpty(message = "Film name cannot be empty")
    private String name;

    @NotEmpty(message = "Description cannot be empty")
    @Size(max = 200, message = "Description should not exceed 200 characters")
    private String description;

    @NotNull(message = "Release date cannot be null")
    @PastOrPresent(message = "Release date must be in the past or present")
    private LocalDate releaseDate;

    @Positive(message = "Duration must be a positive number")
    private int duration;

    @NotNull(message = "Genres cannot be null")
    private List<Genre> genres = new ArrayList<>();

    private List<Integer> likes = new ArrayList<>();

    @NotNull(message = "MPA rating cannot be null")
    private Mpa mpa;

    @Override
    public int compareTo(Film o) {
        int likeComparison = Integer.compare(o.getLikes().size(), this.likes.size());
        if (likeComparison != 0) {
            return likeComparison;
        }
        return Long.compare(this.getId(), o.getId());
    }
}