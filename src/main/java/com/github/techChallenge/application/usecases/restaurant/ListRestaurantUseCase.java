package com.github.techChallenge.application.usecases.restaurant;

import com.github.techChallenge.application.gateways.restaurant.IRestaurantGateway;
import com.github.techChallenge.application.gateways.restaurantCategory.IRestaurantCategoryGateway;
import com.github.techChallenge.domain.restaurant.IRestaurantMapper;
import com.github.techChallenge.domain.restaurant.dto.RestaurantOutputDTO;
import com.github.techChallenge.domain.validation.Validation;
import org.springframework.data.domain.Page;

public class ListRestaurantUseCase {
    private final IRestaurantGateway gateway;
    private final IRestaurantMapper mapper;
    private final IRestaurantCategoryGateway categories;
    public ListRestaurantUseCase(IRestaurantGateway gateway, IRestaurantMapper mapper, IRestaurantCategoryGateway categories) {
        this.gateway = gateway; this.mapper = mapper; this.categories = categories;
    }
    public Page<RestaurantOutputDTO> execute(Integer categoryId, int page, int offset) {
        Validation.pagination(page, offset);
        if (categoryId != null) categories.find(categoryId);
        return mapper.fromDomainPageToOutputDTOPage(gateway.list(categoryId, page, offset));
    }
}
