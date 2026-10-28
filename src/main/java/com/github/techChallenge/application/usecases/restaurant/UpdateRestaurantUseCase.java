package com.github.techChallenge.application.usecases.restaurant;

import com.github.techChallenge.application.exceptions.DuplicateRestaurantTitleException;
import com.github.techChallenge.application.gateways.restaurant.IRestaurantGateway;
import com.github.techChallenge.application.gateways.restaurantCategory.IRestaurantCategoryGateway;
import com.github.techChallenge.application.validators.RestaurantAccessPolicy;
import com.github.techChallenge.domain.restaurant.IRestaurantMapper;
import com.github.techChallenge.domain.restaurant.Restaurant;
import com.github.techChallenge.domain.restaurant.dto.RestaurantUpdateInputDTO;
import com.github.techChallenge.domain.restaurant.dto.RestaurantOutputDTO;
import org.springframework.transaction.annotation.Transactional;

public class UpdateRestaurantUseCase {
    private final IRestaurantGateway gateway;
    private final IRestaurantMapper mapper;
    private final RestaurantAccessPolicy access;
    private final IRestaurantCategoryGateway categories;

    public UpdateRestaurantUseCase(IRestaurantGateway gateway, IRestaurantMapper mapper,
                                   RestaurantAccessPolicy access, IRestaurantCategoryGateway categories) {
        this.gateway = gateway; this.mapper = mapper; this.access = access; this.categories = categories;
    }

    @Transactional
    public RestaurantOutputDTO execute(RestaurantUpdateInputDTO dto, Long id, Long authenticatedUserId) {
        Restaurant restaurant = access.requireOwner(id, authenticatedUserId);
        restaurant.update(dto.name(), dto.address(), dto.categoryId(), dto.openingHours());
        categories.find(restaurant.getCategoryId());
        if (gateway.nameExists(restaurant.getName(), id)) throw new DuplicateRestaurantTitleException(restaurant.getName());
        return mapper.fromDomainToOutputDTO(gateway.update(restaurant, id));
    }
}
