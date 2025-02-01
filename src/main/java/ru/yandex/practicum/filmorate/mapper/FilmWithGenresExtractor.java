package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.ResultSetExtractor;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.springframework.dao.DataAccessException;

public class FilmWithGenresExtractor implements ResultSetExtractor<List<Film>> {
    @Override
    public List<Film> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Integer, Film> filmMap = new HashMap<>();

        while (rs.next()) {
            int filmId = rs.getInt("film_id");

            Film film = filmMap.computeIfAbsent(filmId, id -> {
                Film newFilm = new Film();
                newFilm.setId(id);
                try {
                    newFilm.setName(rs.getString("film_name"));
                    newFilm.setDescription(rs.getString("description"));
                    newFilm.setReleaseDate(rs.getDate("release_date").toLocalDate());
                    newFilm.setDuration(rs.getInt("duration"));
                    newFilm.setMpa(new Mpa(rs.getInt("rating_id"), rs.getString("rating_name")));
                    newFilm.setGenres(new ArrayList<>());
                } catch (SQLException e) {
                    throw new RuntimeException("Error extracting film data", e);
                }
                return newFilm;
            });

            int genreId = rs.getInt("genre_id");
            if (genreId != 0) {
                Genre genre = new Genre(genreId, rs.getString("genre_name"));
                if (!film.getGenres().contains(genre)) {
                    film.getGenres().add(genre);
                }
            }
        }
        return new ArrayList<>(filmMap.values());
    }
}