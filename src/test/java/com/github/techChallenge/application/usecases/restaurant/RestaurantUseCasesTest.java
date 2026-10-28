package com.github.techChallenge.application.usecases.restaurant;

import com.github.techChallenge.application.exceptions.*;
import com.github.techChallenge.application.gateways.menuItem.IMenuItemGateway;
import com.github.techChallenge.application.gateways.restaurant.IRestaurantGateway;
import com.github.techChallenge.application.gateways.restaurantCategory.IRestaurantCategoryGateway;
import com.github.techChallenge.application.validators.RestaurantAccessPolicy;
import com.github.techChallenge.domain.restaurant.*;
import com.github.techChallenge.domain.restaurant.dto.*;
import com.github.techChallenge.domain.user.Address;
import com.github.techChallenge.domain.validation.DomainValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantUseCasesTest {
    @Mock IRestaurantGateway gateway;
    @Mock IRestaurantMapper mapper;
    @Mock IRestaurantCategoryGateway categories;
    @Mock IMenuItemGateway items;
    @Mock RestaurantAccessPolicy access;
    private final Address address = new Address("Rua A", "10", null, "01000-000", "Centro", "São Paulo", "SP", "Brasil");
    private final LocalDateTime date = LocalDateTime.of(2020, 1, 1, 12, 0);
    private Restaurant restaurant() { return new Restaurant(7L, "Restaurante", address, 1, "11h às 23h", 1L, date, date); }
    private RestaurantCreateInputDTO input() { return new RestaurantCreateInputDTO(" Restaurante ", address, 1, "11h às 23h"); }
    private RestaurantUpdateInputDTO update() { return new RestaurantUpdateInputDTO("Novo", address, 1, "12h às 22h"); }
    private RestaurantOutputDTO output() { return new RestaurantOutputDTO(7L, "Restaurante", address, 1, "11h às 23h", 1L, date, date); }

    @Test void shouldCreateUsingTheAuthenticatedOwnerId() {
        var output = output();
        when(gateway.create(any())).thenAnswer(invocation -> {
            Restaurant created = invocation.getArgument(0);
            assertEquals(1L, created.getOwnerId());
            assertEquals("Restaurante", created.getName());
            return restaurant();
        });
        when(mapper.fromDomainToOutputDTO(any())).thenReturn(output);
        assertSame(output, new CreateRestaurantUseCase(gateway, mapper, access, categories).execute(input(), 1L));
        verify(access).requireOwner(1L);
        verify(categories).find(1);
    }

    @Test void shouldRejectInvalidDataBeforePersistence() {
        var invalid = new RestaurantCreateInputDTO("", null, null, null);
        assertThrows(DomainValidationException.class, () -> new CreateRestaurantUseCase(gateway, mapper, access, categories).execute(invalid, 1L));
        verifyNoInteractions(gateway, mapper, categories);
    }

    @Test void shouldRejectCustomerCreation() {
        doThrow(new ForbiddenOperationException("Acesso negado")).when(access).requireOwner(2L);
        assertThrows(ForbiddenOperationException.class, () -> new CreateRestaurantUseCase(gateway, mapper, access, categories).execute(input(), 2L));
        verifyNoInteractions(gateway, mapper, categories);
    }

    @Test void shouldRejectUnknownCuisine() {
        when(categories.find(1)).thenThrow(new RestaurantCategoryNotFoundException(1));
        assertThrows(RestaurantCategoryNotFoundException.class, () -> new CreateRestaurantUseCase(gateway, mapper, access, categories).execute(input(), 1L));
        verify(gateway, never()).create(any());
        verifyNoInteractions(mapper);
    }

    @Test void shouldRejectDuplicatedName() {
        when(gateway.nameExists("Restaurante")).thenReturn(true);
        assertThrows(DuplicateRestaurantTitleException.class, () -> new CreateRestaurantUseCase(gateway, mapper, access, categories).execute(input(), 1L));
        verify(gateway, never()).create(any());
    }

    @Test void shouldUpdateOnlyTheAuthenticatedOwnersRestaurant() {
        var restaurant = restaurant();
        when(access.requireOwner(7L, 1L)).thenReturn(restaurant);
        when(gateway.update(restaurant, 7L)).thenReturn(restaurant);
        when(mapper.fromDomainToOutputDTO(restaurant)).thenReturn(output());
        new UpdateRestaurantUseCase(gateway, mapper, access, categories).execute(update(), 7L, 1L);
        assertEquals("Novo", restaurant.getName());
        assertEquals(1L, restaurant.getOwnerId());
        assertEquals(date, restaurant.getCreatedAt());
        verify(categories).find(1);
    }

    @Test void shouldNotPersistAnInvalidUpdate() {
        when(access.requireOwner(7L, 1L)).thenReturn(restaurant());
        assertThrows(DomainValidationException.class, () -> new UpdateRestaurantUseCase(gateway, mapper, access, categories)
                .execute(new RestaurantUpdateInputDTO("", address, 1, "11h"), 7L, 1L));
        verifyNoInteractions(gateway, mapper, categories);
    }

    @Test void shouldRejectUpdatingAnotherOwnersRestaurant() {
        doThrow(new ForbiddenOperationException("Acesso negado")).when(access).requireOwner(7L, 2L);
        assertThrows(ForbiddenOperationException.class, () -> new UpdateRestaurantUseCase(gateway, mapper, access, categories).execute(update(), 7L, 2L));
        verifyNoInteractions(gateway, mapper, categories);
    }

    @Test void shouldRejectRenamingToAnExistingRestaurant() {
        when(access.requireOwner(7L, 1L)).thenReturn(restaurant());
        when(gateway.nameExists("Novo", 7L)).thenReturn(true);
        assertThrows(DuplicateRestaurantTitleException.class, () -> new UpdateRestaurantUseCase(gateway, mapper, access, categories).execute(update(), 7L, 1L));
        verify(gateway, never()).update(any(), anyLong());
    }

    @Test void shouldFindAndMapRestaurant() {
        var restaurant = restaurant();
        var output = output();
        when(gateway.find(7L)).thenReturn(restaurant);
        when(mapper.fromDomainToOutputDTO(restaurant)).thenReturn(output);
        assertSame(output, new FindRestaurantUseCase(gateway, mapper).execute(7L));
    }

    @Test void shouldPropagateUnknownRestaurant() {
        when(gateway.find(99L)).thenThrow(new RestaurantNotFoundException(99L));
        assertThrows(RestaurantNotFoundException.class, () -> new FindRestaurantUseCase(gateway, mapper).execute(99L));
        verifyNoInteractions(mapper);
    }

    @Test void shouldListWithoutFiltering() {
        Page<Restaurant> page = new PageImpl<>(List.of(restaurant()));
        Page<RestaurantOutputDTO> output = new PageImpl<>(List.of(output()));
        when(gateway.list(null, 0, 10)).thenReturn(page);
        when(mapper.fromDomainPageToOutputDTOPage(page)).thenReturn(output);
        assertSame(output, new ListRestaurantUseCase(gateway, mapper, categories).execute(null, 0, 10));
        verifyNoInteractions(categories);
    }

    @Test void shouldForwardCuisineAndPagination() {
        Page<Restaurant> page = Page.empty();
        Page<RestaurantOutputDTO> output = Page.empty();
        when(gateway.list(1, 2, 5)).thenReturn(page);
        when(mapper.fromDomainPageToOutputDTOPage(page)).thenReturn(output);
        assertSame(output, new ListRestaurantUseCase(gateway, mapper, categories).execute(1, 2, 5));
        verify(categories).find(1);
    }

    @ParameterizedTest @CsvSource({"-1,10", "0,0", "0,101"})
    void shouldRejectInvalidPagination(int page, int offset) {
        assertThrows(DomainValidationException.class, () -> new ListRestaurantUseCase(gateway, mapper, categories).execute(null, page, offset));
        verifyNoInteractions(gateway, mapper, categories);
    }

    @Test void shouldRejectUnknownCuisineFilter() {
        when(categories.find(99)).thenThrow(new RestaurantCategoryNotFoundException(99));
        assertThrows(RestaurantCategoryNotFoundException.class, () -> new ListRestaurantUseCase(gateway, mapper, categories).execute(99, 0, 10));
        verifyNoInteractions(gateway, mapper);
    }

    @Test void shouldDeleteAnEmptyRestaurant() {
        new DeleteRestaurantUseCase(gateway, items, access).execute(7L, 1L);
        verify(access).requireOwner(7L, 1L);
        verify(gateway).delete(7L);
    }

    @Test void shouldPreventOrphanMenuItemsOnRestaurantDeletion() {
        when(items.existsByRestaurant(7L)).thenReturn(true);
        assertThrows(RestaurantHasMenuItemsException.class, () -> new DeleteRestaurantUseCase(gateway, items, access).execute(7L, 1L));
        verify(gateway, never()).delete(anyLong());
    }

    @Test void shouldRejectDeletionByAnotherOwnerBeforeConsultingItems() {
        doThrow(new ForbiddenOperationException("Acesso negado")).when(access).requireOwner(7L, 2L);
        assertThrows(ForbiddenOperationException.class, () -> new DeleteRestaurantUseCase(gateway, items, access).execute(7L, 2L));
        verifyNoInteractions(gateway, items);
    }
}
