package com.github.techChallenge.application.repositories;

import com.github.techChallenge.domain.menuItem.MenuItem;
import org.springframework.data.domain.Page;

public interface IMenuItemRepository {
    MenuItem create(MenuItem menuItem);
    MenuItem update(MenuItem menuItem, Long id);
    MenuItem findByID(Long id);
    Page<MenuItem> listByRestaurant(Long restaurantId, int page, int offset);
    void delete(Long id);
    boolean existsByNameAndRestaurantId(String name, Long restaurantId);
    boolean existsByNameAndRestaurantIdAndIdNot(String name, Long restaurantId, Long id);
    boolean existsById(Long id);
    boolean existsByRestaurantId(Long restaurantId);
}

