package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private int id;

    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Email must be valid")
    private String email;

    @NotEmpty(message = "Login cannot be empty")
    private String login;

    private String name;

    @PastOrPresent(message = "Birthday cannot be in the future")
    private LocalDate birthday;

    private Set<Integer> friends = new HashSet<>();
}

