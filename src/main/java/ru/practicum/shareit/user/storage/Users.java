package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.Optional;

public interface Users {
    User addUser(User user);

    User editUser(User user);

    Optional<User> getUser(Long id);

    void deleteUser(Long id);
}
