package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    User addUser(User user);

    User updateUser(User user);

    Optional<User> getUserById(int id);

    List<User> getAllUsers();

    void addFriend(int userId, int friendId);

    void confirmedFriend(int userId, int friendId);

    Optional<String> getFriendshipStatus(int userId, int friendId);

    void removeFriend(int userId, int friendId);

    List<User> getFriends(int userId);

    boolean existsUserById(int id);

    boolean existsFriendByIds(int userId, int friendId);
}
