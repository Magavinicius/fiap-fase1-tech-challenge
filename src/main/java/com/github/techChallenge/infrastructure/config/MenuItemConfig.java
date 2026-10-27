package com.github.techChallenge.infrastructure.config;

import com.github.techChallenge.application.gateways.menuItem.*;
import com.github.techChallenge.application.gateways.restaurant.IRestaurantGateway;
import com.github.techChallenge.application.repositories.IMenuItemRepository;
import com.github.techChallenge.application.usecases.menuItem.*;
import com.github.techChallenge.application.validators.RestaurantAccessPolicy;
import com.github.techChallenge.domain.menuItem.IMenuItemMapper;
import com.github.techChallenge.infrastructure.mappers.MenuItemMapper;
import com.github.techChallenge.infrastructure.repositories.menuItem.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MenuItemConfig {
    @Bean MenuItemRepositoryGateway menuItemRepositoryGateway(MenuItemMongoRepository repository, MongoTemplate mongo, MenuItemMapper mapper) {
        return new MenuItemRepositoryGateway(repository, mongo, mapper);
    }
    @Bean MenuItemGateway menuItemGateway(IMenuItemRepository repository) { return new MenuItemGateway(repository); }
    @Bean CreateMenuItemUseCase createMenuItemUseCase(IMenuItemGateway gateway, IMenuItemMapper mapper, RestaurantAccessPolicy access) {
        return new CreateMenuItemUseCase(gateway, mapper, access);
    }
    @Bean UpdateMenuItemUseCase updateMenuItemUseCase(IMenuItemGateway gateway, IMenuItemMapper mapper, RestaurantAccessPolicy access) {
        return new UpdateMenuItemUseCase(gateway, mapper, access);
    }
    @Bean FindMenuItemUseCase findMenuItemUseCase(IMenuItemGateway gateway, IMenuItemMapper mapper) {
        return new FindMenuItemUseCase(gateway, mapper);
    }
    @Bean ListMenuItemUseCase listMenuItemUseCase(IMenuItemGateway gateway, IMenuItemMapper mapper, IRestaurantGateway restaurants) {
        return new ListMenuItemUseCase(gateway, mapper, restaurants);
    }
    @Bean DeleteMenuItemUseCase deleteMenuItemUseCase(IMenuItemGateway gateway, IMenuItemMapper mapper, RestaurantAccessPolicy access) {
        return new DeleteMenuItemUseCase(gateway, mapper, access);
    }
}
