package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.Producto;
import com.example.demo.repository.ProductoRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {
    
    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private KafkaTemplate<String, Producto> kafkaTemplate;

    // 1. Listar productos
    @GetMapping
    @CircuitBreaker(name = "dbProductos", fallbackMethod = "fallbackListar")
    @Retry(name = "dbProductos")
    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    // 2. Guardar productos
    @PostMapping
    @CircuitBreaker(name = "dbProductos", fallbackMethod = "fallbackGrabar")
    public Producto grabar(@RequestBody Producto pro) {
        pro.setId(null);
        Producto p = productoRepository.save(pro);
        return p;
    }
    
    // 3. Eliminar productos
    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        productoRepository.deleteById(id);
    }

    // 4. Modificar productos
    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Long id, @RequestBody Producto pro) {
        return productoRepository.save(pro);
    }    

    // -- Metodos FALLBACK (Resiliencia) --

    // Si la BD falla al listar, devolvemos una lista vacia o un mensaje
    public List<Producto>fallbackListar(Throwable e) {
        System.err.println("Error al listar:" + e.getMessage());
        // Al lanzar esta excepcion, Spring envia un HTTP 503 al frontend
        throw new ResponseStatusException(
            HttpStatus.SERVICE_UNAVAILABLE, "La base de datos no está disponible"
        );
        // return Collections.emptyList(); // Evita que el frontend explote enviando valores nulos
    }

    // Si la DB falla al grabar, podrias informar al usuario o mandarlo a kafka
    public Producto fallbackGrabar(Producto pro, Throwable e) {
        System.err.println("Error al grabar, circuito abierto: " + e.getMessage());
        // Creamos un objeto ficticio para avisar al frontend
        kafkaTemplate.send("productos-pendientes", pro);
        Producto errorPro = new Producto();
        errorPro.setNombre("Sistema temporalmente fuera de línea. Intente más tarde.");
        return errorPro;
    }

    @KafkaListener(topics = "productos-pendientes", groupId = "ecommerce-group")
    public void escuchar(Producto pro) {
        // Aquí procesas el producto cuando la DB esté lista
        productoRepository.save(pro);
    }
    
}
