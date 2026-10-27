package com.github.techChallenge.domain.restaurant;

import com.github.techChallenge.domain.restaurant.dto.RestaurantOutputDTO;
import org.springframework.data.domain.Page;

public interface IRestaurantMapper {
    RestaurantOutputDTO fromDomainToOutputDTO(Restaurant restaurant);
    Page<RestaurantOutputDTO> fromDomainPageToOutputDTOPage(Page<Restaurant> restaurants);
}
