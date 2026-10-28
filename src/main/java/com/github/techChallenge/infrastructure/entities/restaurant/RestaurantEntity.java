package com.github.techChallenge.infrastructure.entities.restaurant;

import com.github.techChallenge.domain.restaurant.Restaurant;
import com.github.techChallenge.infrastructure.entities.restaurantCategory.RestaurantCategoryEntity;
import com.github.techChallenge.infrastructure.entities.user.AddressEntity;
import com.github.techChallenge.infrastructure.entities.user.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Table(name = "restaurantes", uniqueConstraints = @UniqueConstraint(name = "uk_restaurante_nome", columnNames = "nome_normalizado"))
public class RestaurantEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nome", nullable = false, length = 100)
    private String name;
    @Column(name = "nome_normalizado", nullable = false, length = 100)
    private String nameKey;
    @Embedded
    private AddressEntity address;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private RestaurantCategoryEntity category;
    @Column(name = "horario_funcionamento", nullable = false)
    private String openingHours;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dono_id", nullable = false)
    private UserEntity owner;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected RestaurantEntity() {}

    public RestaurantEntity(Restaurant restaurant, UserEntity owner, RestaurantCategoryEntity category) {
        this.owner = owner;
        this.createdAt = restaurant.getCreatedAt();
        update(restaurant, category);
    }

    public void update(Restaurant restaurant, RestaurantCategoryEntity category) {
        this.name = restaurant.getName();
        this.nameKey = restaurant.getName().trim().toLowerCase(Locale.ROOT);
        this.address = new AddressEntity(restaurant.getAddress());
        this.category = category;
        this.openingHours = restaurant.getOpeningHours();
        this.updatedAt = restaurant.getUpdatedAt();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public AddressEntity getAddress() { return address; }
    public RestaurantCategoryEntity getCategory() { return category; }
    public String getOpeningHours() { return openingHours; }
    public UserEntity getOwner() { return owner; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
