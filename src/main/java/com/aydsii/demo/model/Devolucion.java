package com.aydsii.demo.model;

import com.aydsii.demo.enums.EstadoDevolucion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_devolucion")
    private Long idDevolucion;

    private String motivo;

    @Enumerated(EnumType.STRING)
    private EstadoDevolucion estadoDevolucion;

    private String descripcion;

    private LocalDate fechaSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_detalle_Pedido", nullable = false)
    private DetallePedido detallePedido;

    public Devolucion(String motivo, String descripcion, DetallePedido detallePedido, EstadoDevolucion estadoDevolucion) {
        this.motivo = motivo;
        this.descripcion = descripcion;
        this.detallePedido = detallePedido;
        this.estadoDevolucion = estadoDevolucion;
        this.fechaSolicitud = LocalDate.now();
    }
}
