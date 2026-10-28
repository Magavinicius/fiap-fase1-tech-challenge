package com.github.techChallenge.application.repositories;

import com.github.techChallenge.domain.restaurant.Restaurant;
import org.springframework.data.domain.Page;

public interface IRestaurantRepository {
    Restaurant create(Restaurant restaurant);
    Restaurant update(Restaurant restaurant, Long id);
    Restaurant findByID(Long id);
    Restaurant lockById(Long id);
    Page<Restaurant> list(Integer categoryId, int page, int offset);
    void delete(Long id);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
}
