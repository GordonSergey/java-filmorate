package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.ResultSetExtractor;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class FilmWithDirectorsExtractor implements ResultSetExtractor<List<Film>> {
    @Override
    public List<Film> extractData(ResultSet rs) throws SQLException {
        Map<Integer, Film> filmMap = new HashMap<>();

        while (rs.next()) {
            int filmId = rs.getInt("film_id");

            Film film = filmMap.computeIfAbsent(filmId, id -> {
                try {
                    Film newFilm = new Film();
                    newFilm.setId(id);
                    newFilm.setName(rs.getString("film_name"));
                    newFilm.setDescription(rs.getString("description"));
                    newFilm.setReleaseDate(rs.getDate("release_date").toLocalDate());
                    newFilm.setDuration(rs.getInt("duration"));
                    newFilm.setMpa(new Mpa(rs.getInt("rating_id"), rs.getString("rating_name")));
                    newFilm.setDirectors(new ArrayList<>());
                    return newFilm;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            int directorId = rs.getInt("director_id");
            if (directorId > 0) {
                Director director = new Director(directorId, rs.getString("director_name"));

                boolean isDuplicate = film.getDirectors().stream()
                        .anyMatch(existingDirector -> existingDirector.getId() == directorId);

                if (!isDuplicate) {
                    film.getDirectors().add(director);
                }
            }
        }

        return new ArrayList<>(filmMap.values());
    }
}