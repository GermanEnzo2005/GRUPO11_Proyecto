package com.aydsii.demo.controller;

import com.aydsii.demo.dto.ApiResponse;
import com.aydsii.demo.dto.ComprarDTO;
import com.aydsii.demo.dto.DevolucionDTO;
import com.aydsii.demo.dto.PedidoDTO;
import com.aydsii.demo.dto.SolicitudDevolucionDTO;

import org.springframework.http.HttpStatus;
import com.aydsii.demo.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;


@RestController
@RequestMapping("/api/pedido")
@Tag(name = "Pedido", description = "Compra de los productos del carrito")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    // CU-01: Comprar carrito
    @PostMapping("/comprar")
    @Operation(summary = "Comprar carrito",
            description = "Valida el carrito y registra el pedido")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Compra relizada correctamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class)
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Stock insuficiente",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Carrito no encontrado",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        ),
    })
    public ResponseEntity<ApiResponse<PedidoDTO>> crearPedido(@Valid @RequestBody ComprarDTO comprarDTO) {
        PedidoDTO pedidoCreado = pedidoService.procesarCompra(comprarDTO);

        //la respuesta con el pedido ya creado 
        ApiResponse<PedidoDTO> response = ApiResponse.success(HttpStatus.CREATED.value(), "Su compra se realizo con exito" , pedidoCreado);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    // CU-04: Solicitar Devolucion de Producto
    @PostMapping("/devolucion")
    @Operation(
        summary = "Solicitar devolución de producto",
        description = "Registra una solicitud de devolución en estado PENDIENTE asociada a un pedido entregado y notifica al área de ventas."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Devolución creada con éxito",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                        value = """
                        {
                            "status": 201,
                            "message": "Devolución creada con éxito. Su solicitud pasará a ser revisada por personal de ventas.",
                            "data": {
                                "descripcion": "Un vaso vino rajado",
                                "estadoDevolucion": "PENDIENTE",
                                "idDevolucion": 1,
                                "idPedido": 1,
                                "motivo": "Producto defectuoso"
                            }
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos inválidos, pedido no entregado, plazo vencido o cantidad a devolver excedida",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                        value = """
                        {
                            "status": 400,
                            "message": "El pedido supera el plazo máximo de 15 días para devoluciones y ya no puede devolverse.",
                            "data": null
                        }
                        """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Pedido no encontrado",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                        value = """
                        {
                            "status": 404,
                            "message": "No se encontró el pedido con ID: 99",
                            "data": null
                        }
                        """
                )
            )
        )
    })
    public ResponseEntity<ApiResponse<DevolucionDTO>> hacerDevolucion(@Valid @RequestBody SolicitudDevolucionDTO solicitud) {
        DevolucionDTO devolucionCreada = pedidoService.hacerDevolucion(solicitud);

        // La respuesta con la devolucion ya creada
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        201,
                        "Devolución creada con éxito. Su solicitud pasará a ser revisada por personal de ventas.",
                        devolucionCreada
                ));
    }
}
