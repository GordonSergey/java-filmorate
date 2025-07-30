
# 🎬 Filmorate

**Filmorate** — это приложение для управления рейтингами фильмов, пользовательскими лайками, друзьями и рекомендациями.  
Пользователи могут делиться мнением о фильмах, ставить оценки, добавлять фильмы в избранное и видеть, что понравилось друзьям.

---

## 🚀 Возможности

- Регистрация и управление пользователями
- Добавление, редактирование и удаление фильмов
- Система лайков и подсчёт популярности фильмов
- Хранение жанров и возрастных рейтингов (MPA)
- Добавление в друзья и отображение общих друзей
- Получение рекомендаций на основе лайков
- Сортировка фильмов по популярности

---

## 🛠️ Технологии и стек

| Технология           | Описание                             |
|----------------------|--------------------------------------|
| Java 21              | Язык программирования                |
| Spring Boot          | Backend-фреймворк                    |
| Spring Web           | Создание REST API                    |
| Spring Data JPA      | Работа с базой данных                |
| PostgreSQL           | Реляционная база данных              |
| Hibernate            | ORM                                  |
| Lombok               | Упрощение кода                       |
| Maven                | Сборка проекта                       |
| JUnit, Mockito       | Модульное тестирование               |
| Testcontainers       | Интеграционные тесты                 |

---

## ⚙️ Архитектура

Монолитное Spring Boot приложение:

- Контроллеры: `UserController`, `FilmController`, `ReviewController`, `DirectorController`, `FeedController`
- Сервисы: `UserService`, `FilmService`, `RecommendationService` и др.
- Хранилища: реализованы на основе интерфейсов с аннотациями `@Repository`

---

## 🗃️ Схема БД и модели данных

**Модели:**

- `User` — пользователь
- `Film` — фильм
- `Like` — лайк
- `Friendship` — друзья
- `Genre`, `MPA` — справочники
- `Review` — отзыв
- `Feed` — лента активности

---

## 🗺️ Схема базы данных

![Схема БД проекта Filmorate](https://github.com/GordonSergey/java-filmorate/blob/main/images/diagramfilmorate.png)

На схеме показаны таблицы: `users`, `films`, `likes`, `friends`, `genres`, `ratings`, `reviews` и их связи.

---

## 🐳 Запуск с помощью Docker

### Требования

- Docker
- Docker Compose

```bash
docker-compose up
```

---

## 📂 Сборка вручную (без Docker)

```bash
# Сборка и установка зависимостей
mvn clean install

# Запуск приложения
mvn spring-boot:run
```

---

## 🧪 Тестирование

```bash
mvn test
```

Используются:

- `JUnit 5` — модульные тесты
- `Testcontainers` — интеграционные тесты с изолированной PostgreSQL

---


---

## 📖 Описание API

### 🎬 Фильм

- `GET /films` — получение списка всех фильмов
- `GET /films/{filmId}` — получение фильма по ID
- `POST /films` — добавление фильма
- `PUT /films` — обновление данных о фильме
- `PUT /films/{id}/like/{userId}` — пользователь ставит лайк фильму
- `DELETE /films/{id}/like/{userId}` — пользователь удаляет лайк
- `GET /films/popular?count={count}` — список N популярных фильмов по лайкам

### 👤 Пользователь

- `GET /users` — получение списка всех пользователей
- `GET /users/{userId}` — получение пользователя по ID
- `POST /users` — создание пользователя
- `PUT /users` — обновление данных о пользователе
- `PUT /users/{userId}/friends/{friendId}` — добавление в друзья
- `DELETE /users/{userId}/friends/{friendId}` — удаление из друзей
- `GET /users/{userId}/friends` — список друзей пользователя
- `GET /users/{userId}/friends/common/{otherId}` — общие друзья с другим пользователем

### 🎯 Рейтинг

- `GET /mpa` — список всех рейтингов
- `GET /mpa/{id}` — рейтинг по ID

### 🏷️ Жанр

- `GET /genres` — список всех жанров
- `GET /genres/{id}` — жанр по ID

## 📊 Примеры SQL-запросов

**Получить все фильмы:**

```sql
SELECT * FROM Film;
```

**Топ-10 популярных фильмов по лайкам:**

```sql
SELECT f.Title, COUNT(l.UserID) AS LikeCount
FROM Film f
LEFT JOIN Like l ON f.FilmID = l.FilmID
GROUP BY f.FilmID
ORDER BY LikeCount DESC
LIMIT 10;
```

**Список друзей пользователя:**

```sql
SELECT u.Name
FROM User u
JOIN Friendship f ON u.UserID = f.UserID_2
WHERE f.UserID_1 = 1 AND f.Status = 'друзья';
```

**Фильмы, лайкнутые пользователем:**

```sql
SELECT f.*
FROM Film f
JOIN Like l ON f.FilmID = l.FilmID
WHERE l.UserID = 1;
```

---

## 👤 Авторы

Проект разработан в рамках командной работы.  
**Тимлид:** [GordonSergey](https://github.com/GordonSergey)

**Участники команды:**
- [IraUs1601](https://github.com/IraUs1601)
- [NikitaMashkarin](https://github.com/NikitaMashkarin)
- [shepilovaElena](https://github.com/shepilovaElena)


Разработано в рамках учебного проекта.  
GitHub: [GordonSergey](https://github.com/GordonSergey)