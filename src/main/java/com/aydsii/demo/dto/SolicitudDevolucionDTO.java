package com.aydsii.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos requeridos para registrar una solicitud de devolución")
public class SolicitudDevolucionDTO {

    @NotNull(message = "El ID del pedido es obligatorio")
    @Schema(description = "ID del pedido asociado", example = "1")
    private Long idPedido;

    @NotNull(message = "El ID del producto es obligatorio")
    @Schema(description = "ID del producto a devolver", example = "1")
    private Long idProducto;

    @NotNull(message = "La cantidad a devolver es obligatoria")
    @Min(value = 1, message = "La cantidad mínima a devolver es 1")
    @Schema(description = "Cantidad de unidades a devolver", example = "1")
    private Integer cantidad;

    @NotBlank(message = "Es obligatorio agregar un motivo para la devolución")
    @Size(max = 45, message = "El motivo no puede superar los 45 caracteres")
    @Schema(description = "Motivo de la devolución", example = "Producto defectuoso")
    private String motivo;

    @Size(max = 45, message = "La descripción no puede superar los 45 caracteres")
    @Schema(description = "Descripción adicional o enlace a fotos", example = "El envase llegó roto en la esquina")
    private String descripcion;

    public SolicitudDevolucionDTO() {}

    public Long getIdPedido() { return idPedido; }
    public void setIdPedido(Long idPedido) { this.idPedido = idPedido; }

    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}