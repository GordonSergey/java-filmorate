package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;

@Data
public class User {
    private int id;

    @NotEmpty(message = "Email cannot be empty")
    @Email(message = "Email should be valid")
    private String email;

    @NotEmpty(message = "Login cannot be empty")
    private String login;

    private String name;

    @PastOrPresent(message = "Birthday cannot be in the future")
    private LocalDate birthday;
}