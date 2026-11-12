package com.example.labmcp.service;

import com.example.labmcp.model.*;
import com.example.labmcp.repository.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class SupermercadoToolsService {

    private final ProductoRepository productoRepo;
    private final ClienteRepository clienteRepo;
    private final PedidoRepository pedidoRepo;

    public SupermercadoToolsService(ProductoRepository productoRepo,
                                    ClienteRepository clienteRepo,
                                    PedidoRepository pedidoRepo) {
        this.productoRepo = productoRepo;
        this.clienteRepo = clienteRepo;
        this.pedidoRepo = pedidoRepo;
    }

    @Tool(description = "Devuelve el catálogo completo de productos del supermercado con su precio, stock y categoría.")
    public List<Producto> listarCatalogo() {
        return productoRepo.findAll();
    }

    @Tool(description = "Busca productos por coincidencia en su nombre para verificar precio y disponibilidad.")
    public List<Producto> buscarProducto(
            @ToolParam(description = "Nombre o fragmento del producto a buscar, por ejemplo: Leche, Arroz") String nombre) {
        return productoRepo.findByNombreContainingIgnoreCase(nombre);
    }

    @Tool(description = "Filtra los productos disponibles según el nombre de su categoría.")
    public List<Producto> listarPorCategoria(
            @ToolParam(description = "Nombre de la categoría, por ejemplo: Lácteos y Huevos, Frutas y Verduras, Despensa y Granos") String categoria) {
        return productoRepo.findByCategoriaNombreIgnoreCase(categoria);
    }

    @Tool(description = "Crea un nuevo pedido a domicilio para un cliente con un producto inicial, valida stock y lo descuenta del inventario.")
    @Transactional
    public Pedido realizarPedido(
            @ToolParam(description = "ID del cliente que realiza el pedido") Long clienteId,
            @ToolParam(description = "ID del producto a comprar") Long productoId,
            @ToolParam(description = "Cantidad de unidades a comprar") Integer cantidad) {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        Cliente cliente = clienteRepo.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el cliente con ID " + clienteId));

        Producto producto = productoRepo.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el producto con ID " + productoId));

        if (producto.getStock() < cantidad) {
            throw new IllegalStateException("Stock insuficiente para " + producto.getNombre() + ". Disponible: " + producto.getStock());
        }

        // Descontar inventario
        producto.setStock(producto.getStock() - cantidad);
        productoRepo.save(producto);

        // Crear encabezado del pedido
        Pedido pedido = new Pedido();
        pedido.setCliente(cliente);
        pedido.setFecha(LocalDateTime.now());
        pedido.setEstado("PENDIENTE");

        // Crear línea de detalle
        DetallePedido detalle = new DetallePedido();
        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(producto.getPrecio());

        BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
        pedido.setTotal(subtotal);
        pedido.getDetalles().add(detalle);

        return pedidoRepo.save(pedido);
    }

    @Tool(description = "Agrega un producto adicional a un pedido existente, recalculando el total y descontando el stock.")
    @Transactional
    public Pedido agregarProductoAPedido(
            @ToolParam(description = "ID del pedido existente") Long pedidoId,
            @ToolParam(description = "ID del producto a agregar") Long productoId,
            @ToolParam(description = "Cantidad de unidades a agregar") Integer cantidad) {

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        Pedido pedido = pedidoRepo.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el pedido con ID " + pedidoId));

        Producto producto = productoRepo.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el producto con ID " + productoId));

        if (producto.getStock() < cantidad) {
            throw new IllegalStateException("Stock insuficiente para " + producto.getNombre() + ". Disponible: " + producto.getStock());
        }

        producto.setStock(producto.getStock() - cantidad);
        productoRepo.save(producto);

        DetallePedido detalle = new DetallePedido();
        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecioUnitario(producto.getPrecio());

        BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
        pedido.setTotal(pedido.getTotal().add(subtotal));
        pedido.getDetalles().add(detalle);

        return pedidoRepo.save(pedido);
    }

    @Tool(description = "Consulta el estado actual, los productos incluidos y el total de un pedido por su ID.")
    public Pedido consultarPedido(
            @ToolParam(description = "ID del pedido a consultar") Long pedidoId) {
        return pedidoRepo.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el pedido con ID " + pedidoId));
    }
}