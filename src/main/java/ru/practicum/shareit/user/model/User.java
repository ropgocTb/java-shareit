package ru.practicum.shareit.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

/**
 * TODO Sprint add-controllers.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 512)
    @Email(message = "Неверный формат почты")
    private String email;
}
