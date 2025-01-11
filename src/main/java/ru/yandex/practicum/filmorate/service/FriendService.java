package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
public class FriendService {

    private final UserStorage userStorage;

    public FriendService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(int userId, int friendId) {
        if (!isUserExist(userId) || !isUserExist(friendId)) {
            throw new NoSuchElementException("User not found");
        }
        Optional<String> friendshipStatusOne = userStorage.getFriendshipStatus(userId, friendId);
        Optional<String> friendshipStatusTwo = userStorage.getFriendshipStatus(friendId, userId);

        if (friendshipStatusOne.isEmpty() && friendshipStatusTwo.isEmpty()) {
            sendRequestForFriendship(userId, friendId);
        } else {
            friendshipStatusOne.ifPresent(s -> confirmedFriendship(userId, friendId, s));
            friendshipStatusTwo.ifPresent(s -> confirmedFriendship(friendId, userId, s));
        }
    }

    public void removeFriend(int userId, int friendId) {
        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(int userId) {
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        List<User> userFriends = getFriends(userId);
        List<User> friendFriends = getFriends(otherId);
        return userFriends.stream()
                .filter(friendFriends::contains)
                .toList();
    }

    public boolean isUserExist(int userId) {
        return userStorage.existsUserById(userId);
    }

    private void confirmedFriendship(int userId, int friendId, String status) {
        if (status.equals("CONFIRMED")) {
            throw new ValidationException("Already friends");
        }
        userStorage.confirmedFriend(userId, friendId);
    }

    private void sendRequestForFriendship(int userId, int friendId) {
        userStorage.addFriend(userId, friendId);
    }
}