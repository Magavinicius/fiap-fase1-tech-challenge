package com.github.techChallenge.domain.restaurant;

import com.github.techChallenge.domain.user.Address;
import com.github.techChallenge.domain.validation.Validation;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class Restaurant {
    private final Long id;
    private String name;
    private Address address;
    private Integer categoryId;
    private String openingHours;
    private final Long ownerId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Restaurant(Long id, String name, Address address, Integer categoryId,
                      String openingHours, Long ownerId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.categoryId = categoryId;
        this.openingHours = openingHours;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Restaurant create(String name, Address address, Integer categoryId,
                                    String openingHours, Long ownerId) {
        validate(name, address, categoryId, openingHours, ownerId);
        LocalDateTime now = LocalDateTime.now();
        return new Restaurant(null, name.trim(), address, categoryId, openingHours.trim(), ownerId, now, now);
    }

    public void update(String name, Address address, Integer categoryId, String openingHours) {
        validate(name, address, categoryId, openingHours, ownerId);
        this.name = name.trim();
        this.address = address;
        this.categoryId = categoryId;
        this.openingHours = openingHours.trim();
        this.updatedAt = LocalDateTime.now();
    }

    private static void validate(String name, Address address, Integer categoryId,
                                 String openingHours, Long ownerId) {
        Map<String, String> errors = new LinkedHashMap<>();
        Validation.text(errors, "name", name, 100);
        Validation.address(errors, address);
        Validation.positiveId(errors, "categoryId", categoryId);
        Validation.text(errors, "openingHours", openingHours, 255);
        Validation.positiveId(errors, "ownerId", ownerId);
        Validation.throwIfInvalid(errors);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Address getAddress() { return address; }
    public Integer getCategoryId() { return categoryId; }
    public String getOpeningHours() { return openingHours; }
    public Long getOwnerId() { return ownerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
