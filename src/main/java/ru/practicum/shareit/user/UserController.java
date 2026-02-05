package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable(name = "id") Long id) {
        return service.getUser(id);
    }

    @GetMapping
    public List<UserDto> getUsers() {
        return service.getUsers();
    }

    @PostMapping
    public UserDto addUser(@RequestBody UserDto userDto) {
        return service.addUser(userDto);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable(name = "id") Long id) {
        service.deleteUser(id);
    }

    @PatchMapping("/{id}")
    public UserDto editUser(@PathVariable(name = "id") Long id, @RequestBody UserDto userDto) {
        return service.editUser(id, userDto);
    }
}
