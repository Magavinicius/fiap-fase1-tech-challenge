package com.github.techChallenge.application.usecases.user;

import com.github.techChallenge.application.gateways.user.IUserGateway;
import com.github.techChallenge.domain.user.Address;
import com.github.techChallenge.domain.user.IUserMapper;
import com.github.techChallenge.domain.user.User;
import com.github.techChallenge.domain.user.UserLevel;
import com.github.techChallenge.domain.user.dto.UserOutputDTO;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListUserUseCaseTest {

    @Mock
    private IUserGateway gateway;

    @Mock
    private IUserMapper mapper;

    @InjectMocks
    private ListUserUseCase useCase;

    @ParameterizedTest(name = "Listagem com filtro = {0}")
    @ValueSource(booleans = {false, true})
    void shouldReturnMappedPageAndForwardPagination(boolean filtered) {
        LocalDateTime now = LocalDateTime.of(2026, 10, 24, 12, 0);
        Address address = new Address(
                "Rua A", "10", null, "01000-000",
                "Centro", "São Paulo", "SP", "Brasil"
        );
        User user = new User(
                1L, "Vinicius", "vinicius@email.com", "vinicius",
                "hash", UserLevel.CUSTOMER, null, address, now, now
        );
        UserOutputDTO dto = new UserOutputDTO(
                1L, user.getName(), user.getEmail(), user.getLogin(),
                user.getUserLevel(), null, address, now, now
        );
        PageRequest pageable = PageRequest.of(2, 5);
        Page<User> users = new PageImpl<>(List.of(user), pageable, 11);
        Page<UserOutputDTO> expected = new PageImpl<>(List.of(dto), pageable, 11);

        if (filtered) {
            when(gateway.listByName("Vini", 2, 5)).thenReturn(users);
        } else {
            when(gateway.list(2, 5)).thenReturn(users);
        }
        when(mapper.fromDomainPageToOutputDTOPage(users)).thenReturn(expected);

        Page<UserOutputDTO> actual = filtered
                ? useCase.execute("Vini", 2, 5)
                : useCase.execute(2, 5);

        assertSame(expected, actual);
        if (filtered) {
            verify(gateway).listByName("Vini", 2, 5);
        } else {
            verify(gateway).list(2, 5);
        }
        verify(mapper).fromDomainPageToOutputDTOPage(users);
        verifyNoMoreInteractions(gateway);
    }

    @ParameterizedTest(name = "Página vazia com filtro = {0}")
    @ValueSource(booleans = {false, true})
    void shouldReturnEmptyPage(boolean filtered) {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<User> users = Page.empty(pageable);
        Page<UserOutputDTO> expected = Page.empty(pageable);

        if (filtered) {
            when(gateway.listByName("Inexistente", 0, 10)).thenReturn(users);
        } else {
            when(gateway.list(0, 10)).thenReturn(users);
        }
        when(mapper.fromDomainPageToOutputDTOPage(users)).thenReturn(expected);

        Page<UserOutputDTO> actual = filtered
                ? useCase.execute("Inexistente", 0, 10)
                : useCase.execute(0, 10);

        assertSame(expected, actual);
        assertTrue(actual.isEmpty());
        verify(mapper).fromDomainPageToOutputDTOPage(users);
    }

    @ParameterizedTest(name = "Erro do gateway com filtro = {0}")
    @ValueSource(booleans = {false, true})
    void shouldPropagateGatewayFailure(boolean filtered) {
        IllegalStateException expected = new IllegalStateException("Falha na consulta");
        if (filtered) {
            when(gateway.listByName("Vini", 0, 10)).thenThrow(expected);
        } else {
            when(gateway.list(0, 10)).thenThrow(expected);
        }

        IllegalStateException actual = assertThrows(
                IllegalStateException.class,
                () -> {
                    if (filtered) {
                        useCase.execute("Vini", 0, 10);
                    } else {
                        useCase.execute(0, 10);
                    }
                }
        );

        assertSame(expected, actual);
        verifyNoInteractions(mapper);
    }
}
