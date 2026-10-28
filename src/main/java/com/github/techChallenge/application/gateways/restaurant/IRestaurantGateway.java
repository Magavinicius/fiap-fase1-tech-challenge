package com.github.techChallenge.application.gateways.restaurant;

import com.github.techChallenge.domain.restaurant.Restaurant;
import org.springframework.data.domain.Page;

public interface IRestaurantGateway {
    Restaurant create(Restaurant restaurant);
    Restaurant update(Restaurant restaurant, Long id);
    Restaurant find(Long id);
    Restaurant lockById(Long id);
    Page<Restaurant> list(Integer categoryId, int page, int offset);
    void delete(Long id);
    boolean nameExists(String name);
    boolean nameExists(String name, Long id);
}
