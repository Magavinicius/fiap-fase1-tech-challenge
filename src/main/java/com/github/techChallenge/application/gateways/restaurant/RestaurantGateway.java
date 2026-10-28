package com.github.techChallenge.application.gateways.restaurant;

import com.github.techChallenge.application.repositories.IRestaurantRepository;
import com.github.techChallenge.domain.restaurant.Restaurant;
import org.springframework.data.domain.Page;

public class RestaurantGateway implements IRestaurantGateway {
    private final IRestaurantRepository repository;
    public RestaurantGateway(IRestaurantRepository repository) { this.repository = repository; }
    public Restaurant create(Restaurant restaurant) { return repository.create(restaurant); }
    public Restaurant update(Restaurant restaurant, Long id) { return repository.update(restaurant, id); }
    public Restaurant find(Long id) { return repository.findByID(id); }
    public Restaurant lockById(Long id) { return repository.lockById(id); }
    public Page<Restaurant> list(Integer categoryId, int page, int offset) { return repository.list(categoryId, page, offset); }
    public void delete(Long id) { repository.delete(id); }
    public boolean nameExists(String name) { return repository.existsByName(name); }
    public boolean nameExists(String name, Long id) { return repository.existsByNameAndIdNot(name, id); }
}
