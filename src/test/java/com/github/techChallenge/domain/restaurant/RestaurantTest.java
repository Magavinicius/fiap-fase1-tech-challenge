package com.github.techChallenge.domain.restaurant;

import com.github.techChallenge.domain.user.Address;
import com.github.techChallenge.domain.validation.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class RestaurantTest {
    private static Address address() { return new Address("Rua A", "10", null, "01000-000", "Centro", "São Paulo", "SP", "Brasil"); }

    @Test void shouldCreateRestaurantLinkedToAnOwnerAndCuisine() {
        Restaurant restaurant = Restaurant.create("  Restaurante  ", address(), 2, "  11h às 23h  ", 7L);
        assertAll(() -> assertNull(restaurant.getId()), () -> assertEquals("Restaurante", restaurant.getName()),
                () -> assertEquals(address(), restaurant.getAddress()), () -> assertEquals(2, restaurant.getCategoryId()),
                () -> assertEquals(7L, restaurant.getOwnerId()), () -> assertEquals("11h às 23h", restaurant.getOpeningHours()),
                () -> assertNotNull(restaurant.getCreatedAt()), () -> assertEquals(restaurant.getCreatedAt(), restaurant.getUpdatedAt()));
    }

    @ParameterizedTest @MethodSource("invalidData")
    void shouldRejectInvalidCreation(String field, String name, Address address, Integer category, String hours, Long owner) {
        var error = assertThrows(DomainValidationException.class, () -> Restaurant.create(name, address, category, hours, owner));
        assertTrue(error.getViolations().containsKey(field));
    }

    static Stream<Arguments> invalidData() {
        return Stream.of(
                Arguments.of("name", null, address(), 1, "11h às 23h", 1L),
                Arguments.of("name", " ", address(), 1, "11h às 23h", 1L),
                Arguments.of("name", "N".repeat(101), address(), 1, "11h às 23h", 1L),
                Arguments.of("address", "R", null, 1, "11h às 23h", 1L),
                Arguments.of("categoryId", "R", address(), null, "11h às 23h", 1L),
                Arguments.of("categoryId", "R", address(), 0, "11h às 23h", 1L),
                Arguments.of("openingHours", "R", address(), 1, null, 1L),
                Arguments.of("openingHours", "R", address(), 1, " ", 1L),
                Arguments.of("openingHours", "R", address(), 1, "H".repeat(256), 1L),
                Arguments.of("ownerId", "R", address(), 1, "11h às 23h", null),
                Arguments.of("ownerId", "R", address(), 1, "11h às 23h", -1L));
    }

    @Test void shouldPreserveIdentityOwnerAndCreationDateOnUpdate() {
        var created = LocalDateTime.of(2020, 1, 1, 12, 0);
        var restaurant = new Restaurant(9L, "Antigo", address(), 1, "11h às 23h", 7L, created, created);
        restaurant.update(" Novo ", address(), 2, " 12h às 22h ");
        assertAll(() -> assertEquals(9L, restaurant.getId()), () -> assertEquals(7L, restaurant.getOwnerId()),
                () -> assertEquals(created, restaurant.getCreatedAt()), () -> assertTrue(restaurant.getUpdatedAt().isAfter(created)),
                () -> assertEquals("Novo", restaurant.getName()), () -> assertEquals(2, restaurant.getCategoryId()),
                () -> assertEquals("12h às 22h", restaurant.getOpeningHours()));
    }

    @Test void shouldReportAllViolationsWithoutPartiallyChangingTheRestaurant() {
        var restaurant = Restaurant.create("Original", address(), 1, "11h às 23h", 7L);
        var updatedAt = restaurant.getUpdatedAt();
        var error = assertThrows(DomainValidationException.class, () -> restaurant.update("", null, null, ""));
        assertEquals(Set.of("name", "address", "categoryId", "openingHours"), error.getViolations().keySet());
        assertEquals("Original", restaurant.getName());
        assertEquals(address(), restaurant.getAddress());
        assertEquals(1, restaurant.getCategoryId());
        assertEquals("11h às 23h", restaurant.getOpeningHours());
        assertEquals(updatedAt, restaurant.getUpdatedAt());
    }

    @Test void shouldValidateAddressInsideTheDomain() {
        var invalid = new Address("", "", "C".repeat(256), "123", "", "", "sp", "");
        var error = assertThrows(DomainValidationException.class, () -> Restaurant.create("R", invalid, 1, "11h às 23h", 1L));
        assertEquals(Set.of("address.address", "address.number", "address.complement", "address.zipCode",
                "address.neighborhood", "address.city", "address.state", "address.country"), error.getViolations().keySet());
    }

    @Test void shouldAcceptMaximumLengthsAndPostalCodeWithoutHyphen() {
        var address = new Address("Rua", "S/N", "Casa", "01000000", "Centro", "São Paulo", "SP", "Brasil");
        assertDoesNotThrow(() -> Restaurant.create("N".repeat(100), address, 1, "H".repeat(255), 1L));
    }

    @Test void shouldRejectAnAddressThatWouldExceedTheDatabaseColumnEvenWithPadding() {
        var padded = new Address(" " + "A".repeat(255), "10", null, "01000-000", "Centro", "São Paulo", "SP", "Brasil");
        var error = assertThrows(DomainValidationException.class, () -> Restaurant.create("R", padded, 1, "11h", 1L));
        assertTrue(error.getViolations().containsKey("address.address"));
    }
}
