package ru.yandex.practicum.catsgram.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DublicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final HashMap<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        if (user.getEmail().isEmpty()) {
            throw new ConditionsNotMetException("Email должен быть указан");
        }

        if (users.containsKey(user.getEmail())) {
            throw new ConditionsNotMetException("Этот email уже используется");
        }
        user.setId(getNextId());
        user.setEmail(user.getEmail());
        user.setRegistrationDate(Instant.now());

        users.put(user.getId(), user);

        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        //Проверяем необходимые условия
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (users.containsKey(newUser.getEmail())) {
            throw new DublicatedDataException("Этот имейл уже используется");
        }

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            if (oldUser.getEmail() != null || !oldUser.getPassword().isBlank()) {
                oldUser.setEmail(newUser.getEmail());
                oldUser.setPassword(newUser.getPassword());
                oldUser.setUsername(newUser.getUsername());
            }

            return oldUser;
        }
        throw new NotFoundException("Пользователь с username: "+ newUser.getUsername()
                + " и email: " + newUser.getEmail() + " не найден");
    }

    //Метод для генерации ID
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentMaxId;
    }
}
