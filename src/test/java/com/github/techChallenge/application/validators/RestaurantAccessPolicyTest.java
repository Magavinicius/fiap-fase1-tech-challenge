package com.github.techChallenge.application.validators;

import com.github.techChallenge.application.exceptions.*;
import com.github.techChallenge.application.gateways.restaurant.IRestaurantGateway;
import com.github.techChallenge.application.gateways.user.IUserGateway;
import com.github.techChallenge.domain.restaurant.Restaurant;
import com.github.techChallenge.domain.user.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantAccessPolicyTest {
    @Mock IUserGateway users;
    @Mock IRestaurantGateway restaurants;

    private User user(Long id, UserLevel level) {
        return new User(id, "Usuário", "user@email.com", "user", "hash", level, null, null, null, null);
    }

    @Test void shouldAllowTheAuthenticatedOwner() {
        var owner = user(1L, UserLevel.OWNER);
        var restaurant = new Restaurant(7L, "R", null, 1, "11h às 23h", 1L, null, null);
        when(users.find(1L)).thenReturn(owner);
        when(restaurants.lockById(7L)).thenReturn(restaurant);
        assertSame(restaurant, new RestaurantAccessPolicy(users, restaurants).requireOwner(7L, 1L));
    }

    @Test void shouldRejectCustomerBeforeConsultingRestaurant() {
        when(users.find(1L)).thenReturn(user(1L, UserLevel.CUSTOMER));
        assertThrows(ForbiddenOperationException.class, () -> new RestaurantAccessPolicy(users, restaurants).requireOwner(7L, 1L));
        verifyNoInteractions(restaurants);
    }

    @Test void shouldRejectAnotherOwner() {
        when(users.find(2L)).thenReturn(user(2L, UserLevel.OWNER));
        when(restaurants.lockById(7L)).thenReturn(new Restaurant(7L, "R", null, 1, "11h às 23h", 1L, null, null));
        assertThrows(ForbiddenOperationException.class, () -> new RestaurantAccessPolicy(users, restaurants).requireOwner(7L, 2L));
    }

    @Test void shouldRejectMissingAuthenticatedUser() {
        assertThrows(ForbiddenOperationException.class, () -> new RestaurantAccessPolicy(users, restaurants).requireOwner(null));
        verifyNoInteractions(users, restaurants);
    }

    @Test void shouldPropagateUserNotFound() {
        when(users.find(99L)).thenThrow(new UserNotFoundException(99L));
        assertThrows(UserNotFoundException.class, () -> new RestaurantAccessPolicy(users, restaurants).requireOwner(99L));
        verifyNoInteractions(restaurants);
    }

    @Test void shouldPropagateRestaurantNotFound() {
        when(users.find(1L)).thenReturn(user(1L, UserLevel.OWNER));
        when(restaurants.lockById(99L)).thenThrow(new RestaurantNotFoundException(99L));
        assertThrows(RestaurantNotFoundException.class, () -> new RestaurantAccessPolicy(users, restaurants).requireOwner(99L, 1L));
    }
}
