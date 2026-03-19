package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Producto;
import com.example.demo.model.Venta;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.VentaRepository;

import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/venta")
@CrossOrigin("*")
public class VentaController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @GetMapping
    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    @PostMapping("/{id}/{cantidad}")
    public String ventaProducto(@PathVariable Long id, @PathVariable int cantidad) {
        // 1. Buscar el producto
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // 2. Verificar stock
        if (producto.getStock() < cantidad) {
            return "Stock insuficiente. Stock disponible: " + producto.getStock();
        }

        // 3. Descontar stock
        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);

        // 4. Registar Venta
        Venta venta = new Venta();
        venta.setProductoId(id);
        venta.setCantidad(cantidad);
        venta.setTotal(producto.getPrecio() * cantidad);
        venta.setFecha(LocalDateTime.now());
        ventaRepository.save(venta);

        return "Compra Realizada. Total: $" + venta.getTotal();

    }

}
