package com.User.taller_mecanico_backend.cliente;

import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {
    
    public Cliente toEntity(ClienteDTO dto){
        return new Cliente(dto.nombre(), dto.contacto());
    }

    public ClienteDTO toDto(Cliente cliente){
        return new ClienteDTO(cliente.getId(), cliente.getNombre(), cliente.getContacto(), cliente.isActivo());
    }
}
