package com.github.techChallenge.application.gateways.menuItem;

import com.github.techChallenge.application.repositories.IMenuItemRepository;
import com.github.techChallenge.domain.menuItem.MenuItem;
import org.springframework.data.domain.Page;

public class MenuItemGateway implements IMenuItemGateway {

    private final IMenuItemRepository repository;

    public MenuItemGateway(IMenuItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public MenuItem create(MenuItem menuItem) {
        return this.repository.create(menuItem);
    }

    @Override
    public MenuItem update(MenuItem menuItem, Long id) {
        return this.repository.update(menuItem, id);
    }

    @Override
    public MenuItem find(Long id) {
        return this.repository.findByID(id);
    }

    @Override
    public Page<MenuItem> listByRestaurant(Long restaurantId, Integer page, Integer offset) {
        return this.repository.listByRestaurant(restaurantId, page, offset);
    }

    @Override
    public void delete(Long id) {
        this.repository.delete(id);
    }

    @Override
    public boolean nameExists(String name, Long restaurantId) {
        return this.repository.existsByNameAndRestaurantId(name, restaurantId);
    }

    @Override
    public boolean nameExists(String name, Long restaurantId, Long id) {
        return this.repository.existsByNameAndRestaurantIdAndIdNot(name, restaurantId, id);
    }

    @Override
    public boolean existById(Long id) {
        return this.repository.existsById(id);
    }

    @Override
    public boolean existsByRestaurant(Long restaurantId) {
        return this.repository.existsByRestaurantId(restaurantId);
    }
}

