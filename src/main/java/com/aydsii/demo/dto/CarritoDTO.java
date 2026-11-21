package com.aydsii.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class CarritoDTO {

    private Long idCarrito;
    private List<ItemCarritoDTO> items;
    private Integer cantidadTotal;
    private Float subtotal;
    private Boolean descuentoMayorista;
    private Float descuentoNivel;
    private Float subtotalConDescuento;

    public CarritoDTO(Long idCarrito, List<ItemCarritoDTO> items, Integer cantidadTotal, Float subtotal, Boolean descuentoMayorista, Float descuentoNivel, Float subtotalConDescuento) {
        this.idCarrito = idCarrito;
        this.items = items;
        this.cantidadTotal = cantidadTotal;
        this.subtotal = subtotal;
        this.descuentoMayorista = descuentoMayorista;
        this.descuentoNivel = descuentoNivel;
        this.subtotalConDescuento = subtotalConDescuento;
    }
}
