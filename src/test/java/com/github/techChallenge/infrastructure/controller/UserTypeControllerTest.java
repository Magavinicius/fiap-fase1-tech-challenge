package com.github.techChallenge.infrastructure.controller;

import com.github.techChallenge.application.exceptions.GlobalExceptionHandler;
import com.github.techChallenge.application.exceptions.ProblemDetailFactory;
import com.github.techChallenge.application.exceptions.UserTypeNotFoundException;
import com.github.techChallenge.application.usecases.usertype.*;
import com.github.techChallenge.application.validators.UserTypeValidator;
import com.github.techChallenge.domain.usertype.dto.UserTypeCreateInputDTO;
import com.github.techChallenge.domain.usertype.dto.UserTypeOutputDTO;
import com.github.techChallenge.domain.usertype.dto.UserTypeUpdateInputDTO;
import com.github.techChallenge.infrastructure.controllers.usertype.UserTypeController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserTypeControllerTest {

    private AutoCloseable mocks;

    private MockMvc mockMvc;

    @Mock
    private CreateUserTypeUseCase createUserTypeUseCase;

    @Mock
    private UpdateUserTypeUseCase updateUserTypeUseCase;

    @Mock
    private FindUserTypeUseCase findUserTypeUseCase;

    @Mock
    private ListUserTypeUseCase listUserTypeUseCase;

    @Mock
    private DeleteUserTypeUseCase deleteUserTypeUseCase;

    @Mock
    private UserTypeValidator userTypeValidator;

    @InjectMocks
    private UserTypeController userTypeController;

    @BeforeEach
    void setup() {
        mocks = MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(userTypeController)
                .setControllerAdvice(new GlobalExceptionHandler(new ProblemDetailFactory()))
                .addFilter((request, response, chain) -> {
                    response.setCharacterEncoding("UTF-8");
                    chain.doFilter(request, response);
                }, "/*")
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    @DisplayName("Deve criar um tipo de usuário e retornar 201")
    void shouldCreateUserTypeController() throws Exception {
        var dto = new UserTypeCreateInputDTO("Administrador");

        when(createUserTypeUseCase.execute(any(UserTypeCreateInputDTO.class)))
                .thenReturn(new UserTypeOutputDTO(1, "Administrador"));

        mockMvc.perform(post("/api/v1/user-type/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andDo(print())
                .andExpect(status().isCreated());

        verify(createUserTypeUseCase, times(1))
                .execute(any(UserTypeCreateInputDTO.class));
    }

    @Test
    @DisplayName("Deve atualizar um tipo de usuário e retornar 200")
    void shouldUpdateUserTypeController() throws Exception {
        var dto = new UserTypeUpdateInputDTO("Administrador");

        when(updateUserTypeUseCase.execute(any(UserTypeUpdateInputDTO.class), eq(1)))
                .thenReturn(new UserTypeOutputDTO(1, "Administrador"));

        mockMvc.perform(put("/api/v1/user-type/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(dto)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(updateUserTypeUseCase, times(1))
                .execute(any(UserTypeUpdateInputDTO.class), eq(1));
    }

    @Test
    @DisplayName("Deve encontrar um tipo de usuário pelo id")
    void shouldFindByIdUserTypeController() throws Exception {
        when(findUserTypeUseCase.execute(1))
                .thenReturn(new UserTypeOutputDTO(1, "Administrador"));

        mockMvc.perform(get("/api/v1/user-type/{id}", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Administrador"));

        verify(findUserTypeUseCase, times(1)).execute(1);
    }

    @Test
    @DisplayName("Deve listar todos os tipos de usuário")
    void shouldListAllUserTypeController() throws Exception {
        var content = List.of(
                new UserTypeOutputDTO(1, "Proprietário"),
                new UserTypeOutputDTO(2, "Cliente")
        );
        var page = new PageImpl<>(content, PageRequest.of(0, 10), content.size());

        when(listUserTypeUseCase.execute(0, 10))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/user-type/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Proprietário"))
                .andExpect(jsonPath("$.content[1].title").value("Cliente"));

        verify(listUserTypeUseCase, times(1)).execute(0, 10);
    }

    @Test
    @DisplayName("Deve deletar um tipo de usuário e retornar 204")
    void shouldDeleteUserTypeController() throws Exception {
        mockMvc.perform(delete("/api/v1/user-type/{id}", 1))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(deleteUserTypeUseCase, times(1)).execute(1);
    }

    @Test
    @DisplayName("Deve retornar 404 ao deletar um tipo de usuário inexistente")
    void shouldReturnNotFoundWhenDeleteNonExistentUserType() throws Exception {
        doThrow(new UserTypeNotFoundException("Tipo de usuário não encontrado"))
                .when(deleteUserTypeUseCase).execute(999);

        mockMvc.perform(delete("/api/v1/user-type/{id}", 999))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(deleteUserTypeUseCase, times(1)).execute(999);
    }

    private static String asJsonString(Object obj) {
        return new ObjectMapper().writeValueAsString(obj);
    }
}