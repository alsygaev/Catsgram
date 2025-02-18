package ru.yandex.practicum.catsgram.model;

import java.time.Instant;
import lombok.*;

@Data // Генерирует геттеры, сеттеры, equals, hashCode и toString
@NoArgsConstructor // Генерирует конструктор без аргументов
@AllArgsConstructor // Генерирует конструктор со всеми аргументами
@Builder // Позволяет использовать паттерн Builder
@EqualsAndHashCode(of = {"id"})

public class Post {
    Long id;
    long authorId;
    String description;
    Instant postDate;

}
