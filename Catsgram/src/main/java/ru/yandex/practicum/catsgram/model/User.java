package ru.yandex.practicum.catsgram.model;

import java.time.Instant;
import lombok.*;

@Data // Генерирует геттеры, сеттеры, equals, hashCode и toString
@NoArgsConstructor // Генерирует конструктор без аргументов
@AllArgsConstructor // Генерирует конструктор со всеми аргументами
@Builder // Позволяет использовать паттерн Builder
@EqualsAndHashCode(of = {"email"})

public class User {
    Long id;
    String username;
    String email;
    String password;
    Instant registrationDate;

}