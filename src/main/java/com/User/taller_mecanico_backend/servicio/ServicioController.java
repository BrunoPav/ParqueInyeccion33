package com.User.taller_mecanico_backend.servicio;

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
@RequestMapping("/api/servicios")
public class ServicioController {
    private final ServicioService servicioService;

    public ServicioController(ServicioService servicioService) {
        this.servicioService = servicioService;
    }

    @GetMapping
    public List<ServicioDTO> listarServicios(@RequestParam(required = false) Long vehiculoId) {
        return vehiculoId == null ? 
            servicioService.listarServicios() : 
            servicioService.listarServiciosPorVehiculoIdOrdenados(vehiculoId);
    } 

    @GetMapping("/{id}")
    public ServicioDTO obtenerPorId(@PathVariable Long id) {
        return servicioService.buscarServicioPorId(id);
    }

    @PostMapping
    public ResponseEntity<ServicioDTO> crearServicio(@RequestBody ServicioDTO dto) {
        ServicioDTO servicio = servicioService.crearServicio(dto);
        return ResponseEntity.created(URI.create("/api/servicios/" + servicio.id()))
                .body(servicio);
    }
    
    @PutMapping("/{id}")
    public ServicioDTO reemplazarServicio(@PathVariable Long id, @RequestBody ServicioDTO dto) {
        return servicioService.reemplazarServicio(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarServicio(@PathVariable Long id) {
        servicioService.eliminarServicio(id);
    }

}
