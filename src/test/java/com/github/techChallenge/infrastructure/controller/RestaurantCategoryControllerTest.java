package com.github.techChallenge.infrastructure.controller;


import com.github.techChallenge.application.exceptions.GlobalExceptionHandler;
import com.github.techChallenge.application.exceptions.ProblemDetailFactory;
import com.github.techChallenge.application.usecases.restaurantCategory.*;
import com.github.techChallenge.application.validators.RestaurantCategoryValidator;
import com.github.techChallenge.domain.restaurantCategory.RestaurantCategory;
import com.github.techChallenge.domain.restaurantCategory.dto.RestaurantCategoryCreateInputDTO;
import com.github.techChallenge.domain.restaurantCategory.dto.RestaurantCategoryCreateOutputDTO;
import com.github.techChallenge.domain.restaurantCategory.dto.RestaurantCategoryOutputDTO;
import com.github.techChallenge.domain.restaurantCategory.dto.RestaurantCategoryUpdateInputDTO;
import com.github.techChallenge.infrastructure.controllers.restaurantCategory.RestaurantCategoryController;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.awt.print.Pageable;
import java.util.List;

import static org.hamcrest.Matchers.instanceOf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;



public class RestaurantCategoryControllerTest {


    private AutoCloseable mocks;

    private MockMvc mockMvc;

    @Mock
    private CreateRestaurantCategoryUseCase createRestaurantCategoryUseCase;

    @Mock
    private UpdateRestaurantCategoryUseCase updateRestaurantCategoryUseCase;

    @Mock
    private FindRestaurantCategoryUseCase findRestaurantCategoryUseCase;

    @Mock
    private ListRestaurantCategoryUseCase listRestaurantCategoryUseCase;

    @Mock
    private DeleteRestaurantCategoryUseCase deleteRestaurantCategoryUseCase;

    @Mock
    private RestaurantCategoryValidator restaurantCategoryValidator;

    @Mock
    private ProblemDetailFactory problemDetailFactory;

    @InjectMocks
    private RestaurantCategoryController restaurantCategoryController;

    public RestaurantCategoryControllerTest() {
    }

    @BeforeEach
    void setup(){
        mocks = MockitoAnnotations.openMocks(this);

        RestaurantCategoryController restaurantCategoryController = new RestaurantCategoryController(createRestaurantCategoryUseCase,
                updateRestaurantCategoryUseCase, findRestaurantCategoryUseCase, listRestaurantCategoryUseCase,
                deleteRestaurantCategoryUseCase, restaurantCategoryValidator);

        mockMvc = MockMvcBuilders.standaloneSetup(restaurantCategoryController)
                .setControllerAdvice(new GlobalExceptionHandler(new ProblemDetailFactory()))
                .addFilter((request, response, chain) -> {
                    response.setCharacterEncoding("UTF-8");
                    chain.doFilter(request, response);
                }, "/*")
                .build();
    }

    @AfterEach
    void tearDown() throws Exception{
        mocks.close();
    }

    @Test
    @DisplayName("Deve criar uma categoria de restaurante e retornar 201")
    void shouldCreateCategoryController() throws Exception {
        var dto = new RestaurantCategoryCreateInputDTO("Restaurante Coreano");

        when(createRestaurantCategoryUseCase.execute(any(RestaurantCategoryCreateInputDTO.class)))
                .thenReturn(new RestaurantCategoryOutputDTO(1, "Restaurante Coreano"));

        mockMvc.perform(post("/api/v1/restaurant-category/")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(asJsonString(dto)))
                .andDo(print())
                .andExpect(status().isCreated());

        verify(createRestaurantCategoryUseCase, times(1))
                .execute(any(RestaurantCategoryCreateInputDTO.class));
    }


    @Test
    @DisplayName("Deve atualizar uma categoria de restaurante e retornar 200")
    void shouldUpdateCategoryController() throws Exception {
        var dto = new RestaurantCategoryUpdateInputDTO("Restaurante Coreano");

        when(updateRestaurantCategoryUseCase.execute(any(RestaurantCategoryUpdateInputDTO.class), eq( 1)))
                .thenReturn(new RestaurantCategoryOutputDTO(1, "Restaurante Coreano"));

        mockMvc.perform(put("/api/v1/restaurant-category/{id}", 1)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content(asJsonString(dto)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(updateRestaurantCategoryUseCase, times(1))
                .execute(any(RestaurantCategoryUpdateInputDTO.class), eq(1));
    }


    @Test
    @DisplayName("Deve encontrar uma categoria de restaurante pelo id")
    void shouldFindByIdCategoryController() throws Exception {


        when(findRestaurantCategoryUseCase.execute(eq( 1)))
                .thenReturn(new RestaurantCategoryOutputDTO(1, "Restaurante Coreano"));

        mockMvc.perform(get("/api/v1/restaurant-category/{id}", 1)
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(findRestaurantCategoryUseCase, times(1))
                .execute(eq(1));
    }

    private static String asJsonString(Object obj) {
        return new ObjectMapper().writeValueAsString(obj);
    }


    @Test
    @DisplayName("Deve listar todas as categoria de restaurante")
    void shouldListAllCategoryController() throws Exception {

        var content = List.of(
                new RestaurantCategoryOutputDTO(1, "Churrascaria"),
                new RestaurantCategoryOutputDTO(2, "Cafeteria")
        );
        var page = new PageImpl<>(content, PageRequest.of(0, 10), content.size());

        when(listRestaurantCategoryUseCase.execute(eq(0), eq(10)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/restaurant-category/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Churrascaria"))
                .andExpect(jsonPath("$.content[1].title").value("Cafeteria"));

        verify(listRestaurantCategoryUseCase, times(1))
                .execute(eq(0), eq(10));
    }

    @Test
    @DisplayName("Deve listar todas as categoria de restaurante")
    void shouldDeleteCategoryController() throws Exception {

        mockMvc.perform(delete("/api/v1/restaurant-category/{id}", 1))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(deleteRestaurantCategoryUseCase, times(1)).execute(1);
    }
}
