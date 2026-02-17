package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class UserIntegrationTests {
    @Autowired
    @Qualifier("UserServiceDb")
    private UserService userService;

    @Autowired
    private UserRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    private User user1;
    private User user2;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        user1 = User.builder()
                .name(getRandomString())
                .email(getRandomEmail())
                .build();
        user2 = User.builder()
                .name(getRandomString())
                .email(getRandomEmail())
                .build();

        userDto = UserDto.builder()
                .name(getRandomString())
                .email(getRandomEmail())
                .build();
    }

    @Test
    void saveNewValidUserTest() throws Exception {
        mvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    public void getUserByIdTest() throws Exception {
        User user = repository.save(user1);

        mvc.perform(get("/users/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(user.getName())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));


        //несуществующий id
        mvc.perform(get("/users/{userId}", user.getId() + 29))
                .andExpect(status().isNotFound());
    }

    @Test
    public void getUserItemsIntegrationTest() throws Exception {
        //сохранить пользователей
        User usr1 = repository.save(user1);
        User usr2 = repository.save(user2);

        mvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].id", is(usr1.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(usr1.getName())))
                .andExpect(jsonPath("$[0].email", is(usr1.getEmail())))
                .andExpect(jsonPath("$[1].id", is(usr2.getId()), Long.class))
                .andExpect(jsonPath("$[1].name", is(usr2.getName())))
                .andExpect(jsonPath("$[1].email", is(usr2.getEmail())));

    }

    @Test
    public void updateUserTest() throws Exception {
        User usr = repository.save(user1);
        User usr1 = repository.save(user2);

        //проверить что не добавляется при обновлении с существующим email
        userDto.setEmail(usr1.getEmail());

        mvc.perform(patch("/users/{id}", usr.getId())
                .content(objectMapper.writeValueAsString(userDto))
                .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        //проверить что не обновляется при неверном id
        userDto.setEmail(getRandomEmail());

        mvc.perform(patch("/users/{id}", usr.getId() + 29)
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        //проверить что обновляется при валидных данных (поля name и email)
        mvc.perform(patch("/users/{id}", usr.getId())
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(usr.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    public void deleteUserTest() throws Exception {
        User usr = repository.save(user1);

        mvc.perform(delete("/users/{id}", usr.getId()))
                .andExpect(status().isOk());

        //проверить что удалилось
        assertTrue(repository.findById(usr.getId()).isEmpty(), "Пользователь остался в бд после удаления");

        //проверить удаление с несуществующим id
        mvc.perform(delete("/users/{id}", usr.getId() + 29))
                .andExpect(status().isNotFound());
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
