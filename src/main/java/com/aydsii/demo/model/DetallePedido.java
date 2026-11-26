package com.aydsii.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDetallePedido;

    private Integer cantidad;
    private Float subTotalDetalle;
    private Float impactoDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto")
    private Producto producto;

    public DetallePedido(Integer cantidad, Float subTotalDetalle, Float impactoDetalle, Producto producto) {
        this.cantidad = cantidad;
        this.subTotalDetalle = subTotalDetalle;
        this.impactoDetalle = impactoDetalle;
        this.producto = producto;
    }

    // Para el CU-04: Solicitar Devolucion de Producto
    // Retorna true si la cantidad solicitada no es vacio y es mayor a 0 y menor o igual a la cantidad de detalle pedido  
    public boolean validarQueCantidadNoSePase(Integer cantidadSolicitada) {
        return cantidadSolicitada != null && cantidadSolicitada > 0 && cantidadSolicitada <= this.cantidad;
    }
}