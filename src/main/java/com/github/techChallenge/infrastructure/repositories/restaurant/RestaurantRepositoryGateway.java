package com.github.techChallenge.infrastructure.repositories.restaurant;

import com.github.techChallenge.application.exceptions.RestaurantCategoryNotFoundException;
import com.github.techChallenge.application.exceptions.RestaurantNotFoundException;
import com.github.techChallenge.application.exceptions.UserNotFoundException;
import com.github.techChallenge.application.repositories.IRestaurantRepository;
import com.github.techChallenge.domain.restaurant.Restaurant;
import com.github.techChallenge.infrastructure.entities.restaurant.RestaurantEntity;
import com.github.techChallenge.infrastructure.entities.restaurantCategory.RestaurantCategoryEntity;
import com.github.techChallenge.infrastructure.mappers.RestaurantMapper;
import com.github.techChallenge.infrastructure.repositories.restaurantCategory.RestaurantCategoryRepository;
import com.github.techChallenge.infrastructure.repositories.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Transactional
public class RestaurantRepositoryGateway implements IRestaurantRepository {
    private final RestaurantRepository repository;
    private final UserRepository users;
    private final RestaurantCategoryRepository categories;
    private final RestaurantMapper mapper;

    public RestaurantRepositoryGateway(RestaurantRepository repository, UserRepository users,
                                       RestaurantCategoryRepository categories, RestaurantMapper mapper) {
        this.repository = repository; this.users = users; this.categories = categories; this.mapper = mapper;
    }

    public Restaurant create(Restaurant restaurant) {
        var owner = users.findById(restaurant.getOwnerId()).orElseThrow(() -> new UserNotFoundException(restaurant.getOwnerId()));
        var entity = new RestaurantEntity(restaurant, owner, category(restaurant.getCategoryId()));
        return mapper.fromEntityToDomain(repository.saveAndFlush(entity));
    }

    public Restaurant update(Restaurant restaurant, Long id) {
        RestaurantEntity entity = findEntity(id);
        entity.update(restaurant, category(restaurant.getCategoryId()));
        return mapper.fromEntityToDomain(repository.saveAndFlush(entity));
    }

    @Transactional(readOnly = true)
    public Restaurant findByID(Long id) { return mapper.fromEntityToDomain(findEntity(id)); }

    public Restaurant lockById(Long id) {
        return mapper.fromEntityToDomain(repository.findForUpdate(id).orElseThrow(() -> new RestaurantNotFoundException(id)));
    }

    @Transactional(readOnly = true)
    public Page<Restaurant> list(Integer categoryId, int page, int offset) {
        var pageable = PageRequest.of(page, offset, Sort.by("id"));
        var entities = categoryId == null ? repository.findAll(pageable) : repository.findByCategory_Id(categoryId, pageable);
        return entities.map(mapper::fromEntityToDomain);
    }

    public void delete(Long id) { repository.delete(findEntity(id)); repository.flush(); }
    public boolean existsByName(String name) { return repository.existsByNameKey(key(name)); }
    public boolean existsByNameAndIdNot(String name, Long id) { return repository.existsByNameKeyAndIdNot(key(name), id); }

    private RestaurantEntity findEntity(Long id) { return repository.findById(id).orElseThrow(() -> new RestaurantNotFoundException(id)); }
    private RestaurantCategoryEntity category(Integer id) { return categories.findById(id).orElseThrow(() -> new RestaurantCategoryNotFoundException(id)); }
    private String key(String name) { return name.trim().toLowerCase(Locale.ROOT); }
}
