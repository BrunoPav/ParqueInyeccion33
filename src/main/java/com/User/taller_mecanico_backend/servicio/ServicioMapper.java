package com.User.taller_mecanico_backend.servicio;

import org.springframework.stereotype.Component;

import com.User.taller_mecanico_backend.vehiculo.Vehiculo;

@Component
public class ServicioMapper {
    
    public Servicio toEntity(ServicioDTO dto, Vehiculo vehiculo) {
        return new Servicio(dto.fecha(), dto.descripcion(), dto.precio(), vehiculo);
    }

    public ServicioDTO toDto(Servicio servicio) {
        return new ServicioDTO (servicio.getId(), servicio.getFecha(), servicio.getDescripcion(), servicio.getPrecio(), servicio.getVehiculo().getId());
    }

}