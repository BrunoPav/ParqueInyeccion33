package com.User.taller_mecanico_backend.cliente;

import java.util.List;

import org.springframework.stereotype.Service;

import com.User.taller_mecanico_backend.common.RecursoNoEncontradoException;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    public ClienteService(ClienteRepository clienteRepository, ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
    }

    public ClienteDTO crearCliente(ClienteDTO dto) {
        Cliente cliente = clienteMapper.toEntity(dto);
        Cliente guardado = clienteRepository.save(cliente);
        return clienteMapper.toDto(guardado);
    }

    public List<ClienteDTO> listarClientes(boolean activo) {
        return clienteRepository.findByActivo(activo)
                .stream()
                .map(clienteMapper::toDto)
                .toList();
    }

    public ClienteDTO buscarClientePorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));
        return clienteMapper.toDto(cliente);
    }

    public List<ClienteDTO> buscarClientesPorNombre(String nombre, boolean activo) {
        return clienteRepository.findByNombreAndActivo(nombre, activo)
                .stream()
                .map(clienteMapper::toDto)
                .toList();
    }

    public ClienteDTO cambiarEstadoCliente(Long id, boolean activo) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));
        cliente.setActivo(activo);
        Cliente actualizado = clienteRepository.save(cliente);
        return clienteMapper.toDto(actualizado);
    }

    public ClienteDTO reemplazarCliente(Long id, ClienteDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", id));
        cliente.setNombre(dto.nombre());
        cliente.setContacto(dto.contacto());
        Cliente actualizado = clienteRepository.save(cliente);
        return clienteMapper.toDto(actualizado);
    }

}
