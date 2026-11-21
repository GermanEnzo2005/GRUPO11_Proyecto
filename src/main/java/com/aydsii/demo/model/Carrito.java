package com.aydsii.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCarrito;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente")
    private Cliente cliente;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemCarrito> items = new ArrayList<>();

    public boolean estaVacio() {
        return items.isEmpty();
    }
    //los metodos nuevos para el CU eliminar item del carrito
    //recorre todos los items del carrito, busca cual es el item 
    //cuyo id se envio como parametro y lo retorna
    public ItemCarrito buscarItem(Long idItem) {
        for (ItemCarrito item : items) {
            if (item.getIdItemCarrito().equals(idItem)) {
                return item;
            }
        }
        return null;
    }
    //elimina el item del listado de items asi luego cunado se hace save carrito
    //este item se elimina automaticamente por cascada.ALL
    public void eliminarItem(ItemCarrito item) {
        items.remove(item);
    }
}
