package ru.practicum.shareit.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service("UserServiceDb")
@Transactional(readOnly = true)
public class UserServiceDb implements UserService {
    private final UserRepository repository;

    public UserServiceDb(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = false)
    public UserDto addUser(UserDto userDto) {
        return UserMapper.toUserDto(repository.save(UserMapper.toUser(userDto)));
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public UserDto editUser(Long id, UserDto userDto) {
        UserDto addedUser = UserMapper.toUserDto(repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id не найден")));
        //проверить имя
        if (userDto.getName() != null && !userDto.getName().equals(addedUser.getName())) {
            addedUser.setName(userDto.getName());
        }

        //проверить почту
        if (userDto.getEmail() != null && !userDto.getEmail().equals(addedUser.getEmail())) {
            addedUser.setEmail(userDto.getEmail());
        }

        return UserMapper.toUserDto(repository.save(UserMapper.toUser(addedUser)));
    }

    @Override
    public UserDto getUser(Long id) {
        return UserMapper.toUserDto(repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id не найден")));
    }

    @Override
    public List<UserDto> getUsers() {
        List<UserDto> users = new ArrayList<>();

        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        Pageable page = PageRequest.of(0, 15, sortById);

        do {
            Page<User> userPage = repository.findAll(page);

            users.addAll(userPage.getContent().stream()
                    .map(UserMapper::toUserDto)
                    .toList());

            if (userPage.hasNext()) {
                page = userPage.nextOrLastPageable();
            } else {
                page = null;
            }
        } while (page != null);

        return users;
    }

    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void deleteUser(Long id) {
        repository.delete(repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с таким id не найден")));
    }
}
