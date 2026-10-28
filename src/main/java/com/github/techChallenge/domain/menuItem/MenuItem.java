package com.github.techChallenge.domain.menuItem;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import com.github.techChallenge.domain.validation.Validation;

public class MenuItem {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean dineInOnly;
    private String photoPath;
    private Long restaurantId;

    public MenuItem() {
    }

    public MenuItem(
            Long id,
            String name,
            String description,
            BigDecimal price,
            Boolean dineInOnly,
            String photoPath,
            Long restaurantId
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.dineInOnly = dineInOnly;
        this.photoPath = photoPath;
        this.restaurantId = restaurantId;
    }

    public static MenuItem create(
            String name,
            String description,
            BigDecimal price,
            Boolean dineInOnly,
            String photoPath,
            Long restaurantId
    ) {
        validate(name, description, price, dineInOnly, photoPath, restaurantId);
        return new MenuItem(
                null,
                name.trim(),
                description,
                price,
                dineInOnly,
                photoPath,
                restaurantId
        );
    }

    public void update(
            String name,
            String description,
            BigDecimal price,
            Boolean dineInOnly,
            String photoPath
    ) {
        validate(name, description, price, dineInOnly, photoPath, restaurantId);
        this.name = name.trim();
        this.description = description;
        this.price = price;
        this.dineInOnly = dineInOnly;
        this.photoPath = photoPath;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isDineInOnly() {
        return dineInOnly;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    private static void validate(String name, String description, BigDecimal price,
                                 Boolean dineInOnly, String photoPath, Long restaurantId) {
        Map<String, String> errors = new LinkedHashMap<>();
        Validation.text(errors, "name", name, 100);
        Validation.text(errors, "description", description, 255);
        Validation.text(errors, "photoPath", photoPath, 255);
        Validation.positiveId(errors, "restaurantId", restaurantId);
        if (price == null || price.signum() <= 0) {
            errors.put("price", "O preço deve ser maior que zero.");
        } else if (price.stripTrailingZeros().scale() > 2
                || price.compareTo(new BigDecimal("9999999999.99")) > 0) {
            errors.put("price", "Informe um preço de até 9999999999.99, com no máximo duas casas decimais.");
        }
        if (dineInOnly == null) errors.put("dineInOnly", "Informe se o item é exclusivo para consumo no local.");
        Validation.throwIfInvalid(errors);
    }
}

