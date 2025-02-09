package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FriendService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Validated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FriendService friendService;
    private final UserStorage userStorage;

    @PostMapping
    public ResponseEntity<User> addUser(@RequestBody @Valid User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addUser(user));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable @Positive int userId) {
        return ResponseEntity.ok(userService.getUserById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found")));
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<Void> addFriend(@PathVariable int userId, @PathVariable int friendId) {
        friendService.addFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable @Positive int userId, @PathVariable @Positive int friendId) {
        friendService.removeFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/friends")
    public ResponseEntity<List<User>> getFriends(@PathVariable @Positive int userId) {
        if (!friendService.isUserExist(userId)) {
            throw new NoSuchElementException("User not found");
        }
        return ResponseEntity.ok(friendService.getFriends(userId));
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public ResponseEntity<List<User>> getCommonFriends(@PathVariable @Positive int userId, @PathVariable @Positive int otherId) {
        if (!friendService.isUserExist(userId) || !friendService.isUserExist(otherId)) {
            throw new NoSuchElementException("One or both users not found");
        }
        return ResponseEntity.ok(friendService.getCommonFriends(userId, otherId));
    }

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<List<Film>> getRecommendations(@PathVariable @Positive int id) {
        return ResponseEntity.ok(userService.findRecommendedFilms(id));
    }

    @GetMapping("/{userId}/feed")
    public ResponseEntity<List<Event>> getUserFeed(@PathVariable int userId) {
        if (!userStorage.existsUserById(userId)) {
            throw new NoSuchElementException("User not found");
        }
        return ResponseEntity.ok(userStorage.getUserFeed(userId));
    }
}