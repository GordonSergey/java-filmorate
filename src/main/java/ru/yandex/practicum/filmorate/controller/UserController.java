package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FriendService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserService userService;
    private final FriendService friendService;

    public UserController(UserService userService, FriendService friendService) {
        this.userService = userService;
        this.friendService = friendService;
    }

    @PostMapping
    public ResponseEntity<?> addUser(@RequestBody @Valid User user) {
        try {
            User createdUser = userService.addUser(user);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        } catch (ValidationException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            if (users.isEmpty()) {
                log.info("No users found.");
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No users available.");
            }
            log.info("Returning all users.");
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error retrieving users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "An unexpected error occurred."));
        }
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@Valid @RequestBody User user) {
        log.info("Updating user: {}", user);
        try {
            User updatedUser = userService.updateUser(user);
            log.info("User updated successfully: {}", updatedUser);
            return ResponseEntity.ok(updatedUser);
        } catch (NoSuchElementException ex) {
            log.warn("Error updating user: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            log.error("Validation error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error while updating user", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Unexpected error occurred"));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable @Positive int userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body((User) Map.of("error", "User not found")));
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<?> addFriend(@PathVariable @Positive int userId, @PathVariable @Positive int friendId) {
        if (!friendService.isUserExist(userId) || !friendService.isUserExist(friendId)) {
            log.warn("User or friend not found. userId={}, friendId={}", userId, friendId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User or friend not found"));
        }
        friendService.addFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public ResponseEntity<?> removeFriend(@PathVariable @Positive int userId, @PathVariable @Positive int friendId) {
        if (!friendService.isUserExist(userId) || !friendService.isUserExist(friendId)) {
            log.warn("User or friend not found. userId={}, friendId={}", userId, friendId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User or friend not found"));
        }
        friendService.removeFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/friends")
    public ResponseEntity<?> getFriends(@PathVariable @Positive int userId) {
        if (!friendService.isUserExist(userId)) {
            log.warn("User not found. userId={}", userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
        List<User> friends = friendService.getFriends(userId);
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public ResponseEntity<?> getCommonFriends(@PathVariable @Positive int userId, @PathVariable @Positive int otherId) {
        if (!friendService.isUserExist(userId) || !friendService.isUserExist(otherId)) {
            log.warn("One or both users not found. userId={}, otherId={}", userId, otherId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "One or both users not found"));
        }
        List<User> commonFriends = friendService.getCommonFriends(userId, otherId);
        return ResponseEntity.ok(commonFriends);
    }
}