package com.github.techChallenge.application.exceptions;

public class RestaurantHasMenuItemsException extends BusinessException {
    public RestaurantHasMenuItemsException() {
        super("RESTAURANT_HAS_MENU_ITEMS", "Remova os itens do cardápio antes de excluir o restaurante.");
    }
}
