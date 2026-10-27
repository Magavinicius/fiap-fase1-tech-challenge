package com.github.techChallenge.infrastructure.security;

import com.github.techChallenge.infrastructure.entities.user.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

public class RestaurantUserPrincipal implements UserDetails {
    private final Long id;
    private final String username;
    private final String password;
    private final List<GrantedAuthority> authorities;

    public RestaurantUserPrincipal(UserEntity user) {
        this.id = user.getId();
        this.username = user.getLogin();
        this.password = user.getPassword();
        this.authorities = user.getUserLevel() == null ? List.of()
                : List.of(new SimpleGrantedAuthority("ROLE_" + user.getUserLevel().name()));
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
}
