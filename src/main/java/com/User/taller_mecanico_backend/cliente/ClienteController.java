package com.User.taller_mecanico_backend.cliente;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }


    @GetMapping
    public List<ClienteDTO> listar(@RequestParam(required = false) String nombre) {
        return nombre == null
            ? clienteService.listarClientes()
            : clienteService.buscarClientesPorNombre(nombre);
    }

    @GetMapping("/{id}")
    public ClienteDTO obtenerPorId(@PathVariable Long id) {
        return clienteService.buscarClientePorId(id);
    }
    
    @PostMapping
    public ResponseEntity<ClienteDTO> crearCliente(@RequestBody ClienteDTO dto) {
        ClienteDTO cliente = clienteService.crearCliente(dto);
        return ResponseEntity.created(URI.create("/api/clientes/" + cliente.id()))
                            .body(cliente);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarCliente(@PathVariable Long id){
        clienteService.eliminarCliente(id);
    }

    @PutMapping("/{id}")
    public ClienteDTO reemplazarCliente(@PathVariable Long id, @RequestBody ClienteDTO dto){
        return clienteService.reemplazarCliente(id,dto);
    }

}