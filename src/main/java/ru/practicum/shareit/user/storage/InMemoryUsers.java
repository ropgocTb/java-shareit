package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.AlreadyTakenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component("InMemoryUsers")
public class InMemoryUsers implements Users {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User addUser(User user) {
        validateUser(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User editUser(User user) {
        User updatedUser = users.get(user.getId());
        if (user.getName() != null) {
            updatedUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            validateEmail(user);
            updatedUser.setEmail(user.getEmail());
        }
        users.put(user.getId(), updatedUser);
        return user;
    }

    @Override
    public Optional<User> getUser(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void deleteUser(Long id) {
        users.remove(id);
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isEmpty() || !user.getEmail().contains("@"))
            throw new ValidationException("почта пользователя не должна быть пустой и должна содержать символ @");
        validateEmail(user);
    }

    private void validateEmail(User user) {
        for (User addedUser : users.values()) {
            if (user.getEmail().equals(addedUser.getEmail()) && !Objects.equals(user.getId(), addedUser.getId()))
                throw new AlreadyTakenException("Пользователь с такой почтой уже существует");
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
