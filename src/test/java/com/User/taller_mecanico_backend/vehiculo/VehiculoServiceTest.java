package com.User.taller_mecanico_backend.vehiculo;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.User.taller_mecanico_backend.cliente.Cliente;
import com.User.taller_mecanico_backend.cliente.ClienteRepository;
import com.User.taller_mecanico_backend.common.RecursoExistente;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    private VehiculoService vehiculoService;

    @BeforeEach
    void inicializar() {
        vehiculoService = new VehiculoService(vehiculoRepository, new VehiculoMapper(), clienteRepository);
    }

    @Test
    void crearVehiculo_cuandoPatenteExiste_lanzaRecursoExistente() {
        when(vehiculoRepository.existsByPatente("ABC123")).thenReturn(true);

        VehiculoDTO dto = new VehiculoDTO(null, "Toyota", "Corolla", 2020, "ABC123", 15555, 12L);

        RecursoExistente ex = assertThrows(
                RecursoExistente.class,
                () -> vehiculoService.crearVehiculo(dto));

        assertEquals("Vehiculo con patente ABC123 ya existe", ex.getMessage());
    }

    @Test
    void reemplazarVehiculo_cuandoConservaSuPropiaPatente_noLanzaRecursoExistente() {
        Vehiculo existente = new Vehiculo();
        Cliente duenio = new Cliente("Juan Perez", "11-2233-4455");

        when(vehiculoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(vehiculoRepository.existsByPatenteAndIdNot("ABC123", 1L)).thenReturn(false);
        when(clienteRepository.findById(12L)).thenReturn(Optional.of(duenio));
        when(vehiculoRepository.save(existente)).thenReturn(existente);

        VehiculoDTO dto = new VehiculoDTO(1L, "Toyota", "Corolla", 2020, "ABC123", 15555, 12L);

        VehiculoDTO resultado = assertDoesNotThrow(
                () -> vehiculoService.reemplazarVehiculo(1L, dto));

        assertEquals("ABC123", resultado.patente());
        assertEquals(15555, resultado.kilometraje());
    }
}

