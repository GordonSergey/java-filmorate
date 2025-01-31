package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorDbStorage directorDbStorage;

    public List<Director> getAllDirectors() {
        return directorDbStorage.getAllDirectors();
    }

    public Director getDirectorById(int id) {
        return directorDbStorage.getDirectorById(id)
                .orElseThrow(() -> new NoSuchElementException("Director with ID " + id + " not found"));
    }

    public Director addDirector(Director director) {
        return directorDbStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        if (!directorDbStorage.existsById(director.getId())) {
            throw new NoSuchElementException("Director with ID " + director.getId() + " not found");
        }
        return directorDbStorage.update(director);
    }

    public void deleteDirector(int id) {
        directorDbStorage.deleteDirector(id);
    }
}