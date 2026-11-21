package com.aydsii.demo.dto;

import lombok.Data;
//el dto que muestra internamente el carrito DTO, muestra los datos asociados a un item del carrito
@Data
public class ItemCarritoDTO {

    private Long idItemCarrito;
    private String producto;
    private Integer cantidad;
    private Float subtotal;

    public ItemCarritoDTO(Long idItemCarrito, String producto, Integer cantidad, Float subtotal) {
        this.idItemCarrito = idItemCarrito;
        this.producto = producto;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
    }
}