package ru.yandex.practicum.filmorate.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Review {
    private int id;
    @NotNull
    @NotBlank
    private String content;
    @NotNull
    private boolean isPositive;
    @NotNull
    private int userId;
    @NotNull
    private int filmId;
    @Builder.Default
    private int useful = 0;
}
