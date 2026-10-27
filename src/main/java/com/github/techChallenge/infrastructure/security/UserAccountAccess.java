package com.github.techChallenge.infrastructure.security;

import com.github.techChallenge.application.exceptions.ForbiddenOperationException;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Locale;

public final class UserAccountAccess {
    private UserAccountAccess() {}

    public static RestaurantUserPrincipal requireSelf(Long targetUserId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof RestaurantUserPrincipal user)
                || targetUserId == null || !targetUserId.equals(user.getId())) {
            throw new ForbiddenOperationException("O usuário autenticado só pode alterar a própria conta.");
        }
        return user;
    }

    public static void requireOwnLogin(RestaurantUserPrincipal user, String login) {
        if (login == null || !user.getUsername().equals(login.trim().toLowerCase(Locale.ROOT))) {
            throw new ForbiddenOperationException("O login deve corresponder ao usuário autenticado.");
        }
    }
}
