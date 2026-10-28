package com.github.techChallenge.domain.menuItem;

import com.github.techChallenge.domain.validation.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class MenuItemValidationTest {
    @ParameterizedTest @MethodSource("invalidItems")
    void shouldValidateWithoutDependingOnDTOAnnotations(String field, String name, String description,
            BigDecimal price, Boolean dineInOnly, String photo, Long restaurantId) {
        var error = assertThrows(DomainValidationException.class,
                () -> MenuItem.create(name, description, price, dineInOnly, photo, restaurantId));
        assertTrue(error.getViolations().containsKey(field));
    }

    static Stream<Arguments> invalidItems() {
        return Stream.of(
                invalid("name", null, "D", "1.00", false, "/foto", 1L),
                invalid("name", " ", "D", "1.00", false, "/foto", 1L),
                invalid("name", "N".repeat(101), "D", "1.00", false, "/foto", 1L),
                invalid("description", "N", null, "1.00", false, "/foto", 1L),
                invalid("description", "N", " ", "1.00", false, "/foto", 1L),
                invalid("description", "N", "D".repeat(256), "1.00", false, "/foto", 1L),
                invalid("description", "N", " " + "D".repeat(255), "1.00", false, "/foto", 1L),
                invalid("price", "N", "D", null, false, "/foto", 1L),
                invalid("price", "N", "D", "0", false, "/foto", 1L),
                invalid("price", "N", "D", "-1", false, "/foto", 1L),
                invalid("price", "N", "D", "1.001", false, "/foto", 1L),
                invalid("price", "N", "D", "10000000000", false, "/foto", 1L),
                invalid("dineInOnly", "N", "D", "1.00", null, "/foto", 1L),
                invalid("photoPath", "N", "D", "1.00", false, null, 1L),
                invalid("photoPath", "N", "D", "1.00", false, " ", 1L),
                invalid("photoPath", "N", "D", "1.00", false, "P".repeat(256), 1L),
                invalid("photoPath", "N", "D", "1.00", false, " " + "P".repeat(255), 1L),
                invalid("restaurantId", "N", "D", "1.00", false, "/foto", null),
                invalid("restaurantId", "N", "D", "1.00", false, "/foto", 0L));
    }

    private static Arguments invalid(String field, String name, String description, String price,
                                     Boolean dineInOnly, String photo, Long restaurantId) {
        return Arguments.of(field, name, description, price == null ? null : new BigDecimal(price), dineInOnly, photo, restaurantId);
    }

    @Test void shouldReportAllMissingFields() {
        var error = assertThrows(DomainValidationException.class, () -> MenuItem.create(null, null, null, null, null, null));
        assertEquals(Set.of("name", "description", "price", "dineInOnly", "photoPath", "restaurantId"), error.getViolations().keySet());
    }

    @Test void shouldNotPartiallyChangeAnItemOnInvalidUpdate() {
        var item = new MenuItem(3L, "Original", "Descrição", new BigDecimal("1.00"), false, "/foto", 7L);
        assertThrows(DomainValidationException.class, () -> item.update("Novo", "Nova descrição", BigDecimal.ZERO, true, "/nova"));
        assertAll(() -> assertEquals(3L, item.getId()), () -> assertEquals(7L, item.getRestaurantId()),
                () -> assertEquals("Original", item.getName()), () -> assertEquals("Descrição", item.getDescription()),
                () -> assertEquals(new BigDecimal("1.00"), item.getPrice()), () -> assertFalse(item.isDineInOnly()),
                () -> assertEquals("/foto", item.getPhotoPath()));
    }

    @Test void shouldAcceptFalseDineInOnlyAndMaximumLimits() {
        var item = MenuItem.create("N".repeat(100), "D".repeat(255), new BigDecimal("9999999999.99"), false, "P".repeat(255), 7L);
        assertFalse(item.isDineInOnly());
        assertEquals(100, item.getName().length());
    }

    @Test void shouldAcceptTrailingZerosWithoutLosingMoneyPrecision() {
        assertDoesNotThrow(() -> MenuItem.create("Item", "Descrição", new BigDecimal("1.2300"), true, "/foto", 7L));
    }
}
