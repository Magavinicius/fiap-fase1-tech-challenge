package com.github.techChallenge.infrastructure.repositories.restaurant;

import com.github.techChallenge.application.exceptions.*;
import com.github.techChallenge.domain.restaurant.Restaurant;
import com.github.techChallenge.domain.restaurantCategory.RestaurantCategory;
import com.github.techChallenge.domain.user.*;
import com.github.techChallenge.infrastructure.entities.restaurantCategory.RestaurantCategoryEntity;
import com.github.techChallenge.infrastructure.entities.user.UserEntity;
import com.github.techChallenge.infrastructure.repositories.restaurantCategory.RestaurantCategoryRepository;
import com.github.techChallenge.infrastructure.repositories.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test") @Transactional
class RestaurantRepositoryIntegrationTest {
    @Autowired RestaurantRepositoryGateway gateway;
    @Autowired RestaurantRepository restaurants;
    @Autowired UserRepository users;
    @Autowired RestaurantCategoryRepository categories;
    private UserEntity owner;
    private RestaurantCategoryEntity category;
    private final Address address = new Address("Rua A", "10", null, "01000-000", "Centro", "São Paulo", "SP", "Brasil");

    @BeforeEach void seedOwnerAndCuisine() {
        String suffix = UUID.randomUUID().toString();
        owner = users.saveAndFlush(new UserEntity(User.create("Proprietário", suffix + "@email.com", "owner." + suffix,
                "Senha@123", UserLevel.OWNER, address)));
        category = categories.saveAndFlush(new RestaurantCategoryEntity(RestaurantCategory.create("Cozinha " + suffix)));
    }

    private Restaurant data(String name) { return Restaurant.create(name, address, category.getId(), "11h às 23h", owner.getId()); }

    @Test void shouldPersistAndFindRestaurantWithOwnerCuisineAddressAndHours() {
        var saved = gateway.create(data("Restaurante " + UUID.randomUUID()));
        var found = gateway.findByID(saved.getId());
        assertNotNull(saved.getId());
        assertEquals(saved.getName(), found.getName());
        assertEquals(owner.getId(), found.getOwnerId());
        assertEquals(category.getId(), found.getCategoryId());
        assertEquals(address, found.getAddress());
        assertEquals("11h às 23h", found.getOpeningHours());
        assertNotNull(found.getCreatedAt());
    }

    @Test void shouldUpdateWithoutChangingTheOwnerOrCreationDate() {
        var saved = gateway.create(data("Antes " + UUID.randomUUID()));
        var createdAt = saved.getCreatedAt();
        saved.update("Depois " + UUID.randomUUID(), address, category.getId(), "12h às 22h");
        var updated = gateway.update(saved, saved.getId());
        assertEquals(saved.getId(), updated.getId());
        assertEquals(owner.getId(), updated.getOwnerId());
        assertEquals(createdAt, updated.getCreatedAt());
        assertEquals("12h às 22h", updated.getOpeningHours());
        assertEquals(saved.getName(), gateway.findByID(saved.getId()).getName());
    }

    @Test void shouldEnforceUniqueNamesInTheDatabaseIgnoringCase() {
        String name = "Duplicado " + UUID.randomUUID();
        gateway.create(data(name));
        assertTrue(gateway.existsByName(name.toUpperCase()));
        assertThrows(DataIntegrityViolationException.class, () -> gateway.create(data(name.toUpperCase())));
    }

    @Test void shouldExcludeTheCurrentRestaurantFromDuplicateNameChecks() {
        var saved = gateway.create(data("Nome " + UUID.randomUUID()));
        assertFalse(gateway.existsByNameAndIdNot(saved.getName(), saved.getId()));
        assertTrue(gateway.existsByNameAndIdNot(saved.getName(), Long.MAX_VALUE));
    }

    @Test void shouldFilterAndPaginateByCuisine() {
        var first = gateway.create(data("A " + UUID.randomUUID()));
        gateway.create(data("B " + UUID.randomUUID()));
        var anotherCategory = categories.saveAndFlush(new RestaurantCategoryEntity(RestaurantCategory.create("Outra " + UUID.randomUUID())));
        gateway.create(Restaurant.create("C " + UUID.randomUUID(), address, anotherCategory.getId(), "12h às 22h", owner.getId()));
        var page = gateway.list(category.getId(), 0, 1);
        assertEquals(2, page.getTotalElements());
        assertEquals(1, page.getContent().size());
        assertEquals(first.getId(), page.getContent().getFirst().getId());
        assertEquals(1, gateway.list(category.getId(), 1, 1).getContent().size());
        assertTrue(gateway.list(null, 0, 100).getTotalElements() >= 3);
    }

    @Test void shouldLockRestaurantInsideTheApplicationTransaction() {
        var saved = gateway.create(data("Bloqueio " + UUID.randomUUID()));
        assertEquals(saved.getId(), gateway.lockById(saved.getId()).getId());
    }

    @Test void shouldDeleteAndThenReturnNotFound() {
        var saved = gateway.create(data("Excluir " + UUID.randomUUID()));
        gateway.delete(saved.getId());
        assertThrows(RestaurantNotFoundException.class, () -> gateway.findByID(saved.getId()));
    }

    @Test void shouldRejectMissingRestaurantInEveryWritePath() {
        assertThrows(RestaurantNotFoundException.class, () -> gateway.findByID(Long.MAX_VALUE));
        assertThrows(RestaurantNotFoundException.class, () -> gateway.update(data("Ausente"), Long.MAX_VALUE));
        assertThrows(RestaurantNotFoundException.class, () -> gateway.delete(Long.MAX_VALUE));
        assertThrows(RestaurantNotFoundException.class, () -> gateway.lockById(Long.MAX_VALUE));
    }

    @Test void shouldRequireAnExistingOwnerAndCuisine() {
        assertThrows(UserNotFoundException.class, () -> gateway.create(Restaurant.create("Ausente", address, category.getId(), "11h", Long.MAX_VALUE)));
        assertThrows(RestaurantCategoryNotFoundException.class, () -> gateway.create(Restaurant.create("Ausente", address, Integer.MAX_VALUE, "11h", owner.getId())));
    }
}
