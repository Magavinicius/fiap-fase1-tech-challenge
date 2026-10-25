package com.github.techChallenge.application.usecases.user;

import com.github.techChallenge.application.exceptions.UserNotFoundException;
import com.github.techChallenge.application.gateways.user.IUserGateway;
import com.github.techChallenge.domain.user.Address;
import com.github.techChallenge.domain.user.IUserMapper;
import com.github.techChallenge.domain.user.User;
import com.github.techChallenge.domain.user.UserLevel;
import com.github.techChallenge.domain.user.dto.UserOutputDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindUserUseCaseTest {

    @Mock
    private IUserGateway gateway;

    @Mock
    private IUserMapper mapper;

    @InjectMocks
    private FindUserUseCase useCase;

    @Test
    void shouldFindUserAndReturnMappedDTO() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 24, 12, 0);
        Address address = new Address(
                "Rua A", "10", null, "01000-000",
                "Centro", "São Paulo", "SP", "Brasil"
        );
        User user = new User(
                1L, "Vinicius", "vinicius@email.com", "vinicius",
                "hash", UserLevel.CUSTOMER, null, address, now, now
        );
        UserOutputDTO expected = new UserOutputDTO(
                1L, user.getName(), user.getEmail(), user.getLogin(),
                user.getUserLevel(), null, address, now, now
        );
        when(gateway.find(1L)).thenReturn(user);
        when(mapper.fromDomainToOutputDTO(user)).thenReturn(expected);

        UserOutputDTO actual = useCase.execute(1L);

        assertSame(expected, actual);
        verify(gateway).find(1L);
        verify(mapper).fromDomainToOutputDTO(user);
    }

    @Test
    void shouldPropagateUserNotFoundWithoutCallingMapper() {
        UserNotFoundException expected = new UserNotFoundException(99L);
        when(gateway.find(99L)).thenThrow(expected);

        UserNotFoundException actual = assertThrows(
                UserNotFoundException.class,
                () -> useCase.execute(99L)
        );

        assertSame(expected, actual);
        verify(gateway).find(99L);
        verifyNoInteractions(mapper);
    }
}
