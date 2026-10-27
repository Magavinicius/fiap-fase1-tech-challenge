package com.github.techChallenge.application.gateways.menuItem;

import com.github.techChallenge.domain.menuItem.MenuItem;
import org.springframework.data.domain.Page;

public interface IMenuItemGateway {
    MenuItem create(MenuItem menuItem);
    MenuItem update(MenuItem menuItem, Long id);
    MenuItem find(Long id);
    Page<MenuItem> listByRestaurant(Long restaurantId, Integer page, Integer offset);
    void delete(Long id);
    boolean nameExists(String name, Long restaurantId);
    boolean nameExists(String name, Long restaurantId, Long id);
    boolean existById(Long id);
    boolean existsByRestaurant(Long restaurantId);
}

