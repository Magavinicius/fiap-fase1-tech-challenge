package com.github.techChallenge.domain.user.dto;

import com.github.techChallenge.domain.user.Address;
import com.github.techChallenge.domain.user.UserLevel;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserUpdateInputDTO(

        @Schema(
                description = "Nome completo do usuário",
                example = "José da Silva",
                maxLength = 50,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String name,

        @Schema(
                description = "E-mail do usuário",
                example = "jose.silva@email.com",
                format = "email",
                maxLength = 255,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String email,

        @Schema(
                description = "Nome de usuário para acesso ao sistema",
                example = "jose.silva",
                minLength = 4,
                maxLength = 100,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String login,

        @Schema(
                description = "Nível de usuário",
                allowableValues = {"OWNER", "CUSTOMER"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        UserLevel userLevel,

        @Schema(
                description = "Endereço do usuário",
                implementation = Address.class,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Address address

) {
}
