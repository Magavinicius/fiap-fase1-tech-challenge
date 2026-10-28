package com.github.techChallenge.infrastructure.repositories.menuItem;

import com.github.techChallenge.infrastructure.entities.menuItem.MenuItemDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MenuItemMongoRepository extends MongoRepository<MenuItemDocument, Long> {
    Page<MenuItemDocument> findByRestaurantId(Long restaurantId, Pageable pageable);
    boolean existsByNameKeyAndRestaurantId(String nameKey, Long restaurantId);
    boolean existsByNameKeyAndRestaurantIdAndIdNot(String nameKey, Long restaurantId, Long id);
    boolean existsByRestaurantId(Long restaurantId);
}
