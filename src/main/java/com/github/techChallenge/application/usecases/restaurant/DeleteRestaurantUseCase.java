package com.github.techChallenge.application.usecases.restaurant;

import com.github.techChallenge.application.exceptions.RestaurantHasMenuItemsException;
import com.github.techChallenge.application.gateways.menuItem.IMenuItemGateway;
import com.github.techChallenge.application.gateways.restaurant.IRestaurantGateway;
import com.github.techChallenge.application.validators.RestaurantAccessPolicy;
import org.springframework.transaction.annotation.Transactional;

public class DeleteRestaurantUseCase {
    private final IRestaurantGateway gateway;
    private final IMenuItemGateway menuItems;
    private final RestaurantAccessPolicy access;
    public DeleteRestaurantUseCase(IRestaurantGateway gateway, IMenuItemGateway menuItems, RestaurantAccessPolicy access) {
        this.gateway = gateway; this.menuItems = menuItems; this.access = access;
    }
    @Transactional
    public void execute(Long id, Long authenticatedUserId) {
        access.requireOwner(id, authenticatedUserId);
        if (menuItems.existsByRestaurant(id)) throw new RestaurantHasMenuItemsException();
        gateway.delete(id);
    }
}
