[Caso semestral EFT DSY1106.docx](https://github.com/user-attachments/files/26121381/Caso.semestral.EFT.DSY1106.docx)

# Estructura del Proyecto

```markdown
ms-pets/
├── src/main/java/com/sanossalvos/mspets/
│   ├── MsPetsApplication.java
│   ├── controller/
│   │   └── PetController.java
│   ├── service/
│   │   ├── PetService.java
│   │   └── PetServiceImpl.java
│   ├── model/
│   │   ├── dto/
│   │   │   ├── PetRequestDTO.java
│   │   │   └── PetResponseDTO.java
│   │   ├── entity/
│   │   │   ├── Pet.java
│   │   │   └── PetType.java (enum: LOST, FOUND)
│   │   └── mapper/
│   │       └── PetMapper.java
│   ├── repository/
│   │   └── PetRepository.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   └── SwaggerConfig.java
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       └── ResourceNotFoundException.java
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/ (si usas Flyway)
└── pom.xml
```

# 🐾 Guía de Transformación: SmartLogix → Sanos y Salvos

> Proyecto base: `AplicacionSemana1_MunicipalidadValleSol` (sistema de inventario con Kafka)  
> Proyecto destino: **Sanos y Salvos** — Plataforma de mascotas perdidas/encontradas  
> Stack: Spring Boot + React + PostgreSQL + Kafka

---

## Índice

1. [Backend — Modelos](#1-backend--modelos)
2. [Backend — Repositorios](#2-backend--repositorios)
3. [Backend — Controladores REST](#3-backend--controladores-rest)
4. [Backend — ControladorKafka](#4-backend--controlador-kafka)
5. [Backend — application.properties](#5-backend--applicationproperties)
6. [Backend — docker-compose.yml](#6-backend--docker-composeyml)
7. [Backend — pom.xml](#7-backend--pomxml)
8. [Frontend — api.js](#8-frontend--apijs)
9. [Frontend — Sidebar](#9-frontend--sidebar)
10. [Frontend — Dashboard](#10-frontend--dashboard)
11. [Frontend — Página Mascotas](#11-frontend--página-mascotas)
12. [Frontend — Página Reportes](#12-frontend--página-reportes)
13. [Frontend — App.jsx](#13-frontend--appjsx)

---

## 1. Backend — Modelos

### 1.1 Reemplazar `Producto.java` → `Mascota.java`

**Ruta:** `src/main/java/com/sanoysalvos/model/Mascota.java`

```java
package com.sanoysalvos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mascotas")
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String especie;         // PERRO, GATO, AVE, OTRO

    private String raza;

    private String color;

    private String tamano;          // PEQUENO, MEDIANO, GRANDE

    @Column(length = 500)
    private String descripcion;

    private String fotoUrl;

    private Double latitud;

    private Double longitud;

    private String contacto;        // Teléfono o email del dueño/reportante

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMascota estado;   // PERDIDA, ENCONTRADA, REUNIDA

    @Column(nullable = false)
    private LocalDateTime fechaReporte;

    @PrePersist
    public void prePersist() {
        if (this.fechaReporte == null) {
            this.fechaReporte = LocalDateTime.now();
        }
    }

    // ---- Getters y Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }

    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getTamano() { return tamano; }
    public void setTamano(String tamano) { this.tamano = tamano; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public EstadoMascota getEstado() { return estado; }
    public void setEstado(EstadoMascota estado) { this.estado = estado; }

    public LocalDateTime getFechaReporte() { return fechaReporte; }
    public void setFechaReporte(LocalDateTime fechaReporte) { this.fechaReporte = fechaReporte; }
}
```

**Ruta:** `src/main/java/com/sanoysalvos/model/EstadoMascota.java`

```java
package com.sanoysalvos.model;

public enum EstadoMascota {
    PERDIDA,
    ENCONTRADA,
    REUNIDA
}
```

---

### 1.2 Reemplazar `Venta.java` → `ReporteMascota.java`

**Ruta:** `src/main/java/com/sanoysalvos/model/ReporteMascota.java`

```java
package com.sanoysalvos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes_mascota")
public class ReporteMascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long mascotaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoReporte tipo;       // PERDIDA, ENCONTRADA

    @Column(length = 500)
    private String descripcion;

    private String ubicacion;       // Dirección textual

    private Double latitud;

    private Double longitud;

    @Column(nullable = false)
    private LocalDateTime fecha;

    private String contacto;        // Quién hace el reporte

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
    }

    // ---- Getters y Setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMascotaId() { return mascotaId; }
    public void setMascotaId(Long mascotaId) { this.mascotaId = mascotaId; }

    public TipoReporte getTipo() { return tipo; }
    public void setTipo(TipoReporte tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }
}
```

**Ruta:** `src/main/java/com/sanoysalvos/model/TipoReporte.java`

```java
package com.sanoysalvos.model;

public enum TipoReporte {
    PERDIDA,
    ENCONTRADA
}
```

---

## 2. Backend — Repositorios

### 2.1 Reemplazar `ProductoRepository.java` → `MascotaRepository.java`

**Ruta:** `src/main/java/com/sanoysalvos/repository/MascotaRepository.java`

```java
package com.sanoysalvos.repository;

import com.sanoysalvos.model.EstadoMascota;
import com.sanoysalvos.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    // Buscar por estado: PERDIDA, ENCONTRADA, REUNIDA
    List<Mascota> findByEstado(EstadoMascota estado);

    // Buscar por especie (PERRO, GATO, etc.)
    List<Mascota> findByEspecie(String especie);

    // Buscar por color y especie (útil para el motor de coincidencias)
    List<Mascota> findByColorAndEspecie(String color, String especie);
}
```

---

### 2.2 Reemplazar `VentaRepository.java` → `ReporteMascotaRepository.java`

**Ruta:** `src/main/java/com/sanoysalvos/repository/ReporteMascotaRepository.java`

```java
package com.sanoysalvos.repository;

import com.sanoysalvos.model.ReporteMascota;
import com.sanoysalvos.model.TipoReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteMascotaRepository extends JpaRepository<ReporteMascota, Long> {

    // Todos los reportes asociados a una mascota
    List<ReporteMascota> findByMascotaId(Long mascotaId);

    // Filtrar por tipo: PERDIDA o ENCONTRADA
    List<ReporteMascota> findByTipo(TipoReporte tipo);

    // Reportes de una mascota filtrados por tipo
    List<ReporteMascota> findByMascotaIdAndTipo(Long mascotaId, TipoReporte tipo);
}
```

---

## 3. Backend — Controladores REST

### 3.1 Reemplazar `ProductoController.java` → `MascotaController.java`

**Ruta:** `src/main/java/com/sanoysalvos/controller/MascotaController.java`

```java
package com.sanoysalvos.controller;

import com.sanoysalvos.model.EstadoMascota;
import com.sanoysalvos.model.Mascota;
import com.sanoysalvos.repository.MascotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
@CrossOrigin(origins = "*")
public class MascotaController {

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // GET /api/mascotas — Listar todas
    @GetMapping
    public List<Mascota> listarTodas() {
        return mascotaRepository.findAll();
    }

    // GET /api/mascotas/{id} — Obtener una
    @GetMapping("/{id}")
    public ResponseEntity<Mascota> obtenerPorId(@PathVariable Long id) {
        return mascotaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/mascotas/estado/{estado} — Filtrar por estado
    @GetMapping("/estado/{estado}")
    public List<Mascota> listarPorEstado(@PathVariable EstadoMascota estado) {
        return mascotaRepository.findByEstado(estado);
    }

    // POST /api/mascotas — Registrar nueva mascota y publicar en Kafka
    @PostMapping
    public Mascota registrar(@RequestBody Mascota mascota) {
        Mascota guardada = mascotaRepository.save(mascota);

        // Publicar evento en Kafka para el motor de coincidencias
        String mensaje = String.format(
            "{\"id\":%d,\"nombre\":\"%s\",\"estado\":\"%s\",\"especie\":\"%s\",\"color\":\"%s\"}",
            guardada.getId(), guardada.getNombre(),
            guardada.getEstado(), guardada.getEspecie(), guardada.getColor()
        );
        kafkaTemplate.send("mascotas-reportadas", mensaje);

        return guardada;
    }

    // PUT /api/mascotas/{id} — Actualizar
    @PutMapping("/{id}")
    public ResponseEntity<Mascota> actualizar(@PathVariable Long id, @RequestBody Mascota datos) {
        return mascotaRepository.findById(id).map(mascota -> {
            mascota.setNombre(datos.getNombre());
            mascota.setEspecie(datos.getEspecie());
            mascota.setRaza(datos.getRaza());
            mascota.setColor(datos.getColor());
            mascota.setTamano(datos.getTamano());
            mascota.setDescripcion(datos.getDescripcion());
            mascota.setFotoUrl(datos.getFotoUrl());
            mascota.setLatitud(datos.getLatitud());
            mascota.setLongitud(datos.getLongitud());
            mascota.setContacto(datos.getContacto());
            mascota.setEstado(datos.getEstado());
            return ResponseEntity.ok(mascotaRepository.save(mascota));
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/mascotas/{id} — Eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!mascotaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        mascotaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

### 3.2 Reemplazar `VentaController.java` → `ReporteMascotaController.java`

**Ruta:** `src/main/java/com/sanoysalvos/controller/ReporteMascotaController.java`

```java
package com.sanoysalvos.controller;

import com.sanoysalvos.model.ReporteMascota;
import com.sanoysalvos.model.TipoReporte;
import com.sanoysalvos.repository.ReporteMascotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
public class ReporteMascotaController {

    @Autowired
    private ReporteMascotaRepository reporteRepository;

    // GET /api/reportes — Todos los reportes
    @GetMapping
    public List<ReporteMascota> listarTodos() {
        return reporteRepository.findAll();
    }

    // GET /api/reportes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ReporteMascota> obtenerPorId(@PathVariable Long id) {
        return reporteRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/reportes/mascota/{mascotaId}
    @GetMapping("/mascota/{mascotaId}")
    public List<ReporteMascota> porMascota(@PathVariable Long mascotaId) {
        return reporteRepository.findByMascotaId(mascotaId);
    }

    // GET /api/reportes/tipo/{tipo}
    @GetMapping("/tipo/{tipo}")
    public List<ReporteMascota> porTipo(@PathVariable TipoReporte tipo) {
        return reporteRepository.findByTipo(tipo);
    }

    // POST /api/reportes — Crear reporte
    @PostMapping
    public ReporteMascota crear(@RequestBody ReporteMascota reporte) {
        return reporteRepository.save(reporte);
    }

    // PUT /api/reportes/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ReporteMascota> actualizar(@PathVariable Long id,
                                                      @RequestBody ReporteMascota datos) {
        return reporteRepository.findById(id).map(reporte -> {
            reporte.setMascotaId(datos.getMascotaId());
            reporte.setTipo(datos.getTipo());
            reporte.setDescripcion(datos.getDescripcion());
            reporte.setUbicacion(datos.getUbicacion());
            reporte.setLatitud(datos.getLatitud());
            reporte.setLongitud(datos.getLongitud());
            reporte.setContacto(datos.getContacto());
            return ResponseEntity.ok(reporteRepository.save(reporte));
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/reportes/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!reporteRepository.existsById(id)) return ResponseEntity.notFound().build();
        reporteRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 4. Backend — Controlador Kafka

### 4.1 Reemplazar `ControladorKafka.java`

**Ruta:** `src/main/java/com/sanoysalvos/kafka/ControladorKafka.java`

```java
package com.sanoysalvos.kafka;

import com.sanoysalvos.model.ReporteMascota;
import com.sanoysalvos.model.TipoReporte;
import com.sanoysalvos.repository.ReporteMascotaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ControladorKafka {

    @Autowired
    private ReporteMascotaRepository reporteRepository;

    /**
     * Escucha el tópico "mascotas-reportadas".
     * Cuando llega un evento de mascota PERDIDA o ENCONTRADA,
     * se genera automáticamente un ReporteMascota de seguimiento.
     */
    @KafkaListener(topics = "mascotas-reportadas", groupId = "sanos-y-salvos-group")
    public void procesarReporteMascota(String mensaje) {
        System.out.println("[Kafka] Mensaje recibido: " + mensaje);

        try {
            // Parseo básico del JSON (sin dependencia extra)
            Long mascotaId = extraerLong(mensaje, "id");
            String estadoStr = extraerString(mensaje, "estado");

            if (mascotaId == null || estadoStr == null) {
                System.out.println("[Kafka] Mensaje incompleto, descartando.");
                return;
            }

            // Solo crear reporte automático para PERDIDA o ENCONTRADA
            TipoReporte tipo;
            try {
                tipo = TipoReporte.valueOf(estadoStr);
            } catch (IllegalArgumentException e) {
                System.out.println("[Kafka] Estado no relevante: " + estadoStr);
                return;
            }

            ReporteMascota reporte = new ReporteMascota();
            reporte.setMascotaId(mascotaId);
            reporte.setTipo(tipo);
            reporte.setDescripcion("Reporte automático generado por el sistema.");
            reporte.setFecha(LocalDateTime.now());
            reporte.setContacto("sistema@sanoysalvos.cl");

            reporteRepository.save(reporte);
            System.out.println("[Kafka] ReporteMascota automático creado para mascotaId=" + mascotaId);

        } catch (Exception e) {
            System.err.println("[Kafka] Error procesando mensaje: " + e.getMessage());
        }
    }

    // ---- Helpers de parseo ----

    private Long extraerLong(String json, String clave) {
        try {
            String patron = "\"" + clave + "\":";
            int inicio = json.indexOf(patron) + patron.length();
            int fin = json.indexOf(",", inicio);
            if (fin == -1) fin = json.indexOf("}", inicio);
            return Long.parseLong(json.substring(inicio, fin).trim());
        } catch (Exception e) { return null; }
    }

    private String extraerString(String json, String clave) {
        try {
            String patron = "\"" + clave + "\":\"";
            int inicio = json.indexOf(patron) + patron.length();
            int fin = json.indexOf("\"", inicio);
            return json.substring(inicio, fin).trim();
        } catch (Exception e) { return null; }
    }
}
```

---

## 5. Backend — application.properties

**Ruta:** `src/main/resources/application.properties`

Reemplazar **todo el contenido** con:

```properties
# ============================================================
# Sanos y Salvos — application.properties
# ============================================================

spring.application.name=sanos-y-salvos

# ---------- Base de datos PostgreSQL ----------
spring.datasource.url=jdbc:postgresql://localhost:5432/sanoysalvos_db
spring.datasource.username=admin
spring.datasource.password=admin123
spring.datasource.driver-class-name=org.postgresql.Driver

# ---------- JPA / Hibernate ----------
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# ---------- Kafka ----------
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=sanos-y-salvos-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer

# Tópico principal
kafka.topic.mascotas=mascotas-reportadas

# ---------- Puerto ----------
server.port=8080
```

---

## 6. Backend — docker-compose.yml

**Ruta:** `docker-compose.yml` (raíz del proyecto)

Reemplazar **todo el contenido** con:

```yaml
version: '3.8'

services:

  # ---- Base de datos PostgreSQL ----
  db-sanoysalvos:
    image: postgres:15
    container_name: db-sanoysalvos
    environment:
      POSTGRES_DB: sanoysalvos_db
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin123
    ports:
      - "5432:5432"
    volumes:
      - pgdata_sanoysalvos:/var/lib/postgresql/data

  # ---- PgAdmin (interfaz visual de BD) ----
  pgadmin:
    image: dpage/pgadmin4
    container_name: pgadmin-sanoysalvos
    environment:
      PGADMIN_DEFAULT_EMAIL: admin@sanoysalvos.cl
      PGADMIN_DEFAULT_PASSWORD: admin123
    ports:
      - "5050:80"
    depends_on:
      - db-sanoysalvos

  # ---- Zookeeper (requerido por Kafka) ----
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    container_name: zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  # ---- Kafka ----
  kafka:
    image: confluentinc/cp-kafka:7.4.0
    container_name: kafka-sanoysalvos
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"

  # ---- Kafdrop (interfaz visual de Kafka) ----
  kafdrop:
    image: obsidiandynamics/kafdrop
    container_name: kafdrop-sanoysalvos
    depends_on:
      - kafka
    ports:
      - "9000:9000"
    environment:
      KAFKA_BROKERCONNECT: kafka:9092

volumes:
  pgdata_sanoysalvos:
```

---

## 7. Backend — pom.xml

En `pom.xml`, cambiar los campos de identificación del proyecto:

```xml
<!-- ANTES -->
<groupId>com.smartlogix</groupId>
<artifactId>smartlogix-backend</artifactId>
<name>SmartLogix</name>
<description>Sistema de inventario con alarmas</description>

<!-- DESPUÉS -->
<groupId>com.sanoysalvos</groupId>
<artifactId>sanoysalvos-backend</artifactId>
<name>SanosYSalvos</name>
<description>Plataforma de mascotas perdidas y encontradas</description>
```

> Las dependencias (Spring Boot, Spring Data JPA, Spring Kafka, PostgreSQL, Resilience4j) **se mantienen igual**. No es necesario agregar ni quitar dependencias.

---

## 8. Frontend — api.js

**Ruta:** `frontend/src/api.js`

Reemplazar **todo el archivo** con:

```javascript
// api.js — Sanos y Salvos
// Centraliza todas las llamadas al backend Spring Boot

const BASE_URL = "http://localhost:8080/api";

// ============================================================
// MASCOTAS
// ============================================================

export const getMascotas = async () => {
  const res = await fetch(`${BASE_URL}/mascotas`);
  if (!res.ok) throw new Error("Error al obtener mascotas");
  return res.json();
};

export const getMascotaById = async (id) => {
  const res = await fetch(`${BASE_URL}/mascotas/${id}`);
  if (!res.ok) throw new Error("Mascota no encontrada");
  return res.json();
};

export const getMascotasPorEstado = async (estado) => {
  const res = await fetch(`${BASE_URL}/mascotas/estado/${estado}`);
  if (!res.ok) throw new Error("Error al filtrar mascotas");
  return res.json();
};

export const crearMascota = async (mascota) => {
  const res = await fetch(`${BASE_URL}/mascotas`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(mascota),
  });
  if (!res.ok) throw new Error("Error al crear mascota");
  return res.json();
};

export const actualizarMascota = async (id, mascota) => {
  const res = await fetch(`${BASE_URL}/mascotas/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(mascota),
  });
  if (!res.ok) throw new Error("Error al actualizar mascota");
  return res.json();
};

export const eliminarMascota = async (id) => {
  const res = await fetch(`${BASE_URL}/mascotas/${id}`, {
    method: "DELETE",
  });
  if (!res.ok) throw new Error("Error al eliminar mascota");
};

// ============================================================
// REPORTES DE MASCOTA
// ============================================================

export const getReportes = async () => {
  const res = await fetch(`${BASE_URL}/reportes`);
  if (!res.ok) throw new Error("Error al obtener reportes");
  return res.json();
};

export const getReportesByMascota = async (mascotaId) => {
  const res = await fetch(`${BASE_URL}/reportes/mascota/${mascotaId}`);
  if (!res.ok) throw new Error("Error al obtener reportes de mascota");
  return res.json();
};

export const getReportesByTipo = async (tipo) => {
  const res = await fetch(`${BASE_URL}/reportes/tipo/${tipo}`);
  if (!res.ok) throw new Error("Error al filtrar reportes");
  return res.json();
};

export const crearReporte = async (reporte) => {
  const res = await fetch(`${BASE_URL}/reportes`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(reporte),
  });
  if (!res.ok) throw new Error("Error al crear reporte");
  return res.json();
};

export const actualizarReporte = async (id, reporte) => {
  const res = await fetch(`${BASE_URL}/reportes/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(reporte),
  });
  if (!res.ok) throw new Error("Error al actualizar reporte");
  return res.json();
};

export const eliminarReporte = async (id) => {
  const res = await fetch(`${BASE_URL}/reportes/${id}`, {
    method: "DELETE",
  });
  if (!res.ok) throw new Error("Error al eliminar reporte");
};
```

---

## 9. Frontend — Sidebar

**Ruta:** `frontend/src/components/Sidebar.jsx`

Reemplazar **todo el archivo** con:

```jsx
import React from "react";
import { Link, useLocation } from "react-router-dom";

const Sidebar = () => {
  const location = useLocation();

  const links = [
    { path: "/",          label: "🏠 Dashboard"  },
    { path: "/mascotas",  label: "🐾 Mascotas"   },
    { path: "/reportes",  label: "📋 Reportes"   },
  ];

  return (
    <aside style={styles.sidebar}>
      <div style={styles.logo}>
        <span style={styles.logoIcon}>🐶</span>
        <span style={styles.logoText}>Sanos y Salvos</span>
      </div>
      <nav>
        {links.map((link) => (
          <Link
            key={link.path}
            to={link.path}
            style={{
              ...styles.link,
              ...(location.pathname === link.path ? styles.activeLink : {}),
            }}
          >
            {link.label}
          </Link>
        ))}
      </nav>
    </aside>
  );
};

const styles = {
  sidebar: {
    width: "220px",
    minHeight: "100vh",
    backgroundColor: "#1a1a2e",
    padding: "24px 16px",
    display: "flex",
    flexDirection: "column",
    gap: "8px",
  },
  logo: {
    display: "flex",
    alignItems: "center",
    gap: "10px",
    marginBottom: "32px",
  },
  logoIcon: { fontSize: "28px" },
  logoText: {
    color: "#e2e8f0",
    fontWeight: "700",
    fontSize: "16px",
  },
  link: {
    display: "block",
    padding: "10px 14px",
    borderRadius: "8px",
    color: "#94a3b8",
    textDecoration: "none",
    fontSize: "14px",
    fontWeight: "500",
    transition: "background 0.2s, color 0.2s",
  },
  activeLink: {
    backgroundColor: "#3b82f6",
    color: "#ffffff",
  },
};

export default Sidebar;
```

---

## 10. Frontend — Dashboard

**Ruta:** `frontend/src/pages/Dashboard.jsx`

Reemplazar **todo el archivo** con:

```jsx
import React, { useEffect, useState } from "react";
import { getMascotas, getReportes } from "../api";

const Dashboard = () => {
  const [mascotas, setMascotas] = useState([]);
  const [reportes, setReportes] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([getMascotas(), getReportes()])
      .then(([m, r]) => {
        setMascotas(m);
        setReportes(r);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, []);

  const perdidas   = mascotas.filter((m) => m.estado === "PERDIDA").length;
  const encontradas = mascotas.filter((m) => m.estado === "ENCONTRADA").length;
  const reunidas   = mascotas.filter((m) => m.estado === "REUNIDA").length;

  const stats = [
    { label: "Mascotas Perdidas",    value: perdidas,    color: "#ef4444", icon: "🔴" },
    { label: "Mascotas Encontradas", value: encontradas, color: "#22c55e", icon: "🟢" },
    { label: "Reunidas con Dueño",   value: reunidas,    color: "#3b82f6", icon: "💙" },
    { label: "Total Reportes",       value: reportes.length, color: "#f59e0b", icon: "📋" },
  ];

  if (loading) return <p style={styles.loading}>Cargando datos...</p>;

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>Dashboard — Sanos y Salvos</h1>
      <p style={styles.subtitle}>Resumen general de mascotas reportadas</p>

      <div style={styles.grid}>
        {stats.map((stat) => (
          <div key={stat.label} style={{ ...styles.card, borderTop: `4px solid ${stat.color}` }}>
            <div style={styles.cardIcon}>{stat.icon}</div>
            <div style={{ ...styles.cardValue, color: stat.color }}>{stat.value}</div>
            <div style={styles.cardLabel}>{stat.label}</div>
          </div>
        ))}
      </div>

      <h2 style={styles.sectionTitle}>Últimas mascotas registradas</h2>
      <table style={styles.table}>
        <thead>
          <tr>
            {["Nombre", "Especie", "Color", "Estado", "Contacto"].map((col) => (
              <th key={col} style={styles.th}>{col}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {mascotas.slice(0, 5).map((m) => (
            <tr key={m.id}>
              <td style={styles.td}>{m.nombre}</td>
              <td style={styles.td}>{m.especie}</td>
              <td style={styles.td}>{m.color}</td>
              <td style={styles.td}>
                <span style={{
                  ...styles.badge,
                  backgroundColor:
                    m.estado === "PERDIDA"    ? "#fee2e2" :
                    m.estado === "ENCONTRADA" ? "#dcfce7" : "#dbeafe",
                  color:
                    m.estado === "PERDIDA"    ? "#dc2626" :
                    m.estado === "ENCONTRADA" ? "#16a34a" : "#1d4ed8",
                }}>
                  {m.estado}
                </span>
              </td>
              <td style={styles.td}>{m.contacto}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

const styles = {
  container: { padding: "32px" },
  title: { fontSize: "24px", fontWeight: "700", marginBottom: "4px" },
  subtitle: { color: "#64748b", marginBottom: "28px" },
  loading: { padding: "32px", color: "#64748b" },
  grid: { display: "grid", gridTemplateColumns: "repeat(4, 1fr)", gap: "16px", marginBottom: "36px" },
  card: { background: "#fff", borderRadius: "12px", padding: "20px", boxShadow: "0 1px 4px rgba(0,0,0,0.08)", textAlign: "center" },
  cardIcon: { fontSize: "28px", marginBottom: "8px" },
  cardValue: { fontSize: "36px", fontWeight: "800", marginBottom: "4px" },
  cardLabel: { fontSize: "13px", color: "#64748b" },
  sectionTitle: { fontSize: "18px", fontWeight: "600", marginBottom: "16px" },
  table: { width: "100%", borderCollapse: "collapse", background: "#fff", borderRadius: "8px", overflow: "hidden", boxShadow: "0 1px 4px rgba(0,0,0,0.08)" },
  th: { background: "#f1f5f9", padding: "12px 16px", textAlign: "left", fontSize: "13px", color: "#64748b", fontWeight: "600" },
  td: { padding: "12px 16px", fontSize: "14px", borderBottom: "1px solid #f1f5f9" },
  badge: { padding: "2px 10px", borderRadius: "999px", fontSize: "12px", fontWeight: "600" },
};

export default Dashboard;
```

---

## 11. Frontend — Página Mascotas

**Ruta:** `frontend/src/pages/Mascotas.jsx`

Reemplazar **todo el archivo** con:

```jsx
import React, { useEffect, useState } from "react";
import { getMascotas, crearMascota, eliminarMascota } from "../api";

const ESTADO_OPTS  = ["PERDIDA", "ENCONTRADA", "REUNIDA"];
const ESPECIE_OPTS = ["PERRO", "GATO", "AVE", "CONEJO", "OTRO"];
const TAMANO_OPTS  = ["PEQUENO", "MEDIANO", "GRANDE"];

const formVacio = {
  nombre: "", especie: "PERRO", raza: "", color: "",
  tamano: "MEDIANO", descripcion: "", fotoUrl: "",
  latitud: "", longitud: "", contacto: "", estado: "PERDIDA",
};

const Mascotas = () => {
  const [mascotas, setMascotas] = useState([]);
  const [form, setForm] = useState(formVacio);
  const [mostrarForm, setMostrarForm] = useState(false);
  const [filtro, setFiltro] = useState("TODOS");
  const [loading, setLoading] = useState(true);

  const cargar = () =>
    getMascotas()
      .then(setMascotas)
      .catch(console.error)
      .finally(() => setLoading(false));

  useEffect(() => { cargar(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    await crearMascota(form);
    setForm(formVacio);
    setMostrarForm(false);
    cargar();
  };

  const handleEliminar = async (id) => {
    if (!window.confirm("¿Eliminar mascota?")) return;
    await eliminarMascota(id);
    cargar();
  };

  const mascotasFiltradas =
    filtro === "TODOS" ? mascotas : mascotas.filter((m) => m.estado === filtro);

  if (loading) return <p style={{ padding: "32px" }}>Cargando...</p>;

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h1 style={styles.title}>🐾 Mascotas</h1>
        <button style={styles.btnPrimary} onClick={() => setMostrarForm(!mostrarForm)}>
          {mostrarForm ? "Cancelar" : "+ Registrar Mascota"}
        </button>
      </div>

      {/* Filtros */}
      <div style={styles.filtros}>
        {["TODOS", ...ESTADO_OPTS].map((op) => (
          <button
            key={op}
            style={{ ...styles.filtroBtn, ...(filtro === op ? styles.filtroBtnActive : {}) }}
            onClick={() => setFiltro(op)}
          >
            {op}
          </button>
        ))}
      </div>

      {/* Formulario */}
      {mostrarForm && (
        <form onSubmit={handleSubmit} style={styles.form}>
          <h3 style={{ marginBottom: "16px" }}>Nueva Mascota</h3>
          <div style={styles.formGrid}>
            {[
              { key: "nombre",   label: "Nombre",    type: "text"   },
              { key: "raza",     label: "Raza",      type: "text"   },
              { key: "color",    label: "Color",     type: "text"   },
              { key: "fotoUrl",  label: "URL Foto",  type: "text"   },
              { key: "latitud",  label: "Latitud",   type: "number" },
              { key: "longitud", label: "Longitud",  type: "number" },
              { key: "contacto", label: "Contacto",  type: "text"   },
            ].map(({ key, label, type }) => (
              <div key={key}>
                <label style={styles.label}>{label}</label>
                <input
                  type={type}
                  value={form[key]}
                  onChange={(e) => setForm({ ...form, [key]: e.target.value })}
                  style={styles.input}
                  required={key === "nombre"}
                  step={type === "number" ? "any" : undefined}
                />
              </div>
            ))}

            {[
              { key: "especie", label: "Especie", opts: ESPECIE_OPTS },
              { key: "tamano",  label: "Tamaño",  opts: TAMANO_OPTS  },
              { key: "estado",  label: "Estado",  opts: ESTADO_OPTS  },
            ].map(({ key, label, opts }) => (
              <div key={key}>
                <label style={styles.label}>{label}</label>
                <select value={form[key]} onChange={(e) => setForm({ ...form, [key]: e.target.value })} style={styles.input}>
                  {opts.map((o) => <option key={o}>{o}</option>)}
                </select>
              </div>
            ))}

            <div style={{ gridColumn: "1 / -1" }}>
              <label style={styles.label}>Descripción</label>
              <textarea value={form.descripcion} onChange={(e) => setForm({ ...form, descripcion: e.target.value })} style={{ ...styles.input, height: "80px" }} />
            </div>
          </div>
          <button type="submit" style={styles.btnPrimary}>Guardar</button>
        </form>
      )}

      {/* Tabla */}
      <table style={styles.table}>
        <thead>
          <tr>{["Nombre", "Especie", "Raza", "Color", "Estado", "Contacto", "Acciones"].map((c) => <th key={c} style={styles.th}>{c}</th>)}</tr>
        </thead>
        <tbody>
          {mascotasFiltradas.map((m) => (
            <tr key={m.id}>
              <td style={styles.td}>{m.nombre}</td>
              <td style={styles.td}>{m.especie}</td>
              <td style={styles.td}>{m.raza || "—"}</td>
              <td style={styles.td}>{m.color}</td>
              <td style={styles.td}>
                <span style={{ ...styles.badge, background: m.estado === "PERDIDA" ? "#fee2e2" : m.estado === "ENCONTRADA" ? "#dcfce7" : "#dbeafe", color: m.estado === "PERDIDA" ? "#dc2626" : m.estado === "ENCONTRADA" ? "#16a34a" : "#1d4ed8" }}>
                  {m.estado}
                </span>
              </td>
              <td style={styles.td}>{m.contacto}</td>
              <td style={styles.td}>
                <button style={styles.btnDanger} onClick={() => handleEliminar(m.id)}>Eliminar</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

const styles = {
  container: { padding: "32px" },
  header: { display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "20px" },
  title: { fontSize: "22px", fontWeight: "700" },
  filtros: { display: "flex", gap: "8px", marginBottom: "20px" },
  filtroBtn: { padding: "6px 14px", borderRadius: "999px", border: "1px solid #e2e8f0", cursor: "pointer", fontSize: "13px", background: "#fff" },
  filtroBtnActive: { background: "#3b82f6", color: "#fff", border: "1px solid #3b82f6" },
  form: { background: "#fff", padding: "24px", borderRadius: "12px", marginBottom: "24px", boxShadow: "0 1px 4px rgba(0,0,0,0.08)" },
  formGrid: { display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: "16px", marginBottom: "16px" },
  label: { display: "block", fontSize: "13px", color: "#64748b", marginBottom: "4px" },
  input: { width: "100%", padding: "8px 12px", border: "1px solid #e2e8f0", borderRadius: "8px", fontSize: "14px", boxSizing: "border-box" },
  table: { width: "100%", borderCollapse: "collapse", background: "#fff", borderRadius: "8px", overflow: "hidden", boxShadow: "0 1px 4px rgba(0,0,0,0.08)" },
  th: { background: "#f1f5f9", padding: "12px 16px", textAlign: "left", fontSize: "13px", color: "#64748b", fontWeight: "600" },
  td: { padding: "12px 16px", fontSize: "14px", borderBottom: "1px solid #f1f5f9" },
  badge: { padding: "2px 10px", borderRadius: "999px", fontSize: "12px", fontWeight: "600" },
  btnPrimary: { padding: "8px 18px", background: "#3b82f6", color: "#fff", border: "none", borderRadius: "8px", cursor: "pointer", fontSize: "14px", fontWeight: "600" },
  btnDanger: { padding: "6px 12px", background: "#ef4444", color: "#fff", border: "none", borderRadius: "6px", cursor: "pointer", fontSize: "13px" },
};

export default Mascotas;
```

---

## 12. Frontend — Página Reportes

**Ruta:** `frontend/src/pages/Reportes.jsx`

Reemplazar **todo el archivo** con:

```jsx
import React, { useEffect, useState } from "react";
import { getReportes, crearReporte, eliminarReporte } from "../api";

const TIPO_OPTS = ["PERDIDA", "ENCONTRADA"];

const formVacio = {
  mascotaId: "", tipo: "PERDIDA",
  descripcion: "", ubicacion: "",
  latitud: "", longitud: "", contacto: "",
};

const Reportes = () => {
  const [reportes, setReportes] = useState([]);
  const [form, setForm] = useState(formVacio);
  const [mostrarForm, setMostrarForm] = useState(false);
  const [loading, setLoading] = useState(true);

  const cargar = () =>
    getReportes()
      .then(setReportes)
      .catch(console.error)
      .finally(() => setLoading(false));

  useEffect(() => { cargar(); }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    await crearReporte({
      ...form,
      mascotaId: Number(form.mascotaId),
      latitud: form.latitud ? Number(form.latitud) : null,
      longitud: form.longitud ? Number(form.longitud) : null,
    });
    setForm(formVacio);
    setMostrarForm(false);
    cargar();
  };

  const handleEliminar = async (id) => {
    if (!window.confirm("¿Eliminar reporte?")) return;
    await eliminarReporte(id);
    cargar();
  };

  if (loading) return <p style={{ padding: "32px" }}>Cargando...</p>;

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h1 style={styles.title}>📋 Reportes</h1>
        <button style={styles.btnPrimary} onClick={() => setMostrarForm(!mostrarForm)}>
          {mostrarForm ? "Cancelar" : "+ Nuevo Reporte"}
        </button>
      </div>

      {mostrarForm && (
        <form onSubmit={handleSubmit} style={styles.form}>
          <h3 style={{ marginBottom: "16px" }}>Nuevo Reporte de Mascota</h3>
          <div style={styles.formGrid}>
            <div>
              <label style={styles.label}>ID Mascota</label>
              <input type="number" value={form.mascotaId} onChange={(e) => setForm({ ...form, mascotaId: e.target.value })} style={styles.input} required />
            </div>
            <div>
              <label style={styles.label}>Tipo</label>
              <select value={form.tipo} onChange={(e) => setForm({ ...form, tipo: e.target.value })} style={styles.input}>
                {TIPO_OPTS.map((t) => <option key={t}>{t}</option>)}
              </select>
            </div>
            <div>
              <label style={styles.label}>Ubicación (texto)</label>
              <input type="text" value={form.ubicacion} onChange={(e) => setForm({ ...form, ubicacion: e.target.value })} style={styles.input} />
            </div>
            <div>
              <label style={styles.label}>Latitud</label>
              <input type="number" step="any" value={form.latitud} onChange={(e) => setForm({ ...form, latitud: e.target.value })} style={styles.input} />
            </div>
            <div>
              <label style={styles.label}>Longitud</label>
              <input type="number" step="any" value={form.longitud} onChange={(e) => setForm({ ...form, longitud: e.target.value })} style={styles.input} />
            </div>
            <div>
              <label style={styles.label}>Contacto</label>
              <input type="text" value={form.contacto} onChange={(e) => setForm({ ...form, contacto: e.target.value })} style={styles.input} />
            </div>
            <div style={{ gridColumn: "1 / -1" }}>
              <label style={styles.label}>Descripción</label>
              <textarea value={form.descripcion} onChange={(e) => setForm({ ...form, descripcion: e.target.value })} style={{ ...styles.input, height: "80px" }} />
            </div>
          </div>
          <button type="submit" style={styles.btnPrimary}>Guardar Reporte</button>
        </form>
      )}

      <table style={styles.table}>
        <thead>
          <tr>
            {["ID", "Mascota ID", "Tipo", "Ubicación", "Contacto", "Fecha", "Acciones"].map((c) => (
              <th key={c} style={styles.th}>{c}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {reportes.map((r) => (
            <tr key={r.id}>
              <td style={styles.td}>{r.id}</td>
              <td style={styles.td}>{r.mascotaId}</td>
              <td style={styles.td}>
                <span style={{ ...styles.badge, background: r.tipo === "PERDIDA" ? "#fee2e2" : "#dcfce7", color: r.tipo === "PERDIDA" ? "#dc2626" : "#16a34a" }}>
                  {r.tipo}
                </span>
              </td>
              <td style={styles.td}>{r.ubicacion || "—"}</td>
              <td style={styles.td}>{r.contacto || "—"}</td>
              <td style={styles.td}>{r.fecha ? new Date(r.fecha).toLocaleDateString("es-CL") : "—"}</td>
              <td style={styles.td}>
                <button style={styles.btnDanger} onClick={() => handleEliminar(r.id)}>Eliminar</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

const styles = {
  container: { padding: "32px" },
  header: { display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "20px" },
  title: { fontSize: "22px", fontWeight: "700" },
  form: { background: "#fff", padding: "24px", borderRadius: "12px", marginBottom: "24px", boxShadow: "0 1px 4px rgba(0,0,0,0.08)" },
  formGrid: { display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: "16px", marginBottom: "16px" },
  label: { display: "block", fontSize: "13px", color: "#64748b", marginBottom: "4px" },
  input: { width: "100%", padding: "8px 12px", border: "1px solid #e2e8f0", borderRadius: "8px", fontSize: "14px", boxSizing: "border-box" },
  table: { width: "100%", borderCollapse: "collapse", background: "#fff", borderRadius: "8px", overflow: "hidden", boxShadow: "0 1px 4px rgba(0,0,0,0.08)" },
  th: { background: "#f1f5f9", padding: "12px 16px", textAlign: "left", fontSize: "13px", color: "#64748b", fontWeight: "600" },
  td: { padding: "12px 16px", fontSize: "14px", borderBottom: "1px solid #f1f5f9" },
  badge: { padding: "2px 10px", borderRadius: "999px", fontSize: "12px", fontWeight: "600" },
  btnPrimary: { padding: "8px 18px", background: "#3b82f6", color: "#fff", border: "none", borderRadius: "8px", cursor: "pointer", fontSize: "14px", fontWeight: "600" },
  btnDanger: { padding: "6px 12px", background: "#ef4444", color: "#fff", border: "none", borderRadius: "6px", cursor: "pointer", fontSize: "13px" },
};

export default Reportes;
```

---

## 13. Frontend — App.jsx

**Ruta:** `frontend/src/App.jsx`

Reemplazar **todo el archivo** con:

```jsx
import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Sidebar from "./components/Sidebar";
import Dashboard from "./pages/Dashboard";
import Mascotas from "./pages/Mascotas";
import Reportes from "./pages/Reportes";

function App() {
  return (
    <Router>
      <div style={styles.layout}>
        <Sidebar />
        <main style={styles.main}>
          <Routes>
            <Route path="/"          element={<Dashboard />} />
            <Route path="/mascotas"  element={<Mascotas />}  />
            <Route path="/reportes"  element={<Reportes />}  />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

const styles = {
  layout: {
    display: "flex",
    minHeight: "100vh",
    fontFamily: "'Inter', 'Segoe UI', sans-serif",
    background: "#f8fafc",
  },
  main: {
    flex: 1,
    overflowY: "auto",
  },
};

export default App;
```

---

## Resumen de renombramientos

| Antes (SmartLogix / Municipalidad) | Después (Sanos y Salvos) |
|---|---|
| `Producto.java` | `Mascota.java` |
| `Venta.java` | `ReporteMascota.java` |
| `ProductoRepository.java` | `MascotaRepository.java` |
| `VentaRepository.java` | `ReporteMascotaRepository.java` |
| `ProductoController.java` | `MascotaController.java` |
| `VentaController.java` | `ReporteMascotaController.java` |
| `/api/productos` | `/api/mascotas` |
| `/api/ventas` | `/api/reportes` |
| Tópico Kafka `incendios-reportados` | Tópico Kafka `mascotas-reportadas` |
| BD `municipalidad_db` | BD `sanoysalvos_db` |
| `pages/Productos.jsx` | `pages/Mascotas.jsx` |
| `pages/Ventas.jsx` | `pages/Reportes.jsx` |

---

*Guía generada para el curso DSY1106 — Desarrollo Fullstack III*  
*Caso Semestral: Sanos y Salvos*
