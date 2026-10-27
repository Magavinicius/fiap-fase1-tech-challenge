package com.github.techChallenge.domain.restaurant.dto;

import com.github.techChallenge.domain.user.Address;
import java.time.LocalDateTime;

public record RestaurantOutputDTO(Long id, String name, Address address, Integer categoryId,
                                  String openingHours, Long ownerId,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {}
