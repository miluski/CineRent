package pl.kielce.tu.backend.controller;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import pl.kielce.tu.backend.model.dto.UserDto;
import pl.kielce.tu.backend.service.user.UserService;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { UserController.class, UserControllerTest.TestConfig.class })
class UserControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Test
    void getUser_returnsUserData_whenUserExists() throws Exception {
        UserDto userDto = UserDto.builder()
                .nickname("FilmLover99")
                .age(24)
                .preferredGenres(Arrays.asList("Komedia", "Sci-Fi", "Akcja"))
                .build();

        when(userService.handleGetUser(ArgumentMatchers.any(HttpServletRequest.class)))
                .thenReturn(ResponseEntity.ok(userDto));

        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.nickname").value("FilmLover99"))
                .andExpect(jsonPath("$.age").value(24))
                .andExpect(jsonPath("$.preferredGenres[0]").value("Komedia"))
                .andExpect(jsonPath("$.preferredGenres[1]").value("Sci-Fi"))
                .andExpect(jsonPath("$.preferredGenres[2]").value("Akcja"));

        verify(userService, atLeastOnce()).handleGetUser(ArgumentMatchers.any(HttpServletRequest.class));
    }

    @Test
    void getUser_returnsNotFound_whenUserDoesNotExist() throws Exception {
        when(userService.handleGetUser(ArgumentMatchers.any(HttpServletRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isNotFound());

        verify(userService).handleGetUser(ArgumentMatchers.any(HttpServletRequest.class));
    }

    @Test
    void editUser_returnsAccepted_whenUpdateSuccessful() throws Exception {
        UserDto updateDto = UserDto.builder()
                .nickname("NewNickname")
                .age(30)
                .build();

        when(userService.handleEditUser(
                ArgumentMatchers.any(HttpServletRequest.class),
                ArgumentMatchers.any(UserDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).build());

        mockMvc.perform(patch("/api/v1/user/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isAccepted());

        verify(userService).handleEditUser(
                ArgumentMatchers.any(HttpServletRequest.class),
                ArgumentMatchers.any(UserDto.class));
    }

    @Test
    void editUser_returnsNotFound_whenUserDoesNotExist() throws Exception {
        UserDto updateDto = UserDto.builder()
                .nickname("NonExistent")
                .build();

        when(userService.handleEditUser(
                ArgumentMatchers.any(HttpServletRequest.class),
                ArgumentMatchers.any(UserDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        mockMvc.perform(patch("/api/v1/user/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void editUser_returnsUnprocessableEntity_whenValidationFails() throws Exception {
        UserDto invalidDto = UserDto.builder()
                .nickname("a")
                .build();

        when(userService.handleEditUser(
                ArgumentMatchers.any(HttpServletRequest.class),
                ArgumentMatchers.any(UserDto.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build());

        mockMvc.perform(patch("/api/v1/user/edit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isUnprocessableEntity());
    }

}
