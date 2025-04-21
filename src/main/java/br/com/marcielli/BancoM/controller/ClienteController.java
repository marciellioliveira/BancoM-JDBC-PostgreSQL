package br.com.marcielli.BancoM.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.marcielli.BancoM.dto.ClienteCreateDTO;
import br.com.marcielli.BancoM.dto.ClienteMapper;
import br.com.marcielli.BancoM.dto.ClienteResponseDTO;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Endereco;
import br.com.marcielli.BancoM.enuns.Role;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.service.ClienteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/clientes") 
public class ClienteController {

	@Autowired
	private ClienteService clienteService;

	@Autowired
	private ClienteMapper clienteMapper;
	
	 // Constantes para facilitar a comparação usando o Enum Role
    private static final String ROLE_ADMIN = Role.ADMIN.name();  // Usando Role enum
    private static final String ROLE_USER = Role.USER.name();  // Usando Role enum

	@PostMapping("") 
	public ResponseEntity<ClienteResponseDTO> adicionarCliente(@Valid @RequestBody ClienteCreateDTO clienteCreateDTO) { 
		  
		// Pegar o clienteCreateDTO e transformá-lo em uma entidade
		Cliente cliente = clienteMapper.toEntity(clienteCreateDTO);

		Endereco endereco = new Endereco();
		endereco.setCep(clienteCreateDTO.getCep());
		endereco.setEstado(clienteCreateDTO.getEstado());
		endereco.setCidade(clienteCreateDTO.getCidade());
		endereco.setBairro(clienteCreateDTO.getBairro());
		endereco.setRua(clienteCreateDTO.getRua());
		endereco.setNumero(clienteCreateDTO.getNumero());
		endereco.setComplemento(clienteCreateDTO.getComplemento());

		cliente.setEndereco(endereco);
		
		// Pegar o objeto/entidade e pedir para salvar no BD usando clienteService.
		Cliente clienteGravado = clienteService.save(cliente);

		// Pegar o objeto clienteGravado já com ID e devolver para o cliente informando
		// que foi gravado no Banco, mas primeiro transformando em DTO
		ClienteResponseDTO clienteResponseDTO = clienteMapper.toDTO(clienteGravado);

		// Agora que já está mapeado, retornamos "clienteResponseDTO" para o cliente.
		return ResponseEntity.status(HttpStatus.CREATED).body(clienteResponseDTO);

	}
	
	@GetMapping("/{clienteId}")
	public ResponseEntity<?> getClienteById(@PathVariable("clienteId") Long clienteId, HttpServletRequest request) {
		
		// Extrair o clienteId do token JWT
        Long clienteIdDoToken = (Long) request.getAttribute("clienteId");

        // Verificar se o usuário logado tem permissões
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.ADMIN.name()));  // Usando o Enum Role

        // Se for admin ou o clienteId da URL for o mesmo do clienteId no token
        if (isAdmin || clienteId.equals(clienteIdDoToken)) {
            Optional<Cliente> clienteById = clienteService.getClienteById(clienteId);
            if (!clienteById.isPresent()) {
                throw new ClienteNaoEncontradoException("Cliente não existe no banco.");
            }
            return ResponseEntity.ok(clienteById.get());
        }

        // Se não for admin e o clienteId não for do mesmo usuário, acesso negado
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");


	}
	
	@GetMapping("/cpf/{cpf}")
	public ResponseEntity<ClienteResponseDTO> buscarPorCpf(@PathVariable Long cpf) {
	    Cliente cliente = clienteService.buscarPorCpf(cpf);
	    if (cliente == null) return ResponseEntity.notFound().build();
	    return ResponseEntity.ok(clienteMapper.toDTO(cliente));
	}

	@PutMapping("/{clienteId}") 
	public ResponseEntity<ClienteResponseDTO> atualizar(@PathVariable("clienteId") Long clienteId,
			@Valid @RequestBody ClienteCreateDTO clienteCreateDTO, HttpServletRequest request) {
		
		
		 // Extrair o clienteId do token JWT
        Long clienteIdDoToken = (Long) request.getAttribute("clienteId");
        System.err.println("a");

        // Verificar se o usuário logado tem permissões
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.ADMIN.name()));  // Usando o Enum Role

        // Se for admin ou o clienteId da URL for o mesmo do clienteId no token
        if (isAdmin || clienteId.equals(clienteIdDoToken)) {
            Cliente cliente = clienteMapper.toEntity(clienteCreateDTO);

            Endereco endereco = new Endereco();
            endereco.setCep(clienteCreateDTO.getCep());
            endereco.setEstado(clienteCreateDTO.getEstado());
            endereco.setCidade(clienteCreateDTO.getCidade());
            endereco.setBairro(clienteCreateDTO.getBairro());
            endereco.setRua(clienteCreateDTO.getRua());
            endereco.setNumero(clienteCreateDTO.getNumero());
            endereco.setComplemento(clienteCreateDTO.getComplemento());

            cliente.setEndereco(endereco);

            Cliente clienteAtualizado = clienteService.update(clienteId, cliente);

            ClienteResponseDTO clienteResponseDTO = clienteMapper.toDTO(clienteAtualizado);

            return ResponseEntity.status(HttpStatus.OK).body(clienteResponseDTO);
        }

        // Se não for admin ou o clienteId não for do mesmo usuário, acesso negado
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);

	}
	
	@DeleteMapping("/{clienteId}") 
	public ResponseEntity<String> deletar(@PathVariable("clienteId") Long clienteId, HttpServletRequest request) {

		 // Extrair o clienteId do token JWT
        Long clienteIdDoToken = (Long) request.getAttribute("clienteId");

        // Verificar se o usuário logado tem permissões
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.ADMIN.name()));  // Usando o Enum Role

        // Se for admin ou o clienteId da URL for o mesmo do clienteId no token
        if (isAdmin || clienteId.equals(clienteIdDoToken)) {
            boolean clienteDeletado = clienteService.delete(clienteId);

            if (clienteDeletado) {
                return new ResponseEntity<String>("Cliente deletado com sucesso", HttpStatus.OK);
            } else {
                return new ResponseEntity<String>("Dados da conta são inválidos.", HttpStatus.NOT_ACCEPTABLE);
            }
        }

        // Se não for admin ou o clienteId não for do mesmo usuário, acesso negado
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");

	}

	@GetMapping("")
	public ResponseEntity<?> getClientes(HttpServletRequest request) {
		Long clienteIdDoToken = (Long) request.getAttribute("clienteId");

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		boolean isAdmin = auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_" + Role.ADMIN.name())); //Vê todos os clientes e User vê somente o dele.

		if (isAdmin) {
			List<Cliente> clientes = clienteService.getAll();
			return ResponseEntity.ok(clientes);
		} else if (clienteIdDoToken != null) {
			Optional<Cliente> cliente = clienteService.getClienteById(clienteIdDoToken);
			if (cliente.isPresent()) {
				return ResponseEntity.ok(List.of(cliente.get())); // Retorna uma lista com 1 cliente
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cliente não encontrado.");
			}
		}

		return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acesso negado");
	}	
}
