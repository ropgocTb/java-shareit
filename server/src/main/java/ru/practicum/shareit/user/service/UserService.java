package ru.practicum.shareit.user.service;


import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto addUser(UserDto user);

    UserDto editUser(Long id, UserDto user);

    UserDto getUser(Long id);

    List<UserDto> getUsers();

    void deleteUser(Long id);
}
