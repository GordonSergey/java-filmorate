package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {

    private final GenreDbStorage genreDbStorage;

    public Collection<Genre> getAll() {
        return genreDbStorage.findAll();
    }

    public Genre getById(long id) {
        Optional<Genre> genre = genreDbStorage.getById(id);
        if (genre.isEmpty()) {
            throw new NoSuchElementException("Genre is not found with id " + id);
        }
        return genre.get();
    }
}
