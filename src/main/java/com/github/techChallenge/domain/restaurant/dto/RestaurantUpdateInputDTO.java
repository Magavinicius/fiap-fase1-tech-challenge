package com.github.techChallenge.domain.restaurant.dto;

import com.github.techChallenge.domain.user.Address;
import io.swagger.v3.oas.annotations.media.Schema;

public record RestaurantUpdateInputDTO(
        @Schema(example = "Restaurante Magalhães", maxLength = 100, requiredMode = Schema.RequiredMode.REQUIRED) String name,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Address address,
        @Schema(description = "ID da categoria que representa o tipo de cozinha", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Integer categoryId,
        @Schema(example = "Segunda a sábado, das 11h às 23h", maxLength = 255, requiredMode = Schema.RequiredMode.REQUIRED) String openingHours
) {}
