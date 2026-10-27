package com.github.techChallenge.application.exceptions;

public class ForbiddenOperationException extends BusinessException {
    public ForbiddenOperationException(String message) { super("FORBIDDEN_OPERATION", message); }
}
