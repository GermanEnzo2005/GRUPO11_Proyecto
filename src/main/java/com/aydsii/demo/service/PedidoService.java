package com.aydsii.demo.service;

import com.aydsii.demo.dto.ComprarDTO;
import com.aydsii.demo.dto.DevolucionDTO;
import com.aydsii.demo.exception.BusinessException;
import com.aydsii.demo.model.Carrito;
import com.aydsii.demo.model.ItemCarrito;
import com.aydsii.demo.model.Producto;
import com.aydsii.demo.model.Cliente;
import com.aydsii.demo.model.Nivel;
import com.aydsii.demo.model.Pedido;
import com.aydsii.demo.enums.EstadoDevolucion;
import com.aydsii.demo.enums.EstadoPedido;
import com.aydsii.demo.model.Envio;
import com.aydsii.demo.repository.EnvioRepository;
import com.aydsii.demo.repository.PedidoRepository;
import com.aydsii.demo.service.transporte.TransporteApiClient;
import com.aydsii.demo.service.transporte.TransporteApiFactory;
import com.aydsii.demo.repository.CarritoRepository;
import com.aydsii.demo.repository.DevolucionRepository;
import com.aydsii.demo.model.Localidad;
import com.aydsii.demo.repository.LocalidadRepository;
import org.springframework.stereotype.Service;
import com.aydsii.demo.dto.PedidoDTO;
import com.aydsii.demo.dto.SolicitudDevolucionDTO;
import com.aydsii.demo.model.DetallePedido;
import com.aydsii.demo.model.Devolucion;

import java.time.LocalDate;
import java.util.ArrayList;

// Para Simple Logging Facade
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoService {
    // Fachada de registro SLF4J (Simple Logging Facade for Java)
    private static final Logger registraLog = LoggerFactory.getLogger(PedidoService.class);

    private final CarritoRepository carritoRepository;
    private final LocalidadRepository localidadRepository;
    private final PedidoRepository pedidoRepository;
    private final EnvioRepository envioRepository;
    private final TransporteApiFactory transporteApiFactory; // Inyección de la factoría
    private final DevolucionRepository devolucionRepository; // CU-04: Solicitar Devolucion de Producto

    public PedidoService(CarritoRepository carritoRepository, LocalidadRepository localidadRepository, PedidoRepository pedidoRepository, EnvioRepository envioRepository, TransporteApiFactory transporteApiFactory, DevolucionRepository devolucionRepository) {
        this.carritoRepository = carritoRepository;
        this.localidadRepository = localidadRepository;
        this.pedidoRepository = pedidoRepository;
        this.envioRepository = envioRepository;
        this.transporteApiFactory = transporteApiFactory;
        this.devolucionRepository = devolucionRepository;   // CU-04: Solicitar Devolucion de Producto
    }

    
    //caso de uso comprar carrito
    
    @Transactional
    public PedidoDTO procesarCompra(ComprarDTO comprarDTO) {
        //busca el carrito por su id
        Carrito carrito = carritoRepository.findById(comprarDTO.getIdCarrito())
                .orElseThrow(() -> new BusinessException("Carrito no encontrado", HttpStatus.NOT_FOUND));

        // verifica que el carrito no este vacio
        if (carrito.estaVacio()) {
            throw new BusinessException("El carrito está vacío");
        }
        //obtiene los items del caerrito
        List<ItemCarrito> items = carrito.getItems();

        //bucle que verifica que todos los productos tengan stock suficiente para la compra
        for(ItemCarrito item : items){
            Producto producto = item.getProducto();

            if (!producto.alcanzaStock(item.getCantidad())) {
                throw new BusinessException("Stock insuficiente para el producto: " + producto.getNombre());
            }
        }

        //ahora se crean los detalles pedido uno por cada item del carrito ademas aprovechamos esta busqueda para crear variables necesarias para el pedido
        float subTotal = 0;
        int cantidadProductos = 0;
        float impactoAmbientalTotal = 0;
        int puntosTotales = 0;
        List<DetallePedido> detalles = new ArrayList<>();

        for( ItemCarrito item : items ){
            float subTotalItem = item.calcularSubTotal();
            int cantidadItem = item.getCantidad();

            subTotal = subTotal + subTotalItem;
            cantidadProductos = cantidadProductos + cantidadItem;

            float impactoAmbientalItem = item.calcularImpactoAmbientalItem();
            impactoAmbientalTotal = impactoAmbientalTotal + impactoAmbientalItem;

            //ahora se crea el detalle del item
            DetallePedido detalle = new DetallePedido(cantidadItem, subTotalItem,impactoAmbientalItem,item.getProducto());
            //lo agrega al arreglo de detalles pedidos
            detalles.add(detalle);

            int puntosItem = item.getProducto().getPuntos() * cantidadItem;
            puntosTotales = puntosTotales + puntosItem;
        }

        //obtine al cliente que realiza el pedido
        Cliente cliente = carrito.getCliente();
        //ahora el nivel que posee el cliente para asi poder conseguir y aplicar los descunetos de pedido y envio
        Nivel nivel = cliente.getNivel();

        float descuentoPedidoCli = nivel.getDescuentoPedido();
        float descuentoEnvioCli = nivel.getDescuentoEnvio();
        //con todo ya conseguiro ahora se crea el pedido
        Pedido pedido = new Pedido(cliente, detalles, cantidadProductos, subTotal, impactoAmbientalTotal, puntosTotales, descuentoEnvioCli, descuentoPedidoCli);

        //ahora se busca la distancia de km de la localidad y se le envia al pedido para qwue calculo el importe

        // busca la localidad por el nombre que llegó en el DTO
        Localidad localidad = localidadRepository
            .findByNombreLocalidadIgnoreCase(comprarDTO.getLocalidad())
            .orElseThrow(() -> new BusinessException(
            "Localidad no encontrada: " + comprarDTO.getLocalidad(), HttpStatus.NOT_FOUND));

        // calcula el costo de envío con la distancia y el tipo de entrega
        pedido.calcularCostoEnvio(localidad.getDistanciaKm(), comprarDTO.getTipoEntrega(),comprarDTO.getServicioTransporte());
        

        //ahora se llma a calcularTotal para que el pedido calcule intenmente su total aplicasndole descuentos a el subtotal y el envio
        pedido.calcularTotal();

        //con todo ya terminado se cambia el estado al pedido a CONFIRMADO
        pedido.cambiarEstado(EstadoPedido.CONFIRMADO);

        //se le suman los puntos de la compra al cliente
        cliente.sumarPuntos(puntosTotales);

        //se descuneta el stock a los productos comprados
        for(DetallePedido detalle : detalles){
            Producto producto = detalle.getProducto();
            int cantidad = detalle.getCantidad();
            producto.descontarStock(cantidad);
        }

        //con todo hecho se guarda el pedido en la base de datos
        pedidoRepository.save(pedido);
        //como Pedido tiene detalles con cascade= ALL los pedidos se guardan tambine en la base de datos sin que se tengan que gguardar uno por uno

        //se crea el envio asociado
        Envio envio = new Envio(pedido, localidad, comprarDTO.getTipoEntrega(), comprarDTO.getDirDestino(), comprarDTO.getServicioTransporte());
        
        // 1. Se obtiene el cliente del servicio de transporte seleccionado (Andreani o Correo Argentino)
        TransporteApiClient apiClient = transporteApiFactory.getClient(comprarDTO.getServicioTransporte());

        // 2. Se pide al servicio de transporte que genere el código de seguimiento
        Long numeroSeguimientoGenerado = apiClient.generarNumeroSeguimiento();

        // 3. Setea los datos correspondientes en el objeto Envio
        envio.setNumeroSeguimiento(numeroSeguimientoGenerado);
        
        //y se guarda tambine
        envioRepository.save(envio);

        //por ultimo se borra el carrito
        carritoRepository.deleteById(comprarDTO.getIdCarrito());

        //con todo ya terminado se envia los datos del pedido al usuario
        return new PedidoDTO(pedido);
    }


    // CU-04: Solicitar Devolucion de Producto

    @Transactional
    public DevolucionDTO hacerDevolucion(SolicitudDevolucionDTO solicitud) {
        registraLog.info("Iniciando solicitud de devolucion (CU-04) para el Pedido ID: {}", solicitud.getIdPedido());  // Log

        // Recupera el pedido con el id del pedido que tiene la solicitud
        Pedido pedido = pedidoRepository.findById(solicitud.getIdPedido())
                .orElseThrow(() -> {
                    registraLog.error("No se encontro el pedido con ID: {}", solicitud.getIdPedido());  // Log
                    return new BusinessException("No se encontró el pedido con ID: " + solicitud.getIdPedido(), HttpStatus.NOT_FOUND);
                });

        // RN-01: Verificar que el pedido pertenezca al cliente: se confirma ya en la interfaz, ya que el cliente solo puede ver sus propios pedidos

        // Obtiene la fecha en la que se realizó el pedido
        LocalDate fechaPedido = pedido.getFechaPedido();
        registraLog.debug("Validando plazo de devolucion para fechaPedido: {}", fechaPedido);   // Log

        // Se valida que el plazo no sea mayor a 15 dias 
        if (!validarPlazo(fechaPedido)) {
            registraLog.warn("El pedido {} supera el plazo maximo de 15 dias.", pedido.getIdPedido());  // Log
            throw new BusinessException("El pedido supera el plazo máximo de 15 días para devoluciones y ya no puede devolverse.", HttpStatus.BAD_REQUEST);
        }

        // Obtiene el estado del pedido
        EstadoPedido estadoPedido = pedido.getEstadoPedido();

        // Verifica que el estado del pedido esté en ENTREGADO
        if (!verificarEstado(estadoPedido)) {
            registraLog.warn("Intento de devolucion sobre pedido {} en estado {}", pedido.getIdPedido(), estadoPedido);   // Log
            throw new BusinessException("Solo se pueden devolver productos de pedidos que se encuentren en estado ENTREGADO.", HttpStatus.BAD_REQUEST);
        }

        // Obtiene el detalle pedido que contiene el producto indicado dentro del pedido indicado
        DetallePedido detallePedido = pedido.getDetallePedidoPorProducto(solicitud.getIdProducto());

        // Verifica que se haya encontrado el detalle pedido que contiene el producto indicado dentro del pedido indicado
        if (detallePedido == null) {
            registraLog.warn("El producto con ID {} no pertenece al pedido {}", solicitud.getIdProducto(), pedido.getIdPedido());    // Log
            throw new BusinessException("El producto indicado no forma parte del pedido.", HttpStatus.BAD_REQUEST);
        }

        // Obtiene la cantidad que se quiere devolver
        Integer cantidad = solicitud.getCantidad();

        // Valida que la cantidad solicitada para devolucion no supere la cantidad comprada
        if (!detallePedido.validarQueCantidadNoSePase(cantidad)) {
            registraLog.warn("Cantidad solicitada ({}) supera la comprada ({}) para el producto {}", 
                cantidad, detallePedido.getCantidad(), detallePedido.getProducto().getNombre());    // Log
            throw new BusinessException("La cantidad a devolver supera la cantidad adquirida para el producto: " + detallePedido.getProducto().getNombre(), HttpStatus.BAD_REQUEST);
        }

        // Instancia Devolucion en estado PENDIENTE
        Devolucion devolucion = new Devolucion(
                solicitud.getMotivo(),
                solicitud.getDescripcion(),
                detallePedido,
                EstadoDevolucion.PENDIENTE
        );

        // Se guarda la devolucion en el repositorio
        Devolucion devolucionGuardada = devolucionRepository.save(devolucion);
        registraLog.info("Devolucion registrada exitosamente en BD con ID: {}", devolucionGuardada.getIdDevolucion());  // Log

        // Con todo realizado se mapea y retorna el DTO con los datos de la devolucion al cliente
        return new DevolucionDTO(devolucionGuardada);
    }

    // Valida que no hayan transcurrido más de 15 días desde la fecha del pedido: True si está dentro del rango
    private boolean validarPlazo(LocalDate fechaPedido) {
        if (fechaPedido == null) return false;
        long diasTranscurridos = java.time.temporal.ChronoUnit.DAYS.between(fechaPedido, LocalDate.now());
        return diasTranscurridos >= 0 && diasTranscurridos <= 15;
    }

    // Verifica que el estado del pedido sea ENTREGADO
    private boolean verificarEstado(EstadoPedido estado) {
        return estado == EstadoPedido.ENTREGADO;
    }
}