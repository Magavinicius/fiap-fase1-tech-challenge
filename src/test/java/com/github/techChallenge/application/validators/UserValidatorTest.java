package com.github.techChallenge.application.validators;

import com.github.techChallenge.application.exceptions.InvalidPasswordException;
import com.github.techChallenge.application.gateways.user.IUserGateway;
import com.github.techChallenge.domain.user.IUserMapper;
import com.github.techChallenge.domain.user.dto.UserAuthInputDTO;
import com.github.techChallenge.infrastructure.security.ISecurityConfig;
import com.github.techChallenge.shared.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserValidatorTest {

    private static final String LOGIN = "vinicius";
    private static final String RAW_PASSWORD = "Senha@123";
    private static final String STORED_HASH = "hash-armazenado-no-banco";

    @Mock
    private IUserGateway gateway;

    @Mock
    private IUserMapper mapper;

    @Mock
    private ISecurityConfig security;

    @InjectMocks
    private UserValidator validator;

    @Test
    void shouldAuthenticateValidCredentials() {
        when(gateway.loginExists(LOGIN)).thenReturn(true);
        when(gateway.getEncryptPasswordByLogin(LOGIN)).thenReturn(STORED_HASH);
        when(security.passwordValidate(RAW_PASSWORD, STORED_HASH)).thenReturn(true);

        assertTrue(validator.authUser(new UserAuthInputDTO(LOGIN, RAW_PASSWORD)));

        verify(gateway).getEncryptPasswordByLogin(LOGIN);
        verify(security).passwordValidate(RAW_PASSWORD, STORED_HASH);
        verifyNoInteractions(mapper);
    }

    @Test
    void shouldRejectUnknownLoginWithoutLookingUpPassword() {
        when(gateway.loginExists(LOGIN)).thenReturn(false);

        assertThrows(
                UnauthorizedException.class,
                () -> validator.authUser(new UserAuthInputDTO(LOGIN, RAW_PASSWORD))
        );

        verify(gateway).loginExists(LOGIN);
        verifyNoMoreInteractions(gateway);
        verifyNoInteractions(security, mapper);
    }

    @Test
    void shouldRejectIncorrectPassword() {
        when(gateway.loginExists(LOGIN)).thenReturn(true);
        when(gateway.getEncryptPasswordByLogin(LOGIN)).thenReturn(STORED_HASH);
        when(security.passwordValidate(RAW_PASSWORD, STORED_HASH)).thenReturn(false);

        assertThrows(
                UnauthorizedException.class,
                () -> validator.authUser(new UserAuthInputDTO(LOGIN, RAW_PASSWORD))
        );

        verify(security).passwordValidate(RAW_PASSWORD, STORED_HASH);
    }

    @ParameterizedTest
    @MethodSource("invalidPasswords")
    void shouldRejectMissingOrBlankPasswords(String rawPassword, String storedHash) {
        when(gateway.loginExists(LOGIN)).thenReturn(true);
        when(gateway.getEncryptPasswordByLogin(LOGIN)).thenReturn(storedHash);

        assertThrows(
                InvalidPasswordException.class,
                () -> validator.authUser(new UserAuthInputDTO(LOGIN, rawPassword))
        );

        verifyNoInteractions(security, mapper);
    }

    static Stream<Arguments> invalidPasswords() {
        return Stream.of(
                Arguments.of(null, STORED_HASH),
                Arguments.of("", STORED_HASH),
                Arguments.of("   ", STORED_HASH),
                Arguments.of(RAW_PASSWORD, null),
                Arguments.of(RAW_PASSWORD, ""),
                Arguments.of(RAW_PASSWORD, "   ")
        );
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldCheckEmailWithoutID(boolean expected) {
        String email = "vinicius@email.com";
        when(gateway.emailExists(email)).thenReturn(expected);

        assertEquals(expected, validator.emailExists(email));

        verify(gateway).emailExists(email);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldCheckEmailIgnoringCurrentID(boolean expected) {
        String email = "vinicius@email.com";
        when(gateway.emailExists(email, 1L)).thenReturn(expected);

        assertEquals(expected, validator.emailExists(email, 1L));

        verify(gateway).emailExists(email, 1L);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldCheckLoginWithoutID(boolean expected) {
        when(gateway.loginExists(LOGIN)).thenReturn(expected);

        assertEquals(expected, validator.loginExists(LOGIN));

        verify(gateway).loginExists(LOGIN);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldCheckLoginIgnoringCurrentID(boolean expected) {
        when(gateway.loginExists(LOGIN, 1L)).thenReturn(expected);

        assertEquals(expected, validator.loginExists(LOGIN, 1L));

        verify(gateway).loginExists(LOGIN, 1L);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldCheckUserID(boolean expected) {
        when(gateway.existById(1L)).thenReturn(expected);

        assertEquals(expected, validator.existsByID(1L));

        verify(gateway).existById(1L);
    }
}
