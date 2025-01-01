# java-filmorate
Template repository for the Filmorate project.

## Схема базы данных

Ниже представлена схема базы данных для приложения. Она включает следующие таблицы:
- Пользователи
- Фильмы
- Жанры
- Рейтинги
- Связи между пользователями

![Диаграмма базы данных](https://github.com/GordonSergey/java-filmorate/blob/main/images/diagramfilmorate.png)

### Примеры запросов

1. **Получить все фильмы**:

    ```sql
    SELECT * 
    FROM Film;
    ```

2. **Получить топ-N самых популярных фильмов (по лайкам)**:

    ```sql
    SELECT f.Title, COUNT(l.UserID) AS LikeCount
    FROM Film f
    LEFT JOIN Like l ON f.FilmID = l.FilmID
    GROUP BY f.FilmID
    ORDER BY LikeCount DESC
    LIMIT 10;
    ```

3. **Получить список друзей пользователя**:

    ```sql
    SELECT u.Name
    FROM User u
    JOIN Friendship f ON u.UserID = f.UserID_2
    WHERE f.UserID_1 = 1 AND f.Status = 'друзья';
    ```

4. **Получить все фильмы, поставленные на лайк определённым пользователем**:

    ```sql
    SELECT f.Title
    FROM Film f
    JOIN Like l ON f.FilmID = l.FilmID
    WHERE l.UserID = 1 AND l.LikeStatus = true;
    ```
