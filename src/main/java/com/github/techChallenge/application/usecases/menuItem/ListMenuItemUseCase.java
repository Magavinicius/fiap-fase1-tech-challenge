package com.github.techChallenge.application.usecases.menuItem;

import com.github.techChallenge.application.gateways.menuItem.IMenuItemGateway;
import com.github.techChallenge.domain.menuItem.IMenuItemMapper;
import com.github.techChallenge.domain.menuItem.MenuItem;
import com.github.techChallenge.domain.menuItem.dto.MenuItemOutputDTO;
import org.springframework.data.domain.Page;
import com.github.techChallenge.application.gateways.restaurant.IRestaurantGateway;
import com.github.techChallenge.domain.validation.Validation;

public class ListMenuItemUseCase extends MenuItemUseCase {
    private final IRestaurantGateway restaurants;

    public ListMenuItemUseCase(IMenuItemGateway gateway, IMenuItemMapper mapper, IRestaurantGateway restaurants) {
        super(gateway, mapper);
        this.restaurants = restaurants;
    }

    public Page<MenuItemOutputDTO> execute(Long restaurantId, int page, int offset) {
        Validation.pagination(page, offset);
        restaurants.find(restaurantId);
        Page<MenuItem> menuItems = this.gateway.listByRestaurant(restaurantId, page, offset);
        return this.mapper.fromDomainPageToOutputDTOPage(menuItems);
    }
}

