-- schema.sql
CREATE TABLE IF NOT EXISTS ratings
(
    id   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS genres
(
    id   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS films
(
    id           INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name         VARCHAR(255)                   NOT NULL,
    description  TEXT,
    release_date DATE                           NOT NULL,
    duration     INTEGER                        NOT NULL,
    rating_id    INTEGER REFERENCES ratings (id) NOT NULL
);

CREATE TABLE IF NOT EXISTS film_genres
(
    PRIMARY KEY (film_id, genre_id),
    film_id   INTEGER NOT NULL references films (id),
    genre_id INTEGER references genres (id)
);

CREATE TABLE IF NOT EXISTS users
(
    id       INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name     VARCHAR(255)        NOT NULL,
    email    VARCHAR(255) UNIQUE NOT NULL,
    login    VARCHAR(255)        NOT NULL,
    birthday DATE                NOT NULL
);

CREATE TABLE IF NOT EXISTS friends
(
    id        INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id   INTEGER     NOT NULL REFERENCES users (id),
    friend_id INTEGER     NOT NULL REFERENCES users (id),
    status    VARCHAR(50) NOT NULL,
    UNIQUE (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS likes
(
    id      INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    user_id INTEGER     NOT NULL REFERENCES users (id),
    film_id INTEGER     NOT NULL REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS directors
(
    id   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS film_directors
(
    film_id INTEGER NOT NULL REFERENCES films (id),
    director_id INTEGER NOT NULL REFERENCES directors (id),
    PRIMARY KEY (film_id, director_id)
);