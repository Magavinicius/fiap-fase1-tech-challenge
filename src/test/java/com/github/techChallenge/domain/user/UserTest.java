package com.github.techChallenge.domain.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private static final LocalDateTime INITIAL_TIMESTAMP =
            LocalDateTime.of(2020, 1, 1, 12, 0);

    private User user;

    @BeforeEach
    void setUp() {
        Address address = new Address("Rua 1","123","N/A",
                "11235-800","Bairro","Cidade",
                "UF","Brasil");

        this.user = new User(
                null, "User 1", "user@user.com.br", "user", "password",
                UserLevel.CUSTOMER, null, address,
                INITIAL_TIMESTAMP, INITIAL_TIMESTAMP
        );
    }

    @Test
    @DisplayName("Deve criar um novo usuário")
    void shouldCreateANewUser() {
        user = User.create(
                "User 1", "user@user.com.br", "user", "password",
                UserLevel.CUSTOMER, user.getAddress()
        );

        assertNull(user.getId());
        assertEquals("User 1", user.getName());
        assertEquals("user@user.com.br", user.getEmail());
        assertEquals("user", user.getLogin());
        assertEquals("password", user.getPassword());
        assertEquals("Rua 1", user.getAddress().address());
        assertEquals("123", user.getAddress().number());
        assertEquals("N/A", user.getAddress().complement());
        assertEquals("11235-800", user.getAddress().zipCode());
        assertEquals("Bairro", user.getAddress().neighborhood());
        assertEquals("Cidade", user.getAddress().city());
        assertEquals("UF", user.getAddress().state());
        assertEquals("Brasil", user.getAddress().country());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals(user.getCreatedAt(), user.getUpdatedAt());
    }

    @Test
    @DisplayName("Deve lançar exceção por dados inválidos na criação")
    void shouldThrowAnExceptionDueToInvalidDataDuringCreation() {

        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> User.create(
                        "",
                        "user@user.com.br",
                        "user",
                        "password",
                        UserLevel.CUSTOMER,
                        user.getAddress()
                )
        );

        assertEquals(
                "O nome é obrigatório.",
                exception.getViolations().get("name")
        );
    }

    @Test
    @DisplayName("Deve atualizar os dados de um usuário existente")
    void shouldUpdateAnExistingUser() {
        //Updating this user
        Address address = new Address("Rua 2","1234","N/A alterado",
                "11235-900","Bairro alterado","Cidade alterado",
                "RJ","Argentinha");

        user.update("User 2", "user_alterado@user.com.br", "user_alterado", UserLevel.CUSTOMER, address);

        assertNull(user.getId());
        assertEquals("User 2", user.getName());
        assertEquals("user_alterado@user.com.br", user.getEmail());
        assertEquals("user_alterado", user.getLogin());
        assertEquals("password", user.getPassword());
        assertEquals("Rua 2", user.getAddress().address());
        assertEquals("1234", user.getAddress().number());
        assertEquals("N/A alterado", user.getAddress().complement());
        assertEquals("11235-900", user.getAddress().zipCode());
        assertEquals("Bairro alterado", user.getAddress().neighborhood());
        assertEquals("Cidade alterado", user.getAddress().city());
        assertEquals("RJ", user.getAddress().state());
        assertEquals("Argentinha", user.getAddress().country());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals(INITIAL_TIMESTAMP, user.getCreatedAt());
        assertTrue(user.getUpdatedAt().isAfter(INITIAL_TIMESTAMP));
    }

    @Test
    @DisplayName("Deve lançar uma excepção devido a erros de validação durante update")
    void shouldThrowAnExceptionDueToInvalidDataDuringUpdate() {
        LocalDateTime originalCreatedAt = user.getCreatedAt();
        LocalDateTime originalUpdatedAt = user.getUpdatedAt();
        Address originalAddress = user.getAddress();

        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> user.update("", "email-invalido", "abc", null, null)
        );

        assertEquals(
                Set.of("name", "email", "login", "userLevel", "address"),
                exception.getViolations().keySet()
        );
        assertAll(
                () -> assertEquals("User 1", user.getName()),
                () -> assertEquals("user@user.com.br", user.getEmail()),
                () -> assertEquals("user", user.getLogin()),
                () -> assertEquals("password", user.getPassword()),
                () -> assertEquals(UserLevel.CUSTOMER, user.getUserLevel()),
                () -> assertSame(originalAddress, user.getAddress()),
                () -> assertEquals(originalCreatedAt, user.getCreatedAt()),
                () -> assertEquals(originalUpdatedAt, user.getUpdatedAt())
        );
    }

    @Test
    @DisplayName("Deve alterar a senha de um usuário")
    void shouldChangePasswordOfAUser() {
        user.changePassword("nova senha");

        assertNull(user.getId());
        assertEquals("User 1", user.getName());
        assertEquals("user@user.com.br", user.getEmail());
        assertEquals("user", user.getLogin());
        assertEquals("nova senha", user.getPassword());
        assertEquals("Rua 1", user.getAddress().address());
        assertEquals("123", user.getAddress().number());
        assertEquals("N/A", user.getAddress().complement());
        assertEquals("11235-800", user.getAddress().zipCode());
        assertEquals("Bairro", user.getAddress().neighborhood());
        assertEquals("Cidade", user.getAddress().city());
        assertEquals("UF", user.getAddress().state());
        assertEquals("Brasil", user.getAddress().country());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals(INITIAL_TIMESTAMP, user.getCreatedAt());
        assertTrue(user.getUpdatedAt().isAfter(INITIAL_TIMESTAMP));
    }



    @ParameterizedTest(name = "Campo inválido: {0}")
    @MethodSource("invalidUpdates")
    void shouldRejectInvalidUpdateFields(
            String expectedField,
            String name,
            String email,
            String login,
            UserLevel level,
            Address address
    ) {
        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> user.update(name, email, login, level, address)
        );

        assertEquals(Set.of(expectedField), exception.getViolations().keySet());
    }

    static Stream<Arguments> invalidUpdates() {
        Address address = validAddress();
        return Stream.of(
                Arguments.of("name", null, "user@email.com", "user", UserLevel.CUSTOMER, address),
                Arguments.of("name", "   ", "user@email.com", "user", UserLevel.CUSTOMER, address),
                Arguments.of("name", "N".repeat(51), "user@email.com", "user", UserLevel.CUSTOMER, address),
                Arguments.of("email", "User", null, "user", UserLevel.CUSTOMER, address),
                Arguments.of("email", "User", "   ", "user", UserLevel.CUSTOMER, address),
                Arguments.of("email", "User", "invalido", "user", UserLevel.CUSTOMER, address),
                Arguments.of("email", "User", "a".repeat(256), "user", UserLevel.CUSTOMER, address),
                Arguments.of("login", "User", "user@email.com", null, UserLevel.CUSTOMER, address),
                Arguments.of("login", "User", "user@email.com", "   ", UserLevel.CUSTOMER, address),
                Arguments.of("login", "User", "user@email.com", "abc", UserLevel.CUSTOMER, address),
                Arguments.of("login", "User", "user@email.com", "u".repeat(101), UserLevel.CUSTOMER, address),
                Arguments.of("userLevel", "User", "user@email.com", "user", null, address),
                Arguments.of("address", "User", "user@email.com", "user", UserLevel.CUSTOMER, null)
        );
    }

    @Test
    void shouldReportEveryInvalidAddressFieldDuringUpdate() {
        Address invalidAddress = new Address("", "", null, "123", "", "", "sp", "");

        InvalidUserDataException exception = assertThrows(
                InvalidUserDataException.class,
                () -> user.update("User", "user@email.com", "user", UserLevel.CUSTOMER, invalidAddress)
        );

        assertEquals(
                Set.of("address.address", "address.number", "address.zipCode",
                        "address.neighborhood", "address.city", "address.state", "address.country"),
                exception.getViolations().keySet()
        );
    }

    @Test
    void shouldNormalizeEmailAndLoginDuringUpdate() {
        user.update("User", "  NOVO@EMAIL.COM  ", "  NOVO.LOGIN  ", UserLevel.OWNER, validAddress());

        assertEquals("novo@email.com", user.getEmail());
        assertEquals("novo.login", user.getLogin());
        assertEquals(UserLevel.OWNER, user.getUserLevel());
    }

    @Test
    void shouldKeepStoredPasswordAndIdentityDuringUpdate() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 12, 0);
        com.github.techChallenge.domain.usertype.UserType type =
                new com.github.techChallenge.domain.usertype.UserType(1, "Proprietário");
        User existing = new User(42L, "User", "user@email.com", "user", "hash",
                UserLevel.OWNER, type, validAddress(), createdAt, createdAt);

        existing.update("Novo nome", "novo@email.com", "novo.login", UserLevel.OWNER, validAddress());

        assertEquals(42L, existing.getId());
        assertEquals("hash", existing.getPassword());
        assertSame(type, existing.getType());
        assertEquals(createdAt, existing.getCreatedAt());
        assertTrue(existing.getUpdatedAt().isAfter(createdAt));
    }

    @Test
    void shouldValidateUpdateWithoutRequiringPassword() {
        User updateData = new User();

        assertDoesNotThrow(() -> updateData.update(
                "User", "user@email.com", "user", UserLevel.CUSTOMER, validAddress()
        ));
        assertNull(updateData.getPassword());
    }

    @Test
    void shouldAcceptNameAndLoginAtMaximumLengths() {
        user.update("N".repeat(50), "user@email.com", "u".repeat(100),
                UserLevel.CUSTOMER, validAddress());

        assertEquals(50, user.getName().length());
        assertEquals(100, user.getLogin().length());
    }

    private static Address validAddress() {
        return new Address("Rua A", "10", null, "01000-000",
                "Centro", "São Paulo", "SP", "Brasil");
    }
}
