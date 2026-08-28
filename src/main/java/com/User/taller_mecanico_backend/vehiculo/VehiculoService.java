package com.User.taller_mecanico_backend.vehiculo;

import java.util.List;

import org.springframework.stereotype.Service;

import com.User.taller_mecanico_backend.cliente.Cliente;
import com.User.taller_mecanico_backend.cliente.ClienteRepository;
import com.User.taller_mecanico_backend.common.RecursoExistente;
import com.User.taller_mecanico_backend.common.RecursoNoEncontradoException;

@Service
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final VehiculoMapper vehiculoMapper;
    private final ClienteRepository clienteRepository;

    public VehiculoService(VehiculoRepository vehiculoRepository, VehiculoMapper vahiculoMapper,
            ClienteRepository clienteRepository) {
        this.vehiculoRepository = vehiculoRepository;
        this.vehiculoMapper = vahiculoMapper;
        this.clienteRepository = clienteRepository;
    }

    public VehiculoDTO crearVehiculo(VehiculoDTO dto) {
        if (vehiculoRepository.existsByPatente(dto.patente())) {
            throw new RecursoExistente("Vehiculo", "patente", dto.patente());
        }
        Cliente cliente = clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", dto.clienteId()));
        Vehiculo vehiculo = vehiculoRepository.save(vehiculoMapper.toEntity(dto, cliente));
        return vehiculoMapper.toDto(vehiculo);
    }

    public List<VehiculoDTO> listarVehiculos() {
        return vehiculoRepository.findAll()
                .stream()
                .map(vehiculoMapper::toDto)
                .toList();
    }

    public VehiculoDTO buscarVehiculoPorId(Long id) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehiculo", id));
        return vehiculoMapper.toDto(vehiculo);
    }

    public VehiculoDTO reemplazarVehiculo(Long id, VehiculoDTO dto) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehiculo", id));
        if (vehiculoRepository.existsByPatenteAndIdNot(dto.patente(), id)) {
            throw new RecursoExistente("Vehiculo", "patente", dto.patente());
        }
        vehiculo.setCliente(clienteRepository.findById(dto.clienteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente", dto.clienteId())));
        vehiculo.setMarca(dto.marca());
        vehiculo.setModelo(dto.modelo());
        vehiculo.setAnio(dto.anio());
        vehiculo.setPatente(dto.patente());
        vehiculo.setKilometraje(dto.kilometraje());
        Vehiculo actualizado = vehiculoRepository.save(vehiculo);
        return vehiculoMapper.toDto(actualizado);
    }

    public void eliminarVehiculo(Long id) {
        if (!vehiculoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Vehiculo", id);
        }
        vehiculoRepository.deleteById(id);
    }

    public VehiculoDTO buscarVehiculoPorPatente(String patente) {
        Vehiculo vehiculo = vehiculoRepository.findByPatente(patente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehiculo", "patente", patente));
        return vehiculoMapper.toDto(vehiculo);
    }

    public List<VehiculoDTO> listarPorCliente(Long clienteId) {
        return vehiculoRepository.findByCliente_Id(clienteId)
                .stream()
                .map(vehiculoMapper::toDto)
                .toList();
    }

}
