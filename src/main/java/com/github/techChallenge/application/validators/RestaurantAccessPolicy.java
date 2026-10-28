package com.github.techChallenge.application.validators;

import com.github.techChallenge.application.exceptions.ForbiddenOperationException;
import com.github.techChallenge.application.gateways.restaurant.IRestaurantGateway;
import com.github.techChallenge.application.gateways.user.IUserGateway;
import com.github.techChallenge.domain.restaurant.Restaurant;
import com.github.techChallenge.domain.user.User;
import com.github.techChallenge.domain.user.UserLevel;

public class RestaurantAccessPolicy {
    private final IUserGateway users;
    private final IRestaurantGateway restaurants;

    public RestaurantAccessPolicy(IUserGateway users, IRestaurantGateway restaurants) {
        this.users = users;
        this.restaurants = restaurants;
    }

    public User requireOwner(Long authenticatedUserId) {
        if (authenticatedUserId == null) {
            throw new ForbiddenOperationException("É necessário identificar o usuário autenticado.");
        }
        User user = users.find(authenticatedUserId);
        if (user.getUserLevel() != UserLevel.OWNER) {
            throw new ForbiddenOperationException("Somente donos de restaurante podem executar esta operação.");
        }
        return user;
    }

    public Restaurant requireOwner(Long restaurantId, Long authenticatedUserId) {
        User user = requireOwner(authenticatedUserId);
        Restaurant restaurant = restaurants.lockById(restaurantId);
        if (!restaurant.getOwnerId().equals(user.getId())) {
            throw new ForbiddenOperationException("O usuário autenticado não é dono deste restaurante.");
        }
        return restaurant;
    }
}
