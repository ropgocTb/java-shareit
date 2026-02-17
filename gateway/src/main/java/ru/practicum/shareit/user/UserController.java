package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserClient userClient;

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUser(@PathVariable(name = "id") Long userId) {
        return userClient.getUser(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        return userClient.getUsers();
    }

    @PostMapping
    public ResponseEntity<Object> postUser(@Valid @RequestBody UserDto userDto) {
        return userClient.addUser(userDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> editUser(@PathVariable(name = "id") Long userId, @RequestBody UserDto userDto) {
        return userClient.editUser(userId, userDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(@PathVariable(name = "id") Long userId) {
        return userClient.deleteUser(userId);
    }
}
