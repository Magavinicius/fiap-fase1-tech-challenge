package com.github.techChallenge.application.usecases.restaurant;

import com.github.techChallenge.application.gateways.restaurant.IRestaurantGateway;
import com.github.techChallenge.domain.restaurant.IRestaurantMapper;
import com.github.techChallenge.domain.restaurant.dto.RestaurantOutputDTO;

public class FindRestaurantUseCase {
    private final IRestaurantGateway gateway;
    private final IRestaurantMapper mapper;
    public FindRestaurantUseCase(IRestaurantGateway gateway, IRestaurantMapper mapper) { this.gateway = gateway; this.mapper = mapper; }
    public RestaurantOutputDTO execute(Long id) { return mapper.fromDomainToOutputDTO(gateway.find(id)); }
}
