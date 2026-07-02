package com.duoc.ms_pedidos.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.ms_pedidos.dto.PedidoRequest;
import com.duoc.ms_pedidos.dto.PedidoResponse;
import com.duoc.ms_pedidos.service.PedidoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Gestión de pedidos y su enriquecimiento con datos de producto")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar pedidos", description = "Devuelve todos los pedidos registrados, enriquecidos con los datos del producto asociado")
    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    public List<PedidoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pedido por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un pedido con ese ID")
    })
    public ResponseEntity<PedidoResponse> buscarPorId(
            @Parameter(description = "ID del pedido a buscar") @PathVariable Long id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear un nuevo pedido")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de pedido inválidos")
    })
    public ResponseEntity<PedidoResponse> crear(@RequestBody PedidoRequest nuevo) {
        PedidoResponse creado = service.crear(nuevo.getProductoId(), nuevo.getCantidad());
        return ResponseEntity.status(201).body(creado);
    }
}
