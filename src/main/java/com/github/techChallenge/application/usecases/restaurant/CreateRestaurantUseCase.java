package com.github.techChallenge.application.usecases.restaurant;

import com.github.techChallenge.application.exceptions.DuplicateRestaurantTitleException;
import com.github.techChallenge.application.gateways.restaurant.IRestaurantGateway;
import com.github.techChallenge.application.gateways.restaurantCategory.IRestaurantCategoryGateway;
import com.github.techChallenge.application.validators.RestaurantAccessPolicy;
import com.github.techChallenge.domain.restaurant.IRestaurantMapper;
import com.github.techChallenge.domain.restaurant.Restaurant;
import com.github.techChallenge.domain.restaurant.dto.RestaurantCreateInputDTO;
import com.github.techChallenge.domain.restaurant.dto.RestaurantOutputDTO;
import org.springframework.transaction.annotation.Transactional;

public class CreateRestaurantUseCase {
    private final IRestaurantGateway gateway;
    private final IRestaurantMapper mapper;
    private final RestaurantAccessPolicy access;
    private final IRestaurantCategoryGateway categories;

    public CreateRestaurantUseCase(IRestaurantGateway gateway, IRestaurantMapper mapper,
                                   RestaurantAccessPolicy access, IRestaurantCategoryGateway categories) {
        this.gateway = gateway; this.mapper = mapper; this.access = access; this.categories = categories;
    }

    @Transactional
    public RestaurantOutputDTO execute(RestaurantCreateInputDTO dto, Long authenticatedUserId) {
        access.requireOwner(authenticatedUserId);
        Restaurant restaurant = Restaurant.create(dto.name(), dto.address(), dto.categoryId(),
                dto.openingHours(), authenticatedUserId);
        categories.find(restaurant.getCategoryId());
        if (gateway.nameExists(restaurant.getName())) throw new DuplicateRestaurantTitleException(restaurant.getName());
        return mapper.fromDomainToOutputDTO(gateway.create(restaurant));
    }
}
