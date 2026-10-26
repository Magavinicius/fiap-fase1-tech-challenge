package com.github.techChallenge.application.usecases.user;

import com.github.techChallenge.application.exceptions.UserNotFoundException;
import com.github.techChallenge.application.gateways.user.IUserGateway;
import com.github.techChallenge.domain.user.IUserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteUserUseCaseTest {

    @Mock
    private IUserGateway gateway;

    @Mock
    private IUserMapper mapper;

    @InjectMocks
    private DeleteUserUseCase useCase;

    @Test
    void shouldDeleteAnExistingUser() {
        assertDoesNotThrow(() -> useCase.execute(1L));

        verify(gateway).delete(1L);
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldPropagateUserNotFound() {
        UserNotFoundException expected = new UserNotFoundException(99L);
        doThrow(expected).when(gateway).delete(99L);

        UserNotFoundException actual = assertThrows(
                UserNotFoundException.class,
                () -> useCase.execute(99L)
        );

        assertSame(expected, actual);
        verify(gateway).delete(99L);
        verifyNoInteractions(mapper);
    }
}
