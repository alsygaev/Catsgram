package ru.yandex.practicum.catsgram.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.Post;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/posts")
public class PostController {
    private final Map<Long, Post> posts = new HashMap<>();

    @GetMapping
    public Collection<Post> findAll() {
        return posts.values();
    }

    @PostMapping
    public Post create(@RequestBody Post post) {
        if (post.getDescription() == null || post.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание не может быть пустым");
        }
        post.setId(getNextId());
        post.setPostDate(Instant.now());
        posts.put(post.getId(), post);

        return post;
    }

    @PutMapping
    public Post update(@RequestBody Post newPost) {
        //Проверяем необходимые условия
        if (newPost.getId() == null) {
            throw new ConditionsNotMetException("ID не может быть пустым!");
        }

        if (posts.containsKey(newPost.getId())) {
            Post oldPost = posts.get(newPost.getId());
            if (oldPost.getDescription() == null || oldPost.getDescription().isBlank()) {
                throw new ConditionsNotMetException("Описание не может быть пустым!");
            }

            //Если пост найден и все условия соблюдены, тогда обновляем содержимое поста
            oldPost.setDescription(newPost.getDescription());

            return oldPost;
        }
        throw new NotFoundException("Пост id "+ newPost.getId() + " не найден");
    }

    //Метод для генерации ID
    private long getNextId() {
        long currentMaxId = posts.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);

        return ++currentMaxId;
    }
}
