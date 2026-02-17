package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.security.SecureRandom;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
//тесты эндпойнтов с моками в слое сервисов
public class UserControllerTests {
    MockMvc mvc;
    private ObjectMapper objectMapper;

    @Mock
    @Qualifier("UserServiceDb")
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void postUserTest() throws Exception {
        UserDto userDto =  UserDto.builder()
                .name(getRandomString())
                .email(getRandomEmail())
                .build();

        UserDto mockUser = UserDto.builder()
                .id(1L)
                .name("mock")
                .email("mock@gmail.com")
                .build();

        when(userService.addUser(any(UserDto.class))).thenReturn(mockUser);

        mvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(mockUser.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(mockUser.getName())))
                .andExpect(jsonPath("$.email", is(mockUser.getEmail())));

        verify(userService, times(1)).addUser(any(UserDto.class));
    }

    @Test
    void getUserTest() throws Exception {
        UserDto mockUser = UserDto.builder()
                .id(1L)
                .name("mock")
                .email("mock@gmail.com")
                .build();

        when(userService.getUser(anyLong())).thenReturn(mockUser);

        mvc.perform(get("/users/{id}", anyLong()))
                .andExpect(jsonPath("$.id", is(mockUser.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(mockUser.getName())))
                .andExpect(jsonPath("$.email", is(mockUser.getEmail())));

        verify(userService, times(1)).getUser(anyLong());
    }

    @Test
    void getUsersTest() throws Exception {
        UserDto mockUser1 = UserDto.builder()
                .id(1L)
                .name("mock")
                .email("mock1@gmail.com")
                .build();

        UserDto mockUser2 = UserDto.builder()
                .id(2L)
                .name("mock")
                .email("mock2@gmail.com")
                .build();

        when(userService.getUsers()).thenReturn(List.of(mockUser1, mockUser2));

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(1L), Long.class))
                .andExpect(jsonPath("$[1].id", is(2L), Long.class));

        verify(userService, times(1)).getUsers();
    }

    @Test
    void patchUserTest() throws Exception {
        UserDto userDto =  UserDto.builder()
                .name(getRandomString())
                .email(getRandomEmail())
                .build();

        UserDto mockUser = UserDto.builder()
                .id(1L)
                .name("updated")
                .email("updated@gmail.com")
                .build();

        when(userService.editUser(eq(mockUser.getId()), any(UserDto.class))).thenReturn(mockUser);

        mvc.perform(patch("/users/{id}", mockUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(mockUser.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(mockUser.getName())))
                .andExpect(jsonPath("$.email", is(mockUser.getEmail())));

        verify(userService, times(1)).editUser(eq(mockUser.getId()), any(UserDto.class));
    }

    @Test
    void deleteUserTest() throws Exception {
        doNothing().when(userService).deleteUser(anyLong());

        mvc.perform(delete("/users/{id}", anyLong()));

        verify(userService, times(1)).deleteUser(anyLong());
    }

    private String getRandomString() {
        final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        final int STRING_LENGTH = 10;
        final SecureRandom RANDOM = new SecureRandom();

        StringBuilder sb = new StringBuilder(STRING_LENGTH);

        for (int i = 0; i < STRING_LENGTH; i++) {
            int randIndex = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(randIndex));
        }

        return sb.toString();
    }

    private String getRandomEmail() {
        return getRandomString() + "@gmail.com";
    }
}
