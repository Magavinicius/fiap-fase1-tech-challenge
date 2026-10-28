package com.github.techChallenge.domain.restaurantCategory;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mock;

public class RestaurantCategoryTest {


    @Test
    @DisplayName("Deve criar uma categoria de restaurant na entidade de dominio")
    void shouldCreateRestaurantCategoryDomain(){

        RestaurantCategory restaurantCategory = RestaurantCategory.create("Hamburgueria");

        assertNotNull(restaurantCategory);
        assertInstanceOf(RestaurantCategory.class, restaurantCategory);
        assertEquals(0, restaurantCategory.getId());
        assertEquals("Hamburgueria", restaurantCategory.getTitle());

    }

    @Test
    @DisplayName("Deve criar uma instancia de categoria de restaurant a partir da entidade de dominio")
    void shouldCreateObjectRestaurantCategoryDomain(){

        RestaurantCategory restaurantCategory = new RestaurantCategory(1, "Hamburgueria");

        assertNotNull(restaurantCategory);
        assertInstanceOf(RestaurantCategory.class, restaurantCategory);
        assertEquals(1, restaurantCategory.getId());
        assertEquals("Hamburgueria", restaurantCategory.getTitle());

    }

    @Test
    @DisplayName("Deve atualizar a categoria de restaurant na entidade de domínio")
    void shouldUpdateRestaurantCategoryDomain(){

        RestaurantCategory restaurantCategory = new RestaurantCategory(1, "Cafeteria");

        assertNotNull(restaurantCategory);
        assertInstanceOf(RestaurantCategory.class, restaurantCategory);
        assertEquals(1, restaurantCategory.getId());
        assertEquals("Cafeteria", restaurantCategory.getTitle());

    }
}
