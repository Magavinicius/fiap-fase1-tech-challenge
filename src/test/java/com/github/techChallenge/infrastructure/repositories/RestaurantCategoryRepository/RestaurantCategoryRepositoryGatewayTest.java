package com.github.techChallenge.infrastructure.repositories.RestaurantCategoryRepository;


import com.github.techChallenge.application.exceptions.RestaurantCategoryNotFoundException;
import com.github.techChallenge.application.exceptions.UserTypeNotFoundException;
import com.github.techChallenge.domain.restaurantCategory.RestaurantCategory;
import com.github.techChallenge.infrastructure.entities.restaurantCategory.RestaurantCategoryEntity;
import com.github.techChallenge.infrastructure.repositories.restaurantCategory.RestaurantCategoryRepositoryGateway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = {"/00_cleanup-tables.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"/00_cleanup-tables.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@Transactional
public class RestaurantCategoryRepositoryGatewayTest {


    @Autowired
    RestaurantCategoryRepositoryGateway restaurantCategoryRepositoryGateway;



    @Test
    @DisplayName("Deve criar uma categoria de restaurante no Banco de Dados")
    public void shouldCreateRestaurantCategoryOnDataBase() {

        RestaurantCategory restaurantCategory = RestaurantCategory.create("Churrascaria");

        RestaurantCategory created = restaurantCategoryRepositoryGateway.create(restaurantCategory);

        assertNotNull(created);
        assertEquals(created.getTitle(), restaurantCategory.getTitle());
        assertEquals("Churrascaria", created.getTitle());
    }

    @Test
    @DisplayName("Deve alterar uma categoria de restaurante no Banco de Dados")
    @Sql(scripts = "/01_users_type_samples.sql")
    @Sql(scripts = "/03_restaurante_category_samples.sql")
    @Sql(scripts = {"/00_cleanup-tables.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldUpdateRestaurantCategoryOnDataBase() {

        RestaurantCategory restaurantCategory = RestaurantCategory.create("Cafeteria");

        RestaurantCategory updated = restaurantCategoryRepositoryGateway.update(restaurantCategory, 1);

        assertNotNull(updated);
        assertEquals(updated.getTitle(), restaurantCategory.getTitle());
        assertEquals("Cafeteria", updated.getTitle());
        assertNotEquals("Churrascaria", updated.getTitle());
    }


    @Test
    @DisplayName("Deve encontrar uma categoria de restaurante no Banco de Dados")
    @Sql(scripts = "/01_users_type_samples.sql")
    @Sql(scripts = "/03_restaurante_category_samples.sql")
    @Sql(scripts = {"/00_cleanup-tables.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldFindByIdRestaurantCategoryOnDataBase() {

        RestaurantCategory findable = restaurantCategoryRepositoryGateway.findByID(1);

        assertNotNull(findable);
        assertNotNull(findable.getId());
        assertNotNull(findable.getTitle());
        assertEquals(findable.getId(), 1);

    }

    @Test
    @DisplayName("Deve listar as categoria de restaurante no Banco de Dados")
    @Sql(scripts = "/01_users_type_samples.sql")
    @Sql(scripts = "/03_restaurante_category_samples.sql")
    @Sql(scripts = {"/00_cleanup-tables.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldListRestaurantCategoryOnDataBase() {

        Page<RestaurantCategory> restaurantCategoryEntitys
                = restaurantCategoryRepositoryGateway.list(0, 10);

        assertNotNull(restaurantCategoryEntitys);
        restaurantCategoryEntitys.stream().forEach(restaurantCategoryEntity -> {
            assertNotNull(restaurantCategoryEntity.getTitle());
            assertNotNull(restaurantCategoryEntity.getId());
        });

        var v1 = restaurantCategoryEntitys.getContent().getFirst();
        var v2 = restaurantCategoryEntitys.getContent().getLast();

        assertEquals(v1.getTitle(), "Churrascaria");
        assertEquals(v1.getId(), 1);
        assertEquals(v2.getTitle(), "Cafeteria");
        assertEquals(v2.getId(), 2);
    }

    @Test
    @DisplayName("Deve deletar as categoria de restaurante no Banco de Dados e lançar a exceção caso não seja encontrado")
    @Sql(scripts = "/01_users_type_samples.sql")
    @Sql(scripts = "/03_restaurante_category_samples.sql")
    @Sql(scripts = {"/00_cleanup-tables.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldDeleteRestaurantCategoryOnDataBase() {
        int id = 1;

        restaurantCategoryRepositoryGateway.delete(id);

        RestaurantCategoryNotFoundException e = assertThrows(RestaurantCategoryNotFoundException.class,
                () -> restaurantCategoryRepositoryGateway.findByID(id)
        );
        assertEquals("Tipo de restaurante não encontrado", e.getMessage());
    }

    @Test
    @DisplayName("Deve retornar true se encontrar o nome da categoria de restaurante no Banco de Dados")
    @Sql(scripts = "/03_restaurante_category_samples.sql")
    @Sql(scripts = {"/00_cleanup-tables.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldReturnTrueIfFindByTitleRestaurantCategoryOnDataBase() {

        assertTrue(restaurantCategoryRepositoryGateway.existsByTitle("Churrascaria"));

    }

    @Test
    @DisplayName("Deve retornar false se não encontrar o nome da categoria de restaurante no Banco de Dados")
    @Sql(scripts = "/03_restaurante_category_samples.sql")
    @Sql(scripts = {"/00_cleanup-tables.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldReturnFalseIfNotFindByTitleRestaurantCategoryOnDataBase() {

        assertFalse(restaurantCategoryRepositoryGateway.existsByTitle("Barraca de Hot Dog"));

    }

    @Test
    @DisplayName("Deve retornar true se existir outra categoria com o mesmo nome e id diferente no Banco de Dados")
    @Sql(scripts = "/03_restaurante_category_samples.sql")
    @Sql(scripts = {"/00_cleanup-tables.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldReturnTrueIfFindByTitleAndIdNotRestaurantCategoryOnDataBase() {


        assertTrue(restaurantCategoryRepositoryGateway.existsByTitleAndIdNot("Churrascaria", 999));

    }

    @Test
    @DisplayName("Deve retornar false se o nome pertencer apenas à própria categoria (mesmo id) no Banco de Dados")
    @Sql(scripts = "/03_restaurante_category_samples.sql")
    @Sql(scripts = {"/00_cleanup-tables.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldReturnFalseIfTitleBelongsToSameIdRestaurantCategoryOnDataBase() {

        assertFalse(restaurantCategoryRepositoryGateway.existsByTitleAndIdNot("Churrascaria", 1));

    }

    @Test
    @DisplayName("Deve retornar false se não encontrar o nome da categoria com id diferente no Banco de Dados")
    @Sql(scripts = "/03_restaurante_category_samples.sql")
    @Sql(scripts = {"/00_cleanup-tables.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldReturnFalseIfNotFindByTitleAndIdNotRestaurantCategoryOnDataBase() {

        assertFalse(restaurantCategoryRepositoryGateway.existsByTitleAndIdNot("Barraca de Hot Dog", 999));
    }

    @Test
    @DisplayName("Deve retornar true se encontrar a categoria de restaurante pelo id no Banco de Dados")
    @Sql(scripts = "/03_restaurante_category_samples.sql")
    @Sql(scripts = {"/00_cleanup-tables.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldReturnTrueIfFindByIdRestaurantCategoryOnDataBase() {

        assertTrue(restaurantCategoryRepositoryGateway.existsById(1));

    }

    @Test
    @DisplayName("Deve retornar false se não encontrar a categoria de restaurante pelo id no Banco de Dados")
    @Sql(scripts = "/03_restaurante_category_samples.sql")
    public void shouldReturnFalseIfNotFindByIdRestaurantCategoryOnDataBase() {

        assertFalse(restaurantCategoryRepositoryGateway.existsById(999));

    }

}
