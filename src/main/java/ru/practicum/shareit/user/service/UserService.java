package ru.practicum.shareit.user.service;


import ru.practicum.shareit.user.dto.UserDto;

public interface UserService {
    UserDto addUser(UserDto user);

    UserDto editUser(Long id, UserDto user);

    UserDto getUser(Long id);

    void deleteUser(Long id);
}
