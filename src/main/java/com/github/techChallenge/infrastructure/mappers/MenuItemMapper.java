package com.github.techChallenge.infrastructure.mappers;

import com.github.techChallenge.domain.menuItem.IMenuItemMapper;
import com.github.techChallenge.domain.menuItem.MenuItem;
import com.github.techChallenge.domain.menuItem.dto.MenuItemOutputDTO;
import com.github.techChallenge.infrastructure.entities.menuItem.MenuItemDocument;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class MenuItemMapper implements IMenuItemMapper {
    public MenuItem fromDocumentToDomain(MenuItemDocument document) {
        return new MenuItem(document.getId(), document.getName(), document.getDescription(), document.getPrice(),
                document.isDineInOnly(), document.getPhotoPath(), document.getRestaurantId());
    }
    public MenuItemOutputDTO fromDomainToOutputDTO(MenuItem item) {
        return new MenuItemOutputDTO(item.getId(), item.getName(), item.getDescription(), item.getPrice(),
                item.isDineInOnly(), item.getPhotoPath(), item.getRestaurantId());
    }
    public Page<MenuItemOutputDTO> fromDomainPageToOutputDTOPage(Page<MenuItem> items) { return items.map(this::fromDomainToOutputDTO); }
}
