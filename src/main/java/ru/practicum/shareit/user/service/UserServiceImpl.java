package ru.practicum.shareit.user.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.storage.Users;

import java.util.List;

@Service("UserServiceImpl")
public class UserServiceImpl implements UserService {
    private final Users storage;

    public UserServiceImpl(@Qualifier("InMemoryUsers") Users storage) {
        this.storage = storage;
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        return UserMapper.toUserDto(storage.addUser(UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto editUser(Long id, UserDto userDto) {
        //проверка на существование
        getUser(id);
        userDto.setId(id);
        return UserMapper.toUserDto(storage.editUser(UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto getUser(Long id) {
        return UserMapper.toUserDto(storage.getUser(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id не найден")));
    }

    @Override
    public List<UserDto> getUsers() {
        return storage.getUsers().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void deleteUser(Long id) {
        getUser(id);
        storage.deleteUser(id);
    }
}
