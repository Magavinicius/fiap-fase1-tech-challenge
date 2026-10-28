package com.github.techChallenge.domain.validation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DomainValidationException extends IllegalArgumentException {
    private final Map<String, String> violations;

    public DomainValidationException(Map<String, String> violations) {
        super("Um ou mais campos informados são inválidos.");
        this.violations = Collections.unmodifiableMap(new LinkedHashMap<>(violations));
    }

    public Map<String, String> getViolations() {
        return violations;
    }
}
