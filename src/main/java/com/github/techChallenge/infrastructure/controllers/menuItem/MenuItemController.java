package com.github.techChallenge.infrastructure.controllers.menuItem;

import com.github.techChallenge.application.usecases.menuItem.*;
import com.github.techChallenge.domain.menuItem.dto.*;
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
@RequestMapping("/api/v1/menu-item")
@Tag(name = "Itens do cardápio", description = "Consulta pública e gestão pelo proprietário do restaurante")
@ApiResponse(responseCode = "400", description = "Dados ou paginação inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
@ApiResponse(responseCode = "404", description = "Restaurante ou item inexistente", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
public class MenuItemController {
    private final CreateMenuItemUseCase create;
    private final UpdateMenuItemUseCase update;
    private final FindMenuItemUseCase find;
    private final ListMenuItemUseCase list;
    private final DeleteMenuItemUseCase delete;

    public MenuItemController(CreateMenuItemUseCase create, UpdateMenuItemUseCase update,
                              FindMenuItemUseCase find, ListMenuItemUseCase list, DeleteMenuItemUseCase delete) {
        this.create = create; this.update = update; this.find = find; this.list = list; this.delete = delete;
    }

    @PostMapping({"", "/"})
    @Operation(summary = "Cadastrar item no cardápio", description = "O usuário autenticado deve ser OWNER e dono do restaurante informado.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponse(responseCode = "201", description = "Item cadastrado")
    @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida")
    @ApiResponse(responseCode = "403", description = "Usuário não é dono do restaurante")
    @ApiResponse(responseCode = "409", description = "Nome já cadastrado no mesmo restaurante")
    public ResponseEntity<MenuItemOutputDTO> create(@RequestBody MenuItemCreateInputDTO input,
                                                    @AuthenticationPrincipal RestaurantUserPrincipal user) {
        var output = create.execute(input, user.getId());
        var location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/v1/menu-item/{id}").buildAndExpand(output.id()).toUri();
        return ResponseEntity.created(location).body(output);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar item", description = "Somente o dono do restaurante pode atualizar. O restaurante do item é preservado.")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponse(responseCode = "200", description = "Item atualizado")
    @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida")
    @ApiResponse(responseCode = "403", description = "Usuário não é dono do restaurante")
    @ApiResponse(responseCode = "409", description = "Nome já cadastrado no mesmo restaurante")
    public MenuItemOutputDTO update(@PathVariable Long id, @RequestBody MenuItemUpdateInputDTO input,
                                    @AuthenticationPrincipal RestaurantUserPrincipal user) { return update.execute(input, id, user.getId()); }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar item do cardápio")
    @ApiResponse(responseCode = "200", description = "Item localizado")
    public MenuItemOutputDTO find(@PathVariable Long id) { return find.execute(id); }

    @GetMapping({"", "/"})
    @Operation(summary = "Listar cardápio do restaurante", description = "Informe restaurantId. Paginação: page >= 0 e offset de 1 a 100.")
    @ApiResponse(responseCode = "200", description = "Página de itens")
    public Page<MenuItemOutputDTO> list(@RequestParam Long restaurantId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int offset) {
        return list.execute(restaurantId, page, offset);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir item do cardápio")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponse(responseCode = "204", description = "Item excluído", content = @Content)
    @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida")
    @ApiResponse(responseCode = "403", description = "Usuário não é dono do restaurante")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal RestaurantUserPrincipal user) {
        delete.execute(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
