package com.User.taller_mecanico_backend.vehiculo;

import org.springframework.stereotype.Component;

import com.User.taller_mecanico_backend.cliente.Cliente;

@Component
public class VehiculoMapper {

    public Vehiculo toEntity(VehiculoDTO dto, Cliente cliente) {
        return new Vehiculo(dto.marca(), dto.modelo(), dto.anio(), dto.patente(), dto.kilometraje(), cliente);
    }

    

    public VehiculoDTO toDto(Vehiculo entidad) {
        return new VehiculoDTO(entidad.getId(), entidad.getMarca(), entidad.getModelo(), entidad.getAnio(),
                entidad.getPatente(), entidad.getKilometraje(), entidad.getCliente().getId());
    }
}
