package com.github.techChallenge.application.usecases.restaurantCategory;


import com.github.techChallenge.application.exceptions.DuplicateRestaurantCategoryTitleException;
import com.github.techChallenge.application.exceptions.RestaurantCategoryNotFoundException;
import com.github.techChallenge.application.gateways.restaurantCategory.IRestaurantCategoryGateway;
import com.github.techChallenge.application.validators.RestaurantCategoryValidator;
import com.github.techChallenge.domain.restaurantCategory.IRestaurantCategoryMapper;
import com.github.techChallenge.domain.restaurantCategory.RestaurantCategory;
import com.github.techChallenge.domain.restaurantCategory.dto.RestaurantCategoryCreateInputDTO;
import com.github.techChallenge.domain.restaurantCategory.dto.RestaurantCategoryOutputDTO;
import com.github.techChallenge.domain.restaurantCategory.dto.RestaurantCategoryUpdateInputDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class restaurantCategoryTest {

    @Mock
    IRestaurantCategoryGateway gateway;

    @Mock
    IRestaurantCategoryMapper mapper;

    @Mock
    RestaurantCategoryValidator restaurantCategoryValidator;

    @Autowired
    RestaurantCategory restaurantCategory = new RestaurantCategory(1, "Cafeteria");

    RestaurantCategoryOutputDTO dtoResponse = new RestaurantCategoryOutputDTO(1, "Cafeteria");



    @Test
    @DisplayName("Deve fazer o caso de uso de criação da categoria do restaurante")
    void shouldDoCreateUseCase(){
        RestaurantCategoryCreateInputDTO dtoRequest = new RestaurantCategoryCreateInputDTO("Cafeteria");
        CreateRestaurantCategoryUseCase createRestaurantCategoryUseCase = new CreateRestaurantCategoryUseCase(gateway, mapper);

        when(gateway.titleExists("Cafeteria")).thenReturn(false);
        when(gateway.create(any(RestaurantCategory.class))).thenReturn(restaurantCategory);
        when(mapper.fromDomainToOutputDTO(restaurantCategory)).thenReturn(dtoResponse);

        RestaurantCategoryOutputDTO dtoResponse = createRestaurantCategoryUseCase.execute(dtoRequest);

        assertNotNull(dtoResponse);
        assertEquals(1, dtoResponse.id());
        assertEquals(dtoRequest.title(), dtoResponse.title());
        assertEquals("Cafeteria", dtoResponse.title());
    }

    @Test
    @DisplayName("Não deve fazer o caso de uso de criação da categoria do restaurante")
    void shouldDoNotCreateUseCase(){
        RestaurantCategoryCreateInputDTO dtoRequest = new RestaurantCategoryCreateInputDTO("Cafeteria");
        CreateRestaurantCategoryUseCase createRestaurantCategoryUseCase = new CreateRestaurantCategoryUseCase(gateway, mapper);

        when(gateway.titleExists("Cafeteria")).thenReturn(true);


        var e = assertThrows(DuplicateRestaurantCategoryTitleException.class,
                () -> createRestaurantCategoryUseCase.execute(dtoRequest));
        assertEquals("O nome da categoria de restaurante Cafeteria já está cadastrado no sistema", e.getMessage());
    }

    @Test
    @DisplayName("Deve fazer o caso de uso de atualização da categoria do restaurante")
    void shouldDoUpdateUseCase(){
        RestaurantCategoryUpdateInputDTO dtoRequest = new RestaurantCategoryUpdateInputDTO("Cafeteria");

        UpdateRestaurantCategoryUseCase updateRestaurantCategoryUseCase
                = new UpdateRestaurantCategoryUseCase(gateway, mapper, restaurantCategoryValidator);

        when(gateway.existById(1)).thenReturn(true);
        when(gateway.update(any(RestaurantCategory.class), any(int.class))).thenReturn(restaurantCategory);
        when(mapper.fromDomainToOutputDTO(restaurantCategory)).thenReturn(dtoResponse);
        when(restaurantCategoryValidator.titleExists("Cafeteria", 1)).thenReturn(false);


        RestaurantCategoryOutputDTO dtoResponse = updateRestaurantCategoryUseCase.execute(dtoRequest, 1);

        assertNotNull(dtoResponse);
        assertEquals(1, dtoResponse.id());
        assertEquals(dtoRequest.title(), dtoResponse.title());
        assertEquals("Cafeteria", dtoResponse.title());
    }

    @Test
    @DisplayName("Não deve fazer o caso de uso de atualização da categoria do restaurante por não achar ID")
    void shouldDoNotUpdateUseCaseIdNotExist(){
        RestaurantCategoryUpdateInputDTO dtoRequest = new RestaurantCategoryUpdateInputDTO("Cafeteria");

        UpdateRestaurantCategoryUseCase updateRestaurantCategoryUseCase
                = new UpdateRestaurantCategoryUseCase(gateway, mapper, restaurantCategoryValidator);

        when(gateway.existById(1)).thenReturn(false);




        var e = assertThrows(RestaurantCategoryNotFoundException.class,
                () -> updateRestaurantCategoryUseCase.execute(dtoRequest, 1));
        assertEquals("Código da categoria do restaurante não foi localizado", e.getMessage());
    }

    @Test
    @DisplayName("Não deve fazer o caso de uso de atualização da categoria do restaurante por já ter o titulo")
    void shouldDoNotUpdateUseCaseTitleAlreadyExist(){
        RestaurantCategoryUpdateInputDTO dtoRequest = new RestaurantCategoryUpdateInputDTO("Cafeteria");

        UpdateRestaurantCategoryUseCase updateRestaurantCategoryUseCase
                = new UpdateRestaurantCategoryUseCase(gateway, mapper, restaurantCategoryValidator);

        when(gateway.existById(1)).thenReturn(true);
        when(restaurantCategoryValidator.titleExists(dtoRequest.title(), 1)).thenReturn(true);


        var e = assertThrows(DuplicateRestaurantCategoryTitleException.class,
                () -> updateRestaurantCategoryUseCase.execute(dtoRequest, 1));
        assertEquals("O nome da categoria de restaurante " + dtoRequest.title() + " já está cadastrado no sistema", e.getMessage());
    }

    @Test
    @DisplayName("Deve fazer o caso de uso de buscar a categoria do restaurante por id")
    void shouldDoFindUseCase(){


        FindRestaurantCategoryUseCase findRestaurantCategoryUseCase
                = new FindRestaurantCategoryUseCase(gateway, mapper);

        when(gateway.find(any(int.class))).thenReturn(restaurantCategory);
        when(mapper.fromDomainToOutputDTO(restaurantCategory)).thenReturn(dtoResponse);


        RestaurantCategoryOutputDTO dtoResponse = findRestaurantCategoryUseCase.execute(1);

        assertNotNull(dtoResponse);
        assertEquals(1, dtoResponse.id());
        assertNotNull(dtoResponse.title());

    }


    @Test
    @DisplayName("Deve fazer o caso de uso de deletar a categoria do restaurante por id")
    void shouldDoDeleteUseCase(){

        DeleteRestaurantCategoryUseCase deleteRestaurantCategoryUseCase
                = new DeleteRestaurantCategoryUseCase(gateway, mapper);

        when(gateway.existById(1)).thenReturn(true);

        deleteRestaurantCategoryUseCase.execute(1);

        verify(gateway).delete(1);



    }

    @Test
    @DisplayName("Não deve fazer o caso de uso de deletar a categoria do restaurante por id por não achar o Id")
    void shouldDoNotDeleteUseCase(){

        DeleteRestaurantCategoryUseCase deleteRestaurantCategoryUseCase
                = new DeleteRestaurantCategoryUseCase(gateway, mapper);

        when(gateway.existById(1)).thenReturn(false);

        var e = assertThrows(RestaurantCategoryNotFoundException.class,
                () -> deleteRestaurantCategoryUseCase.execute(1));
        assertEquals("Código da categoria do restaurante não foi localizado", e.getMessage());



    }

    @Test
    @DisplayName("Deve fazer o caso de uso de listar as categorias de restaurante")
    void shouldListRestaurantCategories() {

        var churrascaria = new RestaurantCategory(1, "Churrascaria");
        var cafeteria    = new RestaurantCategory(2, "Cafeteria");

        Page<RestaurantCategory> domainPage = new PageImpl<>(List.of(churrascaria, cafeteria));
        Page<RestaurantCategoryOutputDTO> outputPage = new PageImpl<>(List.of(
                new RestaurantCategoryOutputDTO(1, "Churrascaria"),
                new RestaurantCategoryOutputDTO(2, "Cafeteria")
        ));

        when(gateway.list(0, 10)).thenReturn(domainPage);
        when(mapper.fromDomainPageToOutputDTOPage(domainPage)).thenReturn(outputPage);


        Page<RestaurantCategoryOutputDTO> result =
                new ListRestaurantCategoryUseCase(gateway, mapper).execute(0, 10);


        assertEquals(outputPage, result);
        verify(gateway).list(0, 10);
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não houver categorias")
    void shouldReturnEmptyPageWhenNoCategories() {

        Page<RestaurantCategory> emptyDomainPage = Page.empty();
        Page<RestaurantCategoryOutputDTO> emptyOutputPage = Page.empty();

        when(gateway.list(0, 10)).thenReturn(emptyDomainPage);
        when(mapper.fromDomainPageToOutputDTOPage(emptyDomainPage)).thenReturn(emptyOutputPage);


        Page<RestaurantCategoryOutputDTO> result =
                new ListRestaurantCategoryUseCase(gateway, mapper).execute(0, 10);


        assertTrue(result.isEmpty());
    }
}
