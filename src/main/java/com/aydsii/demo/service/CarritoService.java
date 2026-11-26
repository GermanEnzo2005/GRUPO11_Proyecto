package com.aydsii.demo.service;

import com.aydsii.demo.config.ConfiguracionNegocio;
import com.aydsii.demo.dto.CarritoDTO;
import com.aydsii.demo.dto.ItemCarritoDTO;
import com.aydsii.demo.exception.BusinessException;
import com.aydsii.demo.model.Carrito;
import com.aydsii.demo.model.ItemCarrito;
import com.aydsii.demo.model.Cliente;
import com.aydsii.demo.model.Nivel;
import com.aydsii.demo.repository.CarritoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CarritoService {
    private final CarritoRepository carritoRepository;

    public CarritoService(CarritoRepository carritoRepository) {
        this.carritoRepository = carritoRepository;
    }

    @Transactional
    public CarritoDTO eliminarItem(Long idCarrito, Long idItem) {
        // busca el carrito, en caso de que no exista muestra el mensaje de que el carrito no existe
        Carrito carrito = carritoRepository.findById(idCarrito)
                .orElseThrow(() -> new BusinessException("Carrito no encontrado", HttpStatus.NOT_FOUND));

        // verifica que el ítem pertenezca a ese carrito, en caso de que no este se le informa al usuario que el producto no existe 
        ItemCarrito itemAEliminar = carrito.buscarItem(idItem);
        if (itemAEliminar == null) {
            throw new BusinessException("El producto no está en el carrito", HttpStatus.NOT_FOUND);
        }

        // lo saca de la lista de items del carrito y guarda el carrito ya actulizado
        carrito.eliminarItem(itemAEliminar);
        carritoRepository.save(carrito);

        // recalcula el carrito con los ítems que quedaron
        float subtotalCarrito = 0;
        int cantidadTotal = 0;
        List<ItemCarritoDTO> lineas = new ArrayList<>();

        for (ItemCarrito item : carrito.getItems()) {
            float subTotalItem = item.calcularSubTotal();
            int cantidadItem = item.getCantidad();

            subtotalCarrito = subtotalCarrito + subTotalItem;
            cantidadTotal = cantidadTotal + cantidadItem;

            lineas.add(new ItemCarritoDTO(item.getIdItemCarrito(),item.getProducto().getNombre(), cantidadItem, subTotalItem));
        }
        
        //descuento por nivel del cliente
        Cliente cliente = carrito.getCliente();
        Nivel nivel = cliente.getNivel();
        float descuentoPedido = nivel.getDescuentoPedido();

        // primero el mayorista y después el de nivel 
        // singleton, ahora se verifica la cantidad mayorista en esa clase y se obtiene el descuwento 
        ConfiguracionNegocio config = ConfiguracionNegocio.getUnica();

        boolean descuentoMayorista = cantidadTotal >= config.getCantidadMayorista();

        float subtotalConDescuento = subtotalCarrito;
        if (descuentoMayorista) {
            subtotalConDescuento = subtotalConDescuento - (subtotalConDescuento * config.getDescuentoMayorista());
        }
        subtotalConDescuento = subtotalConDescuento - (subtotalConDescuento * descuentoPedido);
        //el carrito ya actualizado
        return new CarritoDTO(idCarrito, lineas, cantidadTotal, subtotalCarrito, descuentoMayorista, descuentoPedido, subtotalConDescuento);
    }
}