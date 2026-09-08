package com.User.taller_mecanico_backend.vehiculo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.User.taller_mecanico_backend.cliente.Cliente;
import com.User.taller_mecanico_backend.cliente.ClienteRepository;

@DataJpaTest
class VehiculoRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Test
    void findByClienteId_devuelveSoloLosVehiculosDeEseCliente() {
        Cliente juan = clienteRepository.save(new Cliente("Juan Perez", "11-1111-1111"));
        Cliente maria = clienteRepository.save(new Cliente("Maria Lopez", "22-2222-2222"));

        vehiculoRepository.save(new Vehiculo("Ford", "Focus", 2018, "AAA111", 80000, juan));
        vehiculoRepository.save(new Vehiculo("Renault", "Clio", 2012, "CCC333", 190000, juan));
        vehiculoRepository.save(new Vehiculo("Volkswagen", "Gol", 2015, "BBB222", 140000, maria));

        List<Vehiculo> deJuan = vehiculoRepository.findByCliente_Id(juan.getId());

        assertEquals(2, deJuan.size());
        assertEquals(List.of("AAA111", "CCC333"),
                deJuan.stream().map(Vehiculo::getPatente).sorted().toList());
    }

    @Test
    void findByClienteActivoTrue_excluyeLosVehiculosDeClientesInactivos() {
        Cliente activo = clienteRepository.save(new Cliente("Cliente Activo", "11-1111-1111"));

        Cliente inactivo = new Cliente("Cliente Inactivo", "22-2222-2222");
        inactivo.setActivo(false);
        Cliente inactivoGuardado = clienteRepository.save(inactivo);

        vehiculoRepository.save(new Vehiculo("Ford", "Focus", 2018, "AAA111", 80000, activo));
        vehiculoRepository.save(new Vehiculo("Volkswagen", "Gol", 2015, "BBB222", 140000, inactivoGuardado));

        List<Vehiculo> visibles = vehiculoRepository.findByCliente_ActivoTrue();

        assertEquals(1, visibles.size());
        assertEquals("AAA111", visibles.get(0).getPatente());
    }
}
