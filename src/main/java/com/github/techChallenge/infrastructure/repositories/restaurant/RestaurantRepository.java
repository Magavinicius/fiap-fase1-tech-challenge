package com.github.techChallenge.infrastructure.repositories.restaurant;

import com.github.techChallenge.infrastructure.entities.restaurant.RestaurantEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<RestaurantEntity, Long> {
    boolean existsByNameKey(String nameKey);
    boolean existsByNameKeyAndIdNot(String nameKey, Long id);
    Page<RestaurantEntity> findByCategory_Id(Integer categoryId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RestaurantEntity r where r.id = :id")
    Optional<RestaurantEntity> findForUpdate(@Param("id") Long id);
}
