package com.github.techChallenge.infrastructure.controllers.restaurant;

import com.github.techChallenge.application.usecases.restaurant.*;
import com.github.techChallenge.domain.restaurant.dto.*;
import com.github.techChallenge.infrastructure.security.RestaurantUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/restaurant")
@Tag(name = "Restaurantes", description = "Consulta pública e gestão pelo proprietário autenticado")
@ApiResponse(responseCode = "400", description = "Dados ou paginação inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
@ApiResponse(responseCode = "404", description = "Restaurante ou categoria inexistente", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
public class RestaurantController {
    private final CreateRestaurantUseCase create;
    private final UpdateRestaurantUseCase update;
    private final FindRestaurantUseCase find;
    private final ListRestaurantUseCase list;
    private final DeleteRestaurantUseCase delete;

    public RestaurantController(CreateRestaurantUseCase create, UpdateRestaurantUseCase update,
                                FindRestaurantUseCase find, ListRestaurantUseCase list, DeleteRestaurantUseCase delete) {
        this.create = create; this.update = update; this.find = find; this.list = list; this.delete = delete;
    }

    @PostMapping({"", "/"})
    @Operation(summary = "Cadastrar restaurante", description = "O dono é o usuário autenticado. O nome é único, sem distinguir maiúsculas e minúsculas.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponse(responseCode = "201", description = "Restaurante cadastrado")
    @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida")
    @ApiResponse(responseCode = "403", description = "Usuário não possui nível OWNER")
    @ApiResponse(responseCode = "409", description = "Nome já cadastrado")
    public ResponseEntity<RestaurantOutputDTO> create(@RequestBody RestaurantCreateInputDTO input,
                                                      @AuthenticationPrincipal RestaurantUserPrincipal user) {
        var output = create.execute(input, user.getId());
        var location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/restaurant/{id}").buildAndExpand(output.id()).toUri();
        return ResponseEntity.created(location).body(output);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar restaurante", description = "Somente o dono pode atualizar. O proprietário é preservado.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponse(responseCode = "200", description = "Restaurante atualizado")
    @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida")
    @ApiResponse(responseCode = "403", description = "Usuário não é dono deste restaurante")
    @ApiResponse(responseCode = "409", description = "Nome já cadastrado")
    public RestaurantOutputDTO update(@PathVariable Long id, @RequestBody RestaurantUpdateInputDTO input,
                                      @AuthenticationPrincipal RestaurantUserPrincipal user) { return update.execute(input, id, user.getId()); }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar restaurante")
    @ApiResponse(responseCode = "200", description = "Restaurante localizado")
    public RestaurantOutputDTO find(@PathVariable Long id) { return find.execute(id); }

    @GetMapping({"", "/"})
    @Operation(summary = "Listar restaurantes", description = "Use categoryId para filtrar pelo tipo de cozinha. Paginação: page >= 0 e offset de 1 a 100.")
    @ApiResponse(responseCode = "200", description = "Página de restaurantes")
    public Page<RestaurantOutputDTO> list(@RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int offset) {
        return list.execute(categoryId, page, offset);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir restaurante", description = "Somente o dono pode excluir. Remova primeiro os itens do cardápio.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponse(responseCode = "204", description = "Restaurante excluído", content = @Content)
    @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida")
    @ApiResponse(responseCode = "403", description = "Usuário não é dono deste restaurante")
    @ApiResponse(responseCode = "409", description = "Restaurante ainda possui itens no cardápio")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal RestaurantUserPrincipal user) {
        delete.execute(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
