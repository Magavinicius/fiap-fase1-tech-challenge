package com.github.techChallenge.infrastructure.entities.menuItem;

import com.github.techChallenge.domain.menuItem.MenuItem;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import java.math.BigDecimal;
import java.util.Locale;

@Document("menu_items")
public class MenuItemDocument {
    @Id private Long id;
    @Version private Long version;
    private String name;
    private String nameKey;
    private String description;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal price;
    private boolean dineInOnly;
    private String photoPath;
    private Long restaurantId;

    protected MenuItemDocument() {}

    public MenuItemDocument(Long id, MenuItem item) {
        this.id = id;
        this.restaurantId = item.getRestaurantId();
        update(item);
    }

    public void update(MenuItem item) {
        this.name = item.getName();
        this.nameKey = name.trim().toLowerCase(Locale.ROOT);
        this.description = item.getDescription();
        this.price = item.getPrice();
        this.dineInOnly = item.isDineInOnly();
        this.photoPath = item.getPhotoPath();
    }

    public Long getId() { return id; }
    public Long getVersion() { return version; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public boolean isDineInOnly() { return dineInOnly; }
    public String getPhotoPath() { return photoPath; }
    public Long getRestaurantId() { return restaurantId; }
}
