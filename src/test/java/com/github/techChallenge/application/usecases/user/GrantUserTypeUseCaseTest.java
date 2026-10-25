package com.github.techChallenge.application.usecases.user;

import com.github.techChallenge.application.exceptions.UserNotFoundException;
import com.github.techChallenge.application.exceptions.UserTypeNotFoundException;
import com.github.techChallenge.application.gateways.user.IUserGateway;
import com.github.techChallenge.application.validators.UserTypeValidator;
import com.github.techChallenge.application.validators.UserValidator;
import com.github.techChallenge.domain.user.Address;
import com.github.techChallenge.domain.user.IUserMapper;
import com.github.techChallenge.domain.user.User;
import com.github.techChallenge.domain.user.UserLevel;
import com.github.techChallenge.domain.user.dto.GrantUserTypeInputDTO;
import com.github.techChallenge.domain.user.dto.UserOutputDTO;
import com.github.techChallenge.domain.usertype.UserType;
import com.github.techChallenge.domain.usertype.dto.UserTypeOutputDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrantUserTypeUseCaseTest {

    @Mock
    private IUserGateway gateway;

    @Mock
    private IUserMapper mapper;

    @Mock
    private UserValidator userValidator;

    @Mock
    private UserTypeValidator userTypeValidator;

    @InjectMocks
    private GrantUserTypeUseCase useCase;

    @Test
    void shouldGrantTypeWhenUserAndTypeExist() {
        GrantUserTypeInputDTO input = new GrantUserTypeInputDTO(1L, 2);
        LocalDateTime now = LocalDateTime.of(2026, 10, 24, 12, 0);
        Address address = new Address(
                "Rua A", "10", null, "01000-000",
                "Centro", "São Paulo", "SP", "Brasil"
        );
        User user = new User(
                1L, "Vinicius", "vinicius@email.com", "vinicius",
                "hash", UserLevel.CUSTOMER, new UserType(2, "Cliente"),
                address, now, now
        );
        UserOutputDTO expected = new UserOutputDTO(
                1L, user.getName(), user.getEmail(), user.getLogin(),
                user.getUserLevel(), new UserTypeOutputDTO(2, "Cliente"),
                address, now, now
        );
        when(userValidator.existsByID(1L)).thenReturn(true);
        when(userTypeValidator.existsByID(2)).thenReturn(true);
        when(gateway.grantUserType(input)).thenReturn(user);
        when(mapper.fromDomainToOutputDTO(user)).thenReturn(expected);

        UserOutputDTO actual = useCase.execute(input);

        assertSame(expected, actual);
        verify(userValidator).existsByID(1L);
        verify(userTypeValidator).existsByID(2);
        verify(gateway).grantUserType(input);
        verify(mapper).fromDomainToOutputDTO(user);
    }

    @Test
    void shouldRejectUnknownUserBeforeCheckingType() {
        GrantUserTypeInputDTO input = new GrantUserTypeInputDTO(99L, 2);
        when(userValidator.existsByID(99L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> useCase.execute(input));

        verify(userValidator).existsByID(99L);
        verifyNoInteractions(userTypeValidator, gateway, mapper);
    }

    @Test
    void shouldRejectUnknownTypeWithoutGrantingIt() {
        GrantUserTypeInputDTO input = new GrantUserTypeInputDTO(1L, 99);
        when(userValidator.existsByID(1L)).thenReturn(true);
        when(userTypeValidator.existsByID(99)).thenReturn(false);

        assertThrows(UserTypeNotFoundException.class, () -> useCase.execute(input));

        verify(userValidator).existsByID(1L);
        verify(userTypeValidator).existsByID(99);
        verifyNoInteractions(gateway, mapper);
    }

    @Test
    void shouldPropagateFailureDuringGrantWithoutCallingMapper() {
        GrantUserTypeInputDTO input = new GrantUserTypeInputDTO(1L, 2);
        IllegalStateException expected = new IllegalStateException("Falha na persistência");
        when(userValidator.existsByID(1L)).thenReturn(true);
        when(userTypeValidator.existsByID(2)).thenReturn(true);
        when(gateway.grantUserType(input)).thenThrow(expected);

        IllegalStateException actual = assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(input)
        );

        assertSame(expected, actual);
        verifyNoInteractions(mapper);
    }
}
