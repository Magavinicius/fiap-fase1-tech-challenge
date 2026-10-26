package com.github.techChallenge.application.usecases.user;

import com.github.techChallenge.application.exceptions.DuplicateEmailException;
import com.github.techChallenge.application.exceptions.DuplicateLoginException;
import com.github.techChallenge.application.exceptions.UserNotFoundException;
import com.github.techChallenge.application.gateways.user.IUserGateway;
import com.github.techChallenge.application.validators.UserValidator;
import com.github.techChallenge.domain.user.Address;
import com.github.techChallenge.domain.user.IUserMapper;
import com.github.techChallenge.domain.user.InvalidUserDataException;
import com.github.techChallenge.domain.user.User;
import com.github.techChallenge.domain.user.UserLevel;
import com.github.techChallenge.domain.user.dto.UserOutputDTO;
import com.github.techChallenge.domain.user.dto.UserUpdateInputDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class UpdateUserUseCaseTest {

    private static final LocalDateTime CREATED_AT =
            LocalDateTime.of(2020, 1, 1, 12, 0);
    private static final LocalDateTime UPDATED_AT = CREATED_AT.plusHours(1);

    @Test
    void shouldNotPersistWhenUserDataIsInvalid() {
        UserUpdateInputDTO input = new UserUpdateInputDTO(
                "", "user@email.com", "user", UserLevel.CUSTOMER,
                new Address("Rua A", "10", null, "01000-000",
                        "Centro", "São Paulo", "SP", "Brasil")
        );
        when(gateway.existById(1L)).thenReturn(true);
        when(validator.emailExists(input.email(), 1L)).thenReturn(false);
        when(validator.loginExists(input.login(), 1L)).thenReturn(false);

        assertThrows(InvalidUserDataException.class, () -> updateUserUseCase.execute(input, 1L));

        verify(gateway, never()).update(any(User.class), any(Long.class));
        verifyNoInteractions(mapper);
    }

    @InjectMocks
    private UpdateUserUseCase updateUserUseCase;

    @Mock
    private IUserGateway gateway;

    @Mock
    private IUserMapper mapper;

    @Mock
    private UserValidator validator;

    private User user;

    @BeforeEach
    void setUp() {
        updateUserUseCase = new UpdateUserUseCase(gateway, mapper, validator);
        Address address = new Address("Rua 1","123","N/A",
                "11235-800","Bairro","Cidade",
                "UF","Brasil");

        this.user = new User(1L, "User 1", "user@user.com.br", "user", "password", UserLevel.CUSTOMER,
                null, address, CREATED_AT, CREATED_AT);

    }

    @Test
    @DisplayName("Deve alterar os dados do usuário")
    void shouldUpdateAUserData() {
        User mockUpdatedUser = new User(
            user.getId(),
            "Nome alterado",
            "alterado@email.com",
            "login alterado",
            "password alterado",
            UserLevel.CUSTOMER,
            null,
            new Address("Rua 2","1234","N/A alterado",
                    "11235-900","Bairro alterado","Cidade alterado",
                    "RJ","Argentinha"),
            user.getCreatedAt(),
            UPDATED_AT
        );

        UserOutputDTO convertedOutputDTO = new UserOutputDTO(
            mockUpdatedUser.getId(),
            mockUpdatedUser.getName(),
            mockUpdatedUser.getEmail(),
            mockUpdatedUser.getLogin(),
            mockUpdatedUser.getUserLevel(),
            null,
            mockUpdatedUser.getAddress(),
            mockUpdatedUser.getCreatedAt(),
            mockUpdatedUser.getUpdatedAt());

        when(gateway.existById(any(Long.class))).thenReturn(true);
        when(validator.emailExists(any(String.class), any(Long.class))).thenReturn(false);
        when(validator.loginExists(any(String.class), any(Long.class))).thenReturn(false);
        when(gateway.update(any(User.class), any(Long.class))).thenReturn(mockUpdatedUser);
        when(mapper.fromDomainToOutputDTO(mockUpdatedUser)).thenReturn(convertedOutputDTO);

        UserUpdateInputDTO inputDTO = new UserUpdateInputDTO("User 1","user@user.com.br",
                "user", UserLevel.CUSTOMER, new Address("Rua 2","1234","N/A alterado",
                "11235-900","Bairro alterado","Cidade alterado",
                "RJ","Argentinha"));

        UserOutputDTO outputDTO = updateUserUseCase.execute(inputDTO, 1L);

        assertNotNull(outputDTO.id());
        assertNotNull(outputDTO.createdAt());
        assertNotNull(outputDTO.updatedAt());
        assertEquals(CREATED_AT, outputDTO.createdAt());
        assertEquals(UPDATED_AT, outputDTO.updatedAt());
        Assertions.assertEquals(mockUpdatedUser.getEmail(), outputDTO.email());
        Assertions.assertEquals(mockUpdatedUser.getLogin(), outputDTO.login());
        Assertions.assertEquals(mockUpdatedUser.getName(), outputDTO.name());
        Assertions.assertEquals(mockUpdatedUser.getAddress().address(), outputDTO.address().address());
        Assertions.assertEquals(mockUpdatedUser.getAddress().city(), outputDTO.address().city());
        Assertions.assertEquals(mockUpdatedUser.getAddress().complement(), outputDTO.address().complement());
        Assertions.assertEquals(mockUpdatedUser.getAddress().country(), outputDTO.address().country());
        Assertions.assertEquals(mockUpdatedUser.getAddress().neighborhood(), outputDTO.address().neighborhood());
        Assertions.assertEquals(mockUpdatedUser.getAddress().number(), outputDTO.address().number());
        Assertions.assertEquals(mockUpdatedUser.getAddress().state(), outputDTO.address().state());
        Assertions.assertEquals(mockUpdatedUser.getAddress().zipCode(), outputDTO.address().zipCode());
    }

    @Test
    @DisplayName("Deve lançar exceção caso não exista um usuário com o ID informado")
    void shouldThrowAnExceptionWhenThereIsNotAnyUserWithThisID() {
        User mockUpdatedUser = new User(
                user.getId(),
                "Nome alterado",
                "alterado@email.com",
                "login alterado",
                "password alterado",
                UserLevel.CUSTOMER,
                null,
                new Address("Rua 2","1234","N/A alterado",
                        "11235-900","Bairro alterado","Cidade alterado",
                        "RJ","Argentinha"),
                user.getCreatedAt(),
                UPDATED_AT
        );

        UserOutputDTO convertedOutputDTO = new UserOutputDTO(
                mockUpdatedUser.getId(),
                mockUpdatedUser.getName(),
                mockUpdatedUser.getEmail(),
                mockUpdatedUser.getLogin(),
                mockUpdatedUser.getUserLevel(),
                null,
                mockUpdatedUser.getAddress(),
                mockUpdatedUser.getCreatedAt(),
                mockUpdatedUser.getUpdatedAt());

        when(gateway.existById(any(Long.class))).thenReturn(false);

        UserUpdateInputDTO inputDTO = new UserUpdateInputDTO("User 1","user@user.com.br",
                "user", UserLevel.CUSTOMER, new Address("Rua 2","1234","N/A alterado",
                "11235-900","Bairro alterado","Cidade alterado",
                "RJ","Argentinha"));

        assertThrows(UserNotFoundException.class, () -> updateUserUseCase.execute(inputDTO, 1L));

    }

    @Test
    @DisplayName("Deve lançar exceção caso exista outro usuário com o email informado")
    void shouldThrowAnExceptionWhenThereIsAlreadyAnotherUserWithThisEmail() {
        User mockUpdatedUser = new User(
                user.getId(),
                "Nome alterado",
                "alterado@email.com",
                "login alterado",
                "password alterado",
                UserLevel.CUSTOMER,
                null,
                new Address("Rua 2","1234","N/A alterado",
                        "11235-900","Bairro alterado","Cidade alterado",
                        "RJ","Argentinha"),
                user.getCreatedAt(),
                UPDATED_AT
        );

        UserOutputDTO convertedOutputDTO = new UserOutputDTO(
                mockUpdatedUser.getId(),
                mockUpdatedUser.getName(),
                mockUpdatedUser.getEmail(),
                mockUpdatedUser.getLogin(),
                mockUpdatedUser.getUserLevel(),
                null,
                mockUpdatedUser.getAddress(),
                mockUpdatedUser.getCreatedAt(),
                mockUpdatedUser.getUpdatedAt());

        when(gateway.existById(any(Long.class))).thenReturn(true);
        when(validator.emailExists(any(String.class), any(Long.class))).thenReturn(true);

        UserUpdateInputDTO inputDTO = new UserUpdateInputDTO("User 1","user@user.com.br",
                "user", UserLevel.CUSTOMER, new Address("Rua 2","1234","N/A alterado",
                "11235-900","Bairro alterado","Cidade alterado",
                "RJ","Argentinha"));

        assertThrows(DuplicateEmailException.class, () -> updateUserUseCase.execute(inputDTO, 1L));
    }

    @Test
    @DisplayName("Deve lançar exceção caso exista outro usuário com o login informando")
    void shouldThrowAnExceptionWhenThereIsAlreadyAnotherUserWithThisLogin() {
        User mockUpdatedUser = new User(
                user.getId(),
                "Nome alterado",
                "alterado@email.com",
                "login alterado",
                "password alterado",
                UserLevel.CUSTOMER,
                null,
                new Address("Rua 2","1234","N/A alterado",
                        "11235-900","Bairro alterado","Cidade alterado",
                        "RJ","Argentinha"),
                user.getCreatedAt(),
                UPDATED_AT
        );

        UserOutputDTO convertedOutputDTO = new UserOutputDTO(
                mockUpdatedUser.getId(),
                mockUpdatedUser.getName(),
                mockUpdatedUser.getEmail(),
                mockUpdatedUser.getLogin(),
                mockUpdatedUser.getUserLevel(),
                null,
                mockUpdatedUser.getAddress(),
                mockUpdatedUser.getCreatedAt(),
                mockUpdatedUser.getUpdatedAt());

        when(gateway.existById(any(Long.class))).thenReturn(true);
        when(validator.emailExists(any(String.class), any(Long.class))).thenReturn(false);
        when(validator.loginExists(any(String.class), any(Long.class))).thenReturn(true);

        UserUpdateInputDTO inputDTO = new UserUpdateInputDTO("User 1","user@user.com.br",
                "user", UserLevel.CUSTOMER, new Address("Rua 2","1234","N/A alterado",
                "11235-900","Bairro alterado","Cidade alterado",
                "RJ","Argentinha"));

        assertThrows(DuplicateLoginException.class, () -> updateUserUseCase.execute(inputDTO, 1L));
    }
}
