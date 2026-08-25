package com.User.taller_mecanico_backend.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    public List<Cliente> findByNombre(String nombre); 
    
}
