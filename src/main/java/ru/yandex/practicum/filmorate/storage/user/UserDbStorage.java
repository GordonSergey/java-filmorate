package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.EventRowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseDbStorage;

import java.util.*;

@Slf4j
@Repository
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {

    public UserDbStorage(JdbcTemplate jdbcTemplate, RowMapper<User> mapper) {
        super(jdbcTemplate, mapper);
    }

    @Override
    public User addUser(User user) {
        String query = """
                INSERT INTO users (email, name, login, birthday)
                VALUES (?, ?, ?, ?)""";
        int id = insert(query, user.getEmail(), user.getName(), user.getLogin(), user.getBirthday());
        user.setId(id);
        return user;
    }

    @Override
    public User updateUser(User user) {
        String query = """
                UPDATE users
                SET    email = ?,
                       name = ?,
                       login = ?,
                       birthday = ?
                WHERE  id = ?""";

        update(query, user.getEmail(), user.getName(), user.getLogin(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public Optional<User> getUserById(int id) {
        String query = """
                SELECT *
                FROM users
                WHERE id = ?;
                """;
        return findOne(query, id);
    }

    @Override
    public List<User> getAllUsers() {
        String query = """
                SELECT *
                FROM   users
                """;
        return findMany(query);
    }

    @Override
    public void deleteUser(int id) {
        String deleteReviewLikes = "DELETE FROM review_likes WHERE user_id = ?";
        String deleteReviewDislikes = "DELETE FROM review_dislikes WHERE user_id = ?";
        String deleteReviews = "DELETE FROM reviews WHERE user_id = ?";
        String deleteLikes = "DELETE FROM likes WHERE user_id = ?";
        String deleteFriends = "DELETE FROM friends WHERE user_id = ? OR friend_id = ?";
        String deleteEvents = "DELETE FROM events WHERE user_id = ?";
        String deleteUser = "DELETE FROM users WHERE id = ?";

        jdbcTemplate.update(deleteReviewLikes, id);
        jdbcTemplate.update(deleteReviewDislikes, id);
        jdbcTemplate.update(deleteReviews, id);
        jdbcTemplate.update(deleteLikes, id);
        jdbcTemplate.update(deleteFriends, id, id);
        jdbcTemplate.update(deleteEvents, id);
        jdbcTemplate.update(deleteUser, id);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        String firstRequestForFriendship = """
                INSERT INTO friends (user_id, friend_id, status)
                VALUES (?, ?, ?)""";
        insert(firstRequestForFriendship, userId, friendId, FriendshipStatus.PENDING.toString());
    }

    @Override
    public void confirmedFriend(int userId, int friendId) {
        String query = """
                UPDATE friends
                SET status = ? WHERE user_id = ? AND friend_id = ?
                """;
        insert(query, FriendshipStatus.CONFIRMED.toString(), userId, friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        String query = """
                DELETE FROM friends
                WHERE  user_id = ? AND friend_id = ?""";
        delete(query, userId, friendId);
    }

    public void removeAllFriends(int userId) {
        String query = "DELETE FROM friends WHERE user_id = ? OR friend_id = ?";
        jdbcTemplate.update(query, userId, userId);
    }

    @Override
    public Optional<String> getFriendshipStatus(int userId, int friendId) {
        String query = """
                SELECT status
                FROM friends
                WHERE user_id = ?
                  AND friend_id = ?
                """;
        try {
            return Optional.of(jdbcTemplate.queryForObject(query, String.class, userId, friendId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> getFriends(int userId) {
        String query = """
                SELECT u.*
                FROM   users u
                       INNER JOIN friends f
                               ON u.id = f.friend_id
                WHERE f.user_id = ?
                """;
        return findMany(query, userId);
    }

    @Override
    public boolean existsUserById(int id) {
        String query = "SELECT COUNT(*) FROM users WHERE id=?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public boolean existsFriendByIds(int userId, int friendId) {
        String query = "SELECT COUNT(*) FROM friends WHERE user_id=? AND friend_id=?";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, userId, friendId);
        return count != null && count > 0;
    }

    @Override
    public Optional<Integer> findUserWithSharedFilms(int id) {
        String query = """
                 WITH matched_likes AS (
                     SELECT
                         l2.user_id AS matched_user,
                         COUNT(*) AS common_likes
                     FROM likes l1
                     JOIN likes l2 ON l1.film_id = l2.film_id
                                   AND l1.user_id != l2.user_id
                     WHERE l1.user_id = ?
                     GROUP BY l2.user_id
                 )
                 SELECT matched_user, common_likes
                 FROM matched_likes
                 ORDER BY common_likes DESC
                 LIMIT 1;
                """;

        List<Integer> result = jdbcTemplate.query(query, (rs, rowNum) -> rs.getInt("matched_user"), id);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public void logEvent(int userId, int entityId, String eventType, String operation) {
        String query = "INSERT INTO events (user_id, entity_id, event_type, operation, timestamp) VALUES (?, ?, ?, ?, ?)";
        long timestamp = System.currentTimeMillis();

        jdbcTemplate.update(query, userId, entityId, eventType, operation, timestamp);
    }

    @Override
    public List<Event> getUserFeed(int userId) {
        String sql = "SELECT id, user_id, entity_id, event_type, operation, timestamp FROM events WHERE user_id = ? ORDER BY timestamp ASC";

        return jdbcTemplate.query(sql, new EventRowMapper(), userId);
    }
}