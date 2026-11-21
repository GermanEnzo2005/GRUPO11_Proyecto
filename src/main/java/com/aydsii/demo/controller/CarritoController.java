package com.aydsii.demo.controller;

import com.aydsii.demo.dto.ApiResponse;
import com.aydsii.demo.dto.CarritoDTO;
import com.aydsii.demo.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carrito")
@Tag(name = "Carrito", description = "Operaciones sobre el carrito de compras")
public class CarritoController {
    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @DeleteMapping("/{idCarrito}/items/{idItem}")
    @Operation(summary = "Eliminar producto del carrito",
            description = "Elimina un ítem del carrito y devuelve el carrito actualizado")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Producto eliminado correctamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Carrito o producto no encontrado",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    public ResponseEntity<ApiResponse<CarritoDTO>> eliminarItem(@PathVariable Long idCarrito,
                                                                @PathVariable Long idItem) {
        CarritoDTO carritoActualizado = carritoService.eliminarItem(idCarrito, idItem);

        String mensaje = carritoActualizado.getItems().isEmpty()
                ? "Su carrito está vacío"
                : "Producto eliminado del carrito";

        ApiResponse<CarritoDTO> response = ApiResponse.success(
                HttpStatus.OK.value(), mensaje, carritoActualizado);
        return ResponseEntity.ok(response);
    }
}
