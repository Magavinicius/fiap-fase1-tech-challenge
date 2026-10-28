package com.github.techChallenge.infrastructure.config;

import com.github.techChallenge.application.gateways.restaurant.*;
import com.github.techChallenge.application.gateways.menuItem.IMenuItemGateway;
import com.github.techChallenge.application.gateways.restaurantCategory.IRestaurantCategoryGateway;
import com.github.techChallenge.application.gateways.user.IUserGateway;
import com.github.techChallenge.application.repositories.IRestaurantRepository;
import com.github.techChallenge.application.usecases.restaurant.*;
import com.github.techChallenge.application.validators.RestaurantAccessPolicy;
import com.github.techChallenge.domain.restaurant.IRestaurantMapper;
import com.github.techChallenge.infrastructure.mappers.RestaurantMapper;
import com.github.techChallenge.infrastructure.repositories.restaurant.*;
import com.github.techChallenge.infrastructure.repositories.restaurantCategory.RestaurantCategoryRepository;
import com.github.techChallenge.infrastructure.repositories.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestaurantConfig {
    @Bean RestaurantRepositoryGateway restaurantRepositoryGateway(RestaurantRepository repository,
            UserRepository users, RestaurantCategoryRepository categories, RestaurantMapper mapper) {
        return new RestaurantRepositoryGateway(repository, users, categories, mapper);
    }
    @Bean RestaurantGateway restaurantGateway(IRestaurantRepository repository) { return new RestaurantGateway(repository); }
    @Bean RestaurantAccessPolicy restaurantAccessPolicy(IUserGateway users, IRestaurantGateway restaurants) {
        return new RestaurantAccessPolicy(users, restaurants);
    }
    @Bean CreateRestaurantUseCase createRestaurantUseCase(IRestaurantGateway gateway, IRestaurantMapper mapper,
            RestaurantAccessPolicy access, IRestaurantCategoryGateway categories) {
        return new CreateRestaurantUseCase(gateway, mapper, access, categories);
    }
    @Bean UpdateRestaurantUseCase updateRestaurantUseCase(IRestaurantGateway gateway, IRestaurantMapper mapper,
            RestaurantAccessPolicy access, IRestaurantCategoryGateway categories) {
        return new UpdateRestaurantUseCase(gateway, mapper, access, categories);
    }
    @Bean FindRestaurantUseCase findRestaurantUseCase(IRestaurantGateway gateway, IRestaurantMapper mapper) {
        return new FindRestaurantUseCase(gateway, mapper);
    }
    @Bean ListRestaurantUseCase listRestaurantUseCase(IRestaurantGateway gateway, IRestaurantMapper mapper,
            IRestaurantCategoryGateway categories) { return new ListRestaurantUseCase(gateway, mapper, categories); }
    @Bean DeleteRestaurantUseCase deleteRestaurantUseCase(IRestaurantGateway gateway, IMenuItemGateway menuItems,
            RestaurantAccessPolicy access) { return new DeleteRestaurantUseCase(gateway, menuItems, access); }
}
