package com.User.taller_mecanico_backend.servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.User.taller_mecanico_backend.common.RecursoNoEncontradoException;
import com.User.taller_mecanico_backend.vehiculo.Vehiculo;
import com.User.taller_mecanico_backend.vehiculo.VehiculoRepository;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final ServicioMapper servicioMapper;
    private final VehiculoRepository vehiculoRepository;

    public ServicioService(ServicioRepository servicioRepository, ServicioMapper servicioMapper,
            VehiculoRepository vehiculoRepository) {
        this.servicioRepository = servicioRepository;
        this.servicioMapper = servicioMapper;
        this.vehiculoRepository = vehiculoRepository;
    }

    public ServicioDTO crearServicio(ServicioDTO dto) {
        Vehiculo vehiculo = vehiculoRepository.findById(dto.vehiculoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehiculo", dto.vehiculoId()));
        Servicio servicio = servicioRepository.save(servicioMapper.toEntity(dto, vehiculo));
        return servicioMapper.toDto(servicio);
    }

    public List<ServicioDTO> listarServicios() {
        return servicioRepository.findAll()
                .stream()
                .map(servicioMapper::toDto)
                .toList();
    }

    public ServicioDTO buscarServicioPorId(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio", id));
        return servicioMapper.toDto(servicio);
    }

    public List<ServicioDTO> listarServiciosPorVehiculoIdOrdenados(Long vehiculoId) {
        if (!vehiculoRepository.existsById(vehiculoId)) {
            throw new RecursoNoEncontradoException("Vehiculo", vehiculoId);
        }
        return servicioRepository.findByVehiculo_IdOrderByFechaDesc(vehiculoId)
                .stream()
                .map(servicioMapper::toDto)
                .toList();
    }

    public void eliminarServicio(Long id) {
        if (!servicioRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Servicio", id);
        }
        servicioRepository.deleteById(id);
    }

    public ServicioDTO reemplazarServicio(Long id, ServicioDTO dto) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio", id));
        Vehiculo vehiculo = vehiculoRepository.findById(dto.vehiculoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehiculo", dto.vehiculoId()));
        servicio.setVehiculo(vehiculo);
        servicio.setFecha(dto.fecha());
        servicio.setDescripcion(dto.descripcion());
        servicio.setPrecio(dto.precio());
        Servicio servicioActualizado = servicioRepository.save(servicio);
        return servicioMapper.toDto(servicioActualizado);
    }

}
