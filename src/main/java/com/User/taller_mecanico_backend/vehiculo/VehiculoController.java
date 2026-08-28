package com.User.taller_mecanico_backend.vehiculo;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {
    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping
    public List<VehiculoDTO> listarVehiculos(@RequestParam(required = false) Long clienteId) {
        return clienteId == null ? 
            vehiculoService.listarVehiculos() : 
            vehiculoService.listarPorCliente(clienteId);
    }

    @GetMapping("/{id}")
    public VehiculoDTO obtenerPorId(@PathVariable Long id) {
        return vehiculoService.buscarVehiculoPorId(id);
    }

    @PostMapping
    public ResponseEntity<VehiculoDTO> crearVehiculo(@RequestBody VehiculoDTO dto) {
        VehiculoDTO vehiculo = vehiculoService.crearVehiculo(dto);
        return ResponseEntity.created(URI.create("/api/vehiculos/" + vehiculo.id()))
                .body(vehiculo);
    }

    @PutMapping("/{id}")
    public VehiculoDTO reemplazarVehiculo(@PathVariable Long id, @RequestBody VehiculoDTO dto) {
        return vehiculoService.reemplazarVehiculo(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarVehiculo(@PathVariable Long id) {
        vehiculoService.eliminarVehiculo(id);
    }

}
