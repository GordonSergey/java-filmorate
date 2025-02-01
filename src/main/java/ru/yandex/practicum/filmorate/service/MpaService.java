package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MpaService {

    private final MpaDbStorage mpaDbStorage;

    public Collection<Mpa> getAll() {
        return mpaDbStorage.findAll();
    }

    public Mpa getById(long id) {
        Optional<Mpa> mpa = mpaDbStorage.getById(id);
        if (mpa.isEmpty()) {
            throw new NoSuchElementException("MPA is not found with id " + id);
        }
        return mpa.get();
    }
}