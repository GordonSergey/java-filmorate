package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer, Set<Integer>> friends = new HashMap<>();
    private int userIdCounter = 1;

    @Override
    public User addUser(User user) {
        user.setId(userIdCounter++);
        users.put(user.getId(), user);
        friends.put(user.getId(), new HashSet<>());
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NoSuchElementException("User with ID " + user.getId() + " not found.");
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> getUserById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void addFriend(int userId, int friendId) {
        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        Set<Integer> userFriends = friends.get(userId);
        Set<Integer> friendFriends = friends.get(friendId);

        if (userFriends != null) {
            userFriends.remove(friendId);
        }
        if (friendFriends != null) {
            friendFriends.remove(userId);
        }
    }

    @Override
    public List<User> getFriends(int userId) {
        Set<Integer> friendIds = friends.getOrDefault(userId, Collections.emptySet());
        List<User> friendList = new ArrayList<>();
        for (int friendId : friendIds) {
            friendList.add(users.get(friendId));
        }
        return friendList;
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        Set<Integer> userFriends = friends.getOrDefault(userId, Collections.emptySet());
        Set<Integer> otherFriends = friends.getOrDefault(otherId, Collections.emptySet());
        userFriends.retainAll(otherFriends);
        List<User> commonFriends = new ArrayList<>();
        for (int friendId : userFriends) {
            commonFriends.add(users.get(friendId));
        }
        return commonFriends;
    }
}