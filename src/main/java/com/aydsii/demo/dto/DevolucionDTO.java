package com.aydsii.demo.dto;

import java.time.LocalDate;

import com.aydsii.demo.enums.EstadoDevolucion;
import com.aydsii.demo.model.Devolucion;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con los datos de la devolución registrada")
public class DevolucionDTO {

    @Schema(description = "ID generado de la devolución", example = "1")
    private Long idDevolucion;

    @Schema(description = "ID del pedido asociado", example = "1")
    private Long idPedido;

    @Schema(description = "Motivo registrado", example = "Producto defectuoso")
    private String motivo;

    @Schema(description = "Descripción registrada", example = "El envase llegó roto")
    private String descripcion;

    @Schema(description = "Fecha en que se registró la solicitud", example = "2026-11-24")
    private LocalDate fechaSolicitud;

    @Schema(description = "Estado inicial de la solicitud", example = "PENDIENTE")
    private EstadoDevolucion estadoDevolucion;

    public DevolucionDTO() {}

    public DevolucionDTO(Devolucion devolucion) {
        this.idDevolucion = devolucion.getIdDevolucion();
        this.motivo = devolucion.getMotivo();
        this.descripcion = devolucion.getDescripcion();
        this.fechaSolicitud = devolucion.getFechaSolicitud();
        this.estadoDevolucion = devolucion.getEstadoDevolucion();

        if (devolucion.getDetallePedido() != null && devolucion.getDetallePedido().getPedido() != null) {
            this.idPedido = devolucion.getDetallePedido().getPedido().getIdPedido();
        }
    }

    public Long getIdDevolucion() { return idDevolucion; }
    public void setIdDevolucion(Long idDevolucion) { this.idDevolucion = idDevolucion; }

    public Long getIdPedido() { return idPedido; }
    public void setIdPedido(Long idPedido) { this.idPedido = idPedido; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public EstadoDevolucion getEstadoDevolucion() { return estadoDevolucion; }
    public void setEstadoDevolucion(EstadoDevolucion estadoDevolucion) { this.estadoDevolucion = estadoDevolucion; }
}
