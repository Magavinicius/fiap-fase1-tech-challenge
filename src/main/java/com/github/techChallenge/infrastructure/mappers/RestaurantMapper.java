package com.github.techChallenge.infrastructure.mappers;

import com.github.techChallenge.domain.restaurant.IRestaurantMapper;
import com.github.techChallenge.domain.restaurant.Restaurant;
import com.github.techChallenge.domain.restaurant.dto.RestaurantOutputDTO;
import com.github.techChallenge.domain.user.Address;
import com.github.techChallenge.infrastructure.entities.restaurant.RestaurantEntity;
import com.github.techChallenge.infrastructure.entities.user.AddressEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class RestaurantMapper implements IRestaurantMapper {
    public Restaurant fromEntityToDomain(RestaurantEntity entity) {
        AddressEntity address = entity.getAddress();
        return new Restaurant(entity.getId(), entity.getName(),
                new Address(address.getAddress(), address.getNumber(), address.getComplement(),
                        address.getZipCode(), address.getNeighborhood(), address.getCity(), address.getState(), address.getCountry()),
                entity.getCategory().getId(), entity.getOpeningHours(), entity.getOwner().getId(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public RestaurantOutputDTO fromDomainToOutputDTO(Restaurant restaurant) {
        return new RestaurantOutputDTO(restaurant.getId(), restaurant.getName(), restaurant.getAddress(),
                restaurant.getCategoryId(), restaurant.getOpeningHours(), restaurant.getOwnerId(),
                restaurant.getCreatedAt(), restaurant.getUpdatedAt());
    }

    public Page<RestaurantOutputDTO> fromDomainPageToOutputDTOPage(Page<Restaurant> restaurants) {
        return restaurants.map(this::fromDomainToOutputDTO);
    }
}
