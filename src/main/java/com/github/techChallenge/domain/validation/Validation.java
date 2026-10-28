package com.github.techChallenge.domain.validation;

import com.github.techChallenge.domain.user.Address;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Validation {
    private Validation() {}

    public static void text(Map<String, String> errors, String field, String value, int max) {
        if (value == null || value.isBlank()) {
            errors.put(field, "O campo é obrigatório.");
        } else if (value.length() > max) {
            errors.put(field, "O campo deve possuir no máximo " + max + " caracteres.");
        }
    }

    public static void positiveId(Map<String, String> errors, String field, Number value) {
        if (value == null || value.longValue() <= 0) {
            errors.put(field, "Informe um identificador maior que zero.");
        }
    }

    public static void address(Map<String, String> errors, Address address) {
        if (address == null) {
            errors.put("address", "O endereço é obrigatório.");
            return;
        }
        text(errors, "address.address", address.address(), 255);
        text(errors, "address.number", address.number(), 255);
        text(errors, "address.neighborhood", address.neighborhood(), 255);
        text(errors, "address.city", address.city(), 255);
        text(errors, "address.country", address.country(), 255);
        if (address.complement() != null && address.complement().length() > 255) {
            errors.put("address.complement", "O complemento deve possuir no máximo 255 caracteres.");
        }
        if (address.zipCode() == null || !address.zipCode().matches("[0-9]{5}-?[0-9]{3}")) {
            errors.put("address.zipCode", "O padrão para o CEP é 99999-999 ou 99999999.");
        }
        if (address.state() == null || !address.state().matches("[A-Z]{2}")) {
            errors.put("address.state", "A UF deve possuir duas letras maiúsculas.");
        }
    }

    public static void throwIfInvalid(Map<String, String> errors) {
        if (!errors.isEmpty()) throw new DomainValidationException(errors);
    }

    public static void pagination(int page, int offset) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (page < 0) errors.put("page", "A página deve ser maior ou igual a zero.");
        if (offset < 1 || offset > 100) errors.put("offset", "Informe entre 1 e 100 itens por página.");
        throwIfInvalid(errors);
    }
}
