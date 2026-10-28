package com.github.techChallenge.infrastructure.repositories.menuItem;

import com.github.techChallenge.application.exceptions.MenuItemNotFoundException;
import com.github.techChallenge.domain.menuItem.MenuItem;
import com.github.techChallenge.infrastructure.entities.menuItem.MenuItemDocument;
import com.github.techChallenge.infrastructure.entities.menuItem.MenuItemSequence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest @ActiveProfiles("test")
class MenuItemRepositoryIntegrationTest {
    @Autowired MenuItemRepositoryGateway gateway;
    @Autowired MenuItemMongoRepository repository;
    @Autowired MongoTemplate mongo;

    @BeforeEach void prepareIsolatedTestCollection() {
        assertEquals("fiapfase2_test", mongo.getDb().getName(), "Use o banco MongoDB de testes.");
        repository.deleteAll();
        mongo.dropCollection(MenuItemSequence.class);
    }

    @AfterEach void cleanup() { repository.deleteAll(); }

    private MenuItem item(String name, Long restaurantId) {
        return MenuItem.create(name, "Descrição", new BigDecimal("29.90"), false, "/fotos/prato.png", restaurantId);
    }

    @Test void shouldPersistAllFieldsAndGenerateNumericIds() {
        var saved = gateway.create(item("Item", 7L));
        var found = gateway.findByID(saved.getId());
        assertNotNull(saved.getId());
        assertTrue(saved.getId() > 0);
        assertEquals("Item", found.getName());
        assertEquals("Descrição", found.getDescription());
        assertEquals(0, new BigDecimal("29.90").compareTo(found.getPrice()));
        assertFalse(found.isDineInOnly());
        assertEquals("/fotos/prato.png", found.getPhotoPath());
        assertEquals(7L, found.getRestaurantId());
        assertNotEquals(saved.getId(), gateway.create(item("Segundo", 7L)).getId());
    }

    @Test void shouldStorePriceAsDecimal128() {
        var saved = gateway.create(item("Decimal", 7L));
        var bson = mongo.getCollection("menu_items").find(new org.bson.Document("_id", saved.getId())).first();
        assertNotNull(bson);
        assertInstanceOf(org.bson.types.Decimal128.class, bson.get("price"));
    }

    @Test void shouldUpdateWithoutMovingTheItemToAnotherRestaurant() {
        var saved = gateway.create(item("Antes", 7L));
        saved.update("Depois", "Nova descrição", new BigDecimal("32.50"), true, "/nova.png");
        var updated = gateway.update(saved, saved.getId());
        assertEquals(saved.getId(), updated.getId());
        assertEquals(7L, updated.getRestaurantId());
        assertEquals("Depois", gateway.findByID(saved.getId()).getName());
        assertTrue(updated.isDineInOnly());
        assertEquals("/nova.png", updated.getPhotoPath());
    }

    @Test void shouldPreventDuplicateNamesWithinOneRestaurantAtDatabaseLevel() {
        gateway.create(item("Prato", 7L));
        assertThrows(DuplicateKeyException.class, () -> gateway.create(item("PRATO", 7L)));
        assertDoesNotThrow(() -> gateway.create(item("PRATO", 8L)));
        assertTrue(gateway.existsByNameAndRestaurantId(" prato ", 7L));
    }

    @Test void shouldExcludeTheCurrentItemWhenCheckingDuplicates() {
        var saved = gateway.create(item("Prato", 7L));
        assertFalse(gateway.existsByNameAndRestaurantIdAndIdNot("PRATO", 7L, saved.getId()));
        assertTrue(gateway.existsByNameAndRestaurantIdAndIdNot("PRATO", 7L, Long.MAX_VALUE));
        assertFalse(gateway.existsByNameAndRestaurantId("Prato", 8L));
    }

    @Test void shouldFilterAndPaginateByRestaurant() {
        var first = gateway.create(item("Um", 7L));
        gateway.create(item("Dois", 7L));
        gateway.create(item("Outro", 8L));
        var page = gateway.listByRestaurant(7L, 0, 1);
        assertEquals(2, page.getTotalElements());
        assertEquals(first.getId(), page.getContent().getFirst().getId());
        assertEquals(1, gateway.listByRestaurant(7L, 1, 1).getContent().size());
        assertTrue(gateway.listByRestaurant(99L, 0, 10).isEmpty());
        assertTrue(gateway.existsByRestaurantId(7L));
        assertFalse(gateway.existsByRestaurantId(99L));
    }

    @Test void shouldDeleteAndReportMissingItems() {
        var saved = gateway.create(item("Excluir", 7L));
        assertTrue(gateway.existsById(saved.getId()));
        gateway.delete(saved.getId());
        assertFalse(gateway.existsById(saved.getId()));
        assertThrows(MenuItemNotFoundException.class, () -> gateway.findByID(saved.getId()));
        assertThrows(MenuItemNotFoundException.class, () -> gateway.update(item("Novo", 7L), saved.getId()));
        assertThrows(MenuItemNotFoundException.class, () -> gateway.delete(saved.getId()));
    }

    @Test void shouldRejectAStaleConcurrentUpdate() {
        var saved = gateway.create(item("Concorrência", 7L));
        MenuItemDocument first = repository.findById(saved.getId()).orElseThrow();
        MenuItemDocument stale = repository.findById(saved.getId()).orElseThrow();
        first.update(item("Atualizado", 7L));
        repository.save(first);
        stale.update(item("Obsoleto", 7L));
        assertThrows(OptimisticLockingFailureException.class, () -> repository.save(stale));
    }
}
