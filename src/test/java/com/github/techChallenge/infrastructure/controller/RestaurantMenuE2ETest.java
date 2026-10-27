package com.github.techChallenge.infrastructure.controller;

import com.github.techChallenge.domain.restaurantCategory.RestaurantCategory;
import com.github.techChallenge.domain.user.*;
import com.github.techChallenge.infrastructure.entities.restaurantCategory.RestaurantCategoryEntity;
import com.github.techChallenge.infrastructure.entities.user.UserEntity;
import com.github.techChallenge.infrastructure.repositories.menuItem.MenuItemMongoRepository;
import com.github.techChallenge.infrastructure.repositories.restaurant.RestaurantRepository;
import com.github.techChallenge.infrastructure.repositories.restaurantCategory.RestaurantCategoryRepository;
import com.github.techChallenge.infrastructure.repositories.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RestaurantMenuE2ETest {
    private static final String PASSWORD = "Senha@123";
    @Autowired Environment environment;
    @Autowired UserRepository users;
    @Autowired RestaurantCategoryRepository categories;
    @Autowired RestaurantRepository restaurants;
    @Autowired MenuItemMongoRepository items;
    @Autowired MongoTemplate mongo;
    @Autowired JdbcTemplate jdbc;
    @Autowired ObjectMapper json;
    private final HttpClient http = HttpClient.newHttpClient();
    private UserEntity owner;
    private UserEntity otherOwner;
    private RestaurantCategoryEntity cuisine;

    @BeforeEach void seedExistingUsersAndCuisine() {
        assertEquals("fiapdb_test", jdbc.queryForObject("select database()", String.class), "Use o MySQL de testes.");
        assertEquals("fiapfase2_test", mongo.getDb().getName(), "Use o MongoDB de testes.");
        cleanup();
        owner = createUser("owner.one", UserLevel.OWNER);
        otherOwner = createUser("owner.two", UserLevel.OWNER);
        createUser("customer", UserLevel.CUSTOMER);
        cuisine = categories.saveAndFlush(new RestaurantCategoryEntity(RestaurantCategory.create("Italiana")));
    }

    @AfterEach void cleanup() {
        items.deleteAll();
        restaurants.deleteAll();
        categories.deleteAll();
        users.deleteAll();
    }

    private UserEntity createUser(String login, UserLevel level) {
        var user = User.create(login, login + "@email.com", login, PASSWORD, level,
                new Address("Rua A", "10", null, "01000-000", "Centro", "São Paulo", "SP", "Brasil"));
        user.encodePassword(new BCryptPasswordEncoder(4)::encode);
        return users.saveAndFlush(new UserEntity(user));
    }

    private Map<String, Object> restaurantBody(String name, int categoryId) {
        return new LinkedHashMap<>(Map.of("name", name, "categoryId", categoryId, "openingHours", "Segunda a sábado, 11h às 23h",
                "address", Map.of("address", "Rua A", "number", "10", "complement", "Casa", "zipCode", "01000-000",
                        "neighborhood", "Centro", "city", "São Paulo", "state", "SP", "country", "Brasil")));
    }

    private Map<String, Object> itemBody(Long restaurantId) {
        return new LinkedHashMap<>(Map.of("name", "Prato", "description", "Prato da casa", "price", new BigDecimal("29.90"),
                "dineInOnly", false, "photoPath", "/fotos/prato.png", "restaurantId", restaurantId));
    }

    private HttpResponse<String> request(String method, String path, Object body, String login) throws Exception {
        return request(method, path, body, login, PASSWORD);
    }

    private HttpResponse<String> request(String method, String path, Object body, String login, String password) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + environment.getProperty("local.server.port") + path))
                .header("Accept", "application/json");
        if (login != null) builder.header("Authorization", "Basic " + Base64.getEncoder()
                .encodeToString((login + ":" + password).getBytes(StandardCharsets.UTF_8)));
        if (body != null) builder.header("Content-Type", "application/json");
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private JsonNode body(HttpResponse<String> response) { return json.readTree(response.body()); }

    private void problem(HttpResponse<String> response, int expectedStatus) {
        assertEquals(expectedStatus, response.statusCode(), response.body());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("application/problem+json"));
        assertEquals(expectedStatus, body(response).path("status").asInt());
        assertFalse(body(response).path("code").asText().isBlank());
    }

    private Long createRestaurant(String name, String login) throws Exception {
        var response = request("POST", "/api/v1/restaurant", restaurantBody(name, cuisine.getId()), login);
        assertEquals(201, response.statusCode(), response.body());
        assertTrue(response.headers().firstValue("Location").orElse("").contains("/api/v1/restaurant/"));
        return body(response).path("id").asLong();
    }

    private Long createItem(Long restaurantId) throws Exception {
        var response = request("POST", "/api/v1/menu-item", itemBody(restaurantId), "owner.one");
        assertEquals(201, response.statusCode(), response.body());
        return body(response).path("id").asLong();
    }

    @Test void shouldExecuteBothCompleteCRUDsOverHttpAndRealDatabases() throws Exception {
        Long restaurantId = createRestaurant("Restaurante", "owner.one");
        var foundRestaurant = request("GET", "/api/v1/restaurant/" + restaurantId, null, null);
        assertEquals(200, foundRestaurant.statusCode());
        assertEquals(owner.getId(), body(foundRestaurant).path("ownerId").asLong());
        var restaurantUpdate = restaurantBody("Restaurante atualizado", cuisine.getId());
        assertEquals(200, request("PUT", "/api/v1/restaurant/" + restaurantId, restaurantUpdate, "owner.one").statusCode());
        Long itemId = createItem(restaurantId);
        var menu = request("GET", "/api/v1/menu-item?restaurantId=" + restaurantId, null, null);
        assertEquals(200, menu.statusCode());
        assertEquals(itemId, body(menu).path("content").get(0).path("id").asLong());
        var itemUpdate = itemBody(restaurantId);
        itemUpdate.remove("restaurantId");
        itemUpdate.put("name", "Prato atualizado");
        itemUpdate.put("price", new BigDecimal("32.50"));
        itemUpdate.put("dineInOnly", true);
        var updated = request("PUT", "/api/v1/menu-item/" + itemId, itemUpdate, "owner.one");
        assertEquals(200, updated.statusCode(), updated.body());
        assertEquals(restaurantId, body(updated).path("restaurantId").asLong());
        assertTrue(body(updated).path("dineInOnly").asBoolean());
        var found = request("GET", "/api/v1/menu-item/" + itemId, null, null);
        assertEquals("Prato atualizado", body(found).path("name").asText());
        assertEquals(0, new BigDecimal("32.50").compareTo(body(found).path("price").decimalValue()));
        problem(request("DELETE", "/api/v1/restaurant/" + restaurantId, null, "owner.one"), 409);
        assertEquals(204, request("DELETE", "/api/v1/menu-item/" + itemId, null, "owner.one").statusCode());
        assertEquals(204, request("DELETE", "/api/v1/restaurant/" + restaurantId, null, "owner.one").statusCode());
        problem(request("GET", "/api/v1/restaurant/" + restaurantId, null, null), 404);
        problem(request("GET", "/api/v1/menu-item/" + itemId, null, null), 404);
    }

    @Test void shouldRequireAuthenticationForWrites() throws Exception {
        problem(request("POST", "/api/v1/restaurant", restaurantBody("R", cuisine.getId()), null), 401);
        problem(request("POST", "/api/v1/menu-item", itemBody(1L), null), 401);
        assertEquals(0, restaurants.count());
        assertEquals(0, items.count());
    }

    @Test void shouldRejectIncorrectAndUnknownCredentials() throws Exception {
        problem(request("POST", "/api/v1/restaurant", restaurantBody("R", cuisine.getId()), "owner.one", "errada"), 401);
        problem(request("POST", "/api/v1/restaurant", restaurantBody("R", cuisine.getId()), "inexistente"), 401);
    }

    @Test void shouldDenyCustomersEvenWhenTheyKnowTheRestaurantId() throws Exception {
        Long id = createRestaurant("Restaurante", "owner.one");
        problem(request("POST", "/api/v1/restaurant", restaurantBody("Outro", cuisine.getId()), "customer"), 403);
        problem(request("POST", "/api/v1/menu-item", itemBody(id), "customer"), 403);
        assertEquals(1, restaurants.count());
        assertEquals(0, items.count());
    }

    @Test void shouldDenyAnotherOwnerForEveryMutationOfTheRestaurantAndMenu() throws Exception {
        Long restaurantId = createRestaurant("Restaurante", "owner.one");
        Long itemId = createItem(restaurantId);
        problem(request("POST", "/api/v1/menu-item", itemBody(restaurantId), "owner.two"), 403);
        problem(request("PUT", "/api/v1/menu-item/" + itemId, itemBody(restaurantId), "owner.two"), 403);
        problem(request("DELETE", "/api/v1/menu-item/" + itemId, null, "owner.two"), 403);
        problem(request("PUT", "/api/v1/restaurant/" + restaurantId, restaurantBody("Invasão", cuisine.getId()), "owner.two"), 403);
        problem(request("DELETE", "/api/v1/restaurant/" + restaurantId, null, "owner.two"), 403);
        assertEquals("Restaurante", restaurants.findById(restaurantId).orElseThrow().getName());
        assertEquals("Prato", items.findById(itemId).orElseThrow().getName());
    }

    @Test void shouldAssignTheOwnerFromAuthenticatedCredentials() throws Exception {
        Long first = createRestaurant("Primeiro", "owner.one");
        Long second = createRestaurant("Segundo", "owner.two");
        assertEquals(owner.getId(), body(request("GET", "/api/v1/restaurant/" + first, null, null)).path("ownerId").asLong());
        assertEquals(otherOwner.getId(), body(request("GET", "/api/v1/restaurant/" + second, null, null)).path("ownerId").asLong());
    }

    @Test void shouldReturnAllRestaurantValidationViolationsWith400() throws Exception {
        var payload = restaurantBody("", cuisine.getId());
        payload.put("address", null);
        payload.put("categoryId", null);
        payload.put("openingHours", "");
        var response = request("POST", "/api/v1/restaurant", payload, "owner.one");
        problem(response, 400);
        assertTrue(body(response).path("violations").has("name"));
        assertTrue(body(response).path("violations").has("address"));
        assertTrue(body(response).path("violations").has("categoryId"));
        assertTrue(body(response).path("violations").has("openingHours"));
        assertEquals(0, restaurants.count());
    }

    @Test void shouldRejectUnknownCuisineWith404() throws Exception {
        problem(request("POST", "/api/v1/restaurant", restaurantBody("R", Integer.MAX_VALUE), "owner.one"), 404);
    }

    @Test void shouldRejectDuplicateRestaurantNamesWith409() throws Exception {
        createRestaurant("Restaurante", "owner.one");
        problem(request("POST", "/api/v1/restaurant", restaurantBody(" RESTAURANTE ", cuisine.getId()), "owner.one"), 409);
    }

    @Test void shouldRejectUnknownRestaurantWhenCreatingOrListingItems() throws Exception {
        problem(request("POST", "/api/v1/menu-item", itemBody(Long.MAX_VALUE), "owner.one"), 404);
        problem(request("GET", "/api/v1/menu-item?restaurantId=" + Long.MAX_VALUE, null, null), 404);
    }

    @ParameterizedTest @ValueSource(strings = {"0", "-1", "1.001"})
    void shouldRejectInvalidPricesWithoutSaving(String price) throws Exception {
        Long restaurantId = createRestaurant("Restaurante", "owner.one");
        var payload = itemBody(restaurantId);
        payload.put("price", new BigDecimal(price));
        var response = request("POST", "/api/v1/menu-item", payload, "owner.one");
        problem(response, 400);
        assertTrue(body(response).path("violations").has("price"));
        assertEquals(0, items.count());
    }

    @Test void shouldReportMissingAvailabilityWithoutThrowingNullPointerException() throws Exception {
        Long restaurantId = createRestaurant("Restaurante", "owner.one");
        var payload = itemBody(restaurantId);
        payload.remove("dineInOnly");
        var response = request("POST", "/api/v1/menu-item", payload, "owner.one");
        problem(response, 400);
        assertTrue(body(response).path("violations").has("dineInOnly"));
    }

    @Test void shouldScopeDuplicateItemNamesToOneRestaurant() throws Exception {
        Long first = createRestaurant("Primeiro", "owner.one");
        Long second = createRestaurant("Segundo", "owner.one");
        createItem(first);
        var duplicate = itemBody(first);
        duplicate.put("name", " PRATO ");
        problem(request("POST", "/api/v1/menu-item", duplicate, "owner.one"), 409);
        assertEquals(201, request("POST", "/api/v1/menu-item", itemBody(second), "owner.one").statusCode());
    }

    @Test void shouldFilterRestaurantsByCuisine() throws Exception {
        Long first = createRestaurant("Primeiro", "owner.one");
        var otherCuisine = categories.saveAndFlush(new RestaurantCategoryEntity(RestaurantCategory.create("Japonesa")));
        var other = request("POST", "/api/v1/restaurant", restaurantBody("Segundo", otherCuisine.getId()), "owner.one");
        assertEquals(201, other.statusCode());
        var filtered = request("GET", "/api/v1/restaurant?categoryId=" + cuisine.getId(), null, null);
        assertEquals(200, filtered.statusCode());
        assertEquals(1, body(filtered).path("totalElements").asInt());
        assertEquals(first, body(filtered).path("content").get(0).path("id").asLong());
        problem(request("GET", "/api/v1/restaurant?categoryId=" + Integer.MAX_VALUE, null, null), 404);
    }

    @Test void shouldRejectInvalidPaginationWith400() throws Exception {
        problem(request("GET", "/api/v1/restaurant?page=-1", null, null), 400);
        problem(request("GET", "/api/v1/restaurant?offset=101", null, null), 400);
        problem(request("GET", "/api/v1/menu-item?restaurantId=1&offset=0", null, null), 400);
    }

    @Test void shouldReturn404ForMissingResourcesOnReadsAndWrites() throws Exception {
        problem(request("GET", "/api/v1/menu-item/" + Long.MAX_VALUE, null, null), 404);
        problem(request("PUT", "/api/v1/menu-item/" + Long.MAX_VALUE, itemBody(1L), "owner.one"), 404);
        problem(request("DELETE", "/api/v1/menu-item/" + Long.MAX_VALUE, null, "owner.one"), 404);
        problem(request("DELETE", "/api/v1/restaurant/" + Long.MAX_VALUE, null, "owner.one"), 404);
    }

    @Test void shouldPreventChangingAnotherOwnersPasswordOrProfile() throws Exception {
        var password = Map.of("login", "owner.two", "password", "OutraSenha@123");
        problem(request("PATCH", "/api/v1/user/change-password/" + otherOwner.getId(), password, null), 401);
        problem(request("PATCH", "/api/v1/user/change-password/" + otherOwner.getId(), password, "owner.one"), 403);
        problem(request("PATCH", "/api/v1/user/change-password/" + owner.getId(), password, "owner.one"), 403);
        var profile = Map.of("name", "Outro", "email", "outro@email.com", "login", "outro", "userLevel", "OWNER", "address", restaurantBody("R", cuisine.getId()).get("address"));
        problem(request("PUT", "/api/v1/user/" + otherOwner.getId(), profile, "owner.one"), 403);
        problem(request("DELETE", "/api/v1/user/" + otherOwner.getId(), null, "owner.one"), 403);
        problem(request("PATCH", "/api/v1/user/grant-level", Map.of("userID", otherOwner.getId(), "userTypeID", 1), "owner.one"), 403);
        assertEquals("owner.two", users.findById(otherOwner.getId()).orElseThrow().getLogin());
    }

    @Test void shouldAllowChangingOnesOwnPasswordAndThenUseTheNewCredential() throws Exception {
        String newPassword = "NovaSenha@123";
        var changed = request("PATCH", "/api/v1/user/change-password/" + owner.getId(), Map.of("login", "owner.one", "password", newPassword), "owner.one");
        assertEquals(200, changed.statusCode(), changed.body());
        problem(request("POST", "/api/v1/restaurant", restaurantBody("R", cuisine.getId()), "owner.one"), 401);
        var response = request("POST", "/api/v1/restaurant", restaurantBody("R", cuisine.getId()), "owner.one", newPassword);
        assertEquals(201, response.statusCode(), response.body());
    }

    @Test void shouldRejectOverlongAddressWith400BeforeTheDatabaseConstraint() throws Exception {
        var payload = restaurantBody("Restaurante", cuisine.getId());
        var address = new LinkedHashMap<>(Map.of("address", " " + "A".repeat(255), "number", "10",
                "zipCode", "01000-000", "neighborhood", "Centro", "city", "São Paulo", "state", "SP", "country", "Brasil"));
        payload.put("address", address);
        var response = request("POST", "/api/v1/restaurant", payload, "owner.one");
        problem(response, 400);
        assertTrue(body(response).path("violations").has("address.address"));
        assertEquals(0, restaurants.count());
    }

    @Test void shouldPublishTheNewEndpointsAndBasicAuthenticationInOpenApi() throws Exception {
        var response = request("GET", "/v3/api-docs", null, null);
        assertEquals(200, response.statusCode(), response.body());
        var specification = body(response);
        assertEquals("basic", specification.path("components").path("securitySchemes").path("basicAuth").path("scheme").asText());
        assertTrue(specification.path("paths").path("/api/v1/restaurant").has("post"));
        assertTrue(specification.path("paths").path("/api/v1/menu-item/{id}").has("delete"));
    }
}
