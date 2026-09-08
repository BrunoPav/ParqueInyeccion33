package com.User.taller_mecanico_backend.cliente;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.User.taller_mecanico_backend.common.RecursoNoEncontradoException;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    private ClienteService clienteService;

    @BeforeEach
    void inicializar() {
        clienteService = new ClienteService(clienteRepository, new ClienteMapper());
    }

    @Test
    void buscarClientePorId_cuandoNoExiste_lanzaRecursoNoEncontrado() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNoEncontradoException ex = assertThrows(
                RecursoNoEncontradoException.class,
                () -> clienteService.buscarClientePorId(99L));

        assertEquals("Cliente con id 99 no encontrado", ex.getMessage());
    }

    @Test
    void cambiarEstadoCliente_aplicadoDosVeces_noLanzaExcepcion() {
        Cliente cliente = new Cliente("Juan Perez", "11-2233-4455");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        clienteService.cambiarEstadoCliente(1L, false);
        ClienteDTO primera = assertDoesNotThrow(() -> clienteService.cambiarEstadoCliente(1L, false));
        assertEquals(false, primera.activo());

        clienteService.cambiarEstadoCliente(1L, false);
        ClienteDTO segunda = assertDoesNotThrow(() -> clienteService.cambiarEstadoCliente(1L, false));
        assertEquals(false, segunda.activo());

    }

    @Test
    void crearCliente_ignoraActivoEnDTO_siempreCreaActivo() {
        ClienteDTO dto = new ClienteDTO(null, "Maria Lopez", "22-3344-5566", false);

        // El mock devuelve la misma entidad que recibio, como haria un save real.
        // Si devolviera un Cliente construido aca, la assertion del activo pasaria
        // siempre y no probaria nada del mapper.
        when(clienteRepository.save(any(Cliente.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        ClienteDTO resultado = assertDoesNotThrow(() -> clienteService.crearCliente(dto));

        assertEquals("Maria Lopez", resultado.nombre());
        assertEquals("22-3344-5566", resultado.contacto());
        assertEquals(true, resultado.activo());
    }

}
