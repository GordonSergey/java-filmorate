package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Import({FilmDbStorage.class, UserDbStorage.class})
@ComponentScan
class FilmorateApplicationTests {

	private final UserDbStorage userDbStorage;

	private final FilmDbStorage filmDbStorage;

	@Test
	void testFindAllUsers() {
		User newUser = User.builder()
				.email("test@mail.ru")
				.login("test")
				.name("test")
				.birthday(LocalDate.of(1990, 12, 12))
				.build();

		userDbStorage.addUser(newUser);
		Collection<User> users = userDbStorage.getAllUsers();

		assertThat(users).hasSize(1);
	}

	@Test
	void testCreateUser() {
		User newUser = User.builder()
				.email("test@mail.ru")
				.login("test")
				.name("test")
				.birthday(LocalDate.of(1990, 12, 12))
				.build();

		User user = userDbStorage.addUser(newUser);

		assertThat(user).hasFieldOrPropertyWithValue("id", user.getId());
	}

	@Test
	void testFindUserById() {
		User newUser = User.builder()
				.email("test@mail.ru")
				.login("test")
				.name("test")
				.birthday(LocalDate.of(1990, 12, 12))
				.build();
		User testUser = userDbStorage.addUser(newUser);

		User user = userDbStorage.getUserById(testUser.getId()).get();

		assertThat(user).hasFieldOrPropertyWithValue("id", testUser.getId());
	}

	@Test
	void testUpdateUser() {
		User newUser = User.builder()
				.email("test@mail.ru")
				.login("test")
				.name("test")
				.birthday(LocalDate.of(1990, 12, 12))
				.build();
		User testUser = userDbStorage.addUser(newUser);
		testUser.setName("Updated");

		User user = userDbStorage.updateUser(testUser);
		user = userDbStorage.getUserById(user.getId()).get();

		assertThat(user).hasFieldOrPropertyWithValue("name", "Updated");
	}

	@Test
	void testCreateFilms() {
		Mpa mpa = new Mpa();
		mpa.setId(2);
		Genre genre = new Genre();
		genre.setId(2);
		List<Genre> genres = new ArrayList<>();
		genres.add(genre);
		Film film = Film.builder()
				.mpa(mpa)
				.genres(genres)
				.name("testFilm")
				.description("testDescription")
				.releaseDate(LocalDate.of(1990, 12, 12))
				.duration(120)
				.build();

		Film result = filmDbStorage.addFilm(film);

		assertThat(result).hasFieldOrPropertyWithValue("id", result.getId());
	}

	@Test
	void testFindByIdFilms() {
		Mpa mpa = new Mpa();
		mpa.setId(2);
		Genre genre = new Genre();
		genre.setId(2);
		List<Genre> genres = new ArrayList<>();
		genres.add(genre);

		Film film = Film.builder()
				.mpa(mpa)
				.genres(genres)
				.name("testFilm")
				.description("testDescription")
				.releaseDate(LocalDate.of(1990, 12, 12))
				.duration(120)
				.build();

		film = filmDbStorage.addFilm(film);
		Film result = filmDbStorage.getFilmById(film.getId()).get();

		assertThat(result).hasFieldOrPropertyWithValue("id", film.getId());
	}

	@Test
	void testFindAllFilms() {
		Mpa mpa = new Mpa();
		mpa.setId(2);
		Genre genre = new Genre();
		genre.setId(2);
		List<Genre> genres = new ArrayList<>();
		genres.add(genre);

		Film film = Film.builder()
				.mpa(mpa)
				.genres(genres)
				.name("testFilm")
				.description("testDescription")
				.releaseDate(LocalDate.of(1990, 12, 12))
				.duration(120)
				.build();

		filmDbStorage.addFilm(film);
		Collection<Film> films = filmDbStorage.getAllFilms();

		assertThat(films).hasSize(1);
	}
}