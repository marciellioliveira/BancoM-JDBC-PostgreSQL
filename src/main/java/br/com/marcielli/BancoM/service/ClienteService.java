package br.com.marcielli.BancoM.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Endereco;
import br.com.marcielli.BancoM.repository.ClienteRepository;

@Service
public class ClienteService {
	
	@Autowired
	private ClienteRepository clienteRepository;	
	
	@Transactional
	public Cliente save(Cliente cliente) {
		
		//Validar Nome
		
		//Validar CPF
		
		Cliente novoCliente = new Cliente();
		Endereco novoEndereco = new Endereco();
		
		
		novoEndereco.setCep(cliente.getEndereco().getCep());
		novoEndereco.setEstado(cliente.getEndereco().getEstado());
		novoEndereco.setCidade(cliente.getEndereco().getCidade());
		novoEndereco.setBairro(cliente.getEndereco().getBairro());
		novoEndereco.setRua(cliente.getEndereco().getRua());
		novoEndereco.setNumero(cliente.getEndereco().getNumero());
		novoEndereco.setComplemento(cliente.getEndereco().getComplemento());
				
		novoCliente.setNome(cliente.getNome());
		novoCliente.setCpf(cliente.getCpf());
		novoCliente.setEndereco(novoEndereco);
		//novoCliente.setContas(cliente.getContas());
		
		return clienteRepository.save(novoCliente);
	}
	
	
	public Cliente update(Cliente cliente, Long id) {
		
		Optional<Cliente> clienteH2 = clienteRepository.findById(id);
		
		Cliente clienteAtualizado = null;
		
		if(clienteH2.isPresent()) {
			
			clienteAtualizado = clienteH2.get();
			clienteAtualizado.setNome(cliente.getNome());
			clienteAtualizado.setCpf(cliente.getCpf());			
		}
		
		return clienteRepository.save(clienteAtualizado);		
		
	}
		
	public List<Cliente> getAll(){
		return clienteRepository.findAll();
	}
	
	public Optional<Cliente> getClienteById(Cliente cliente){	
		return clienteRepository.findById(cliente.getId());
	}
	
	public String delete(Long clienteId) {
		clienteRepository.deleteById(clienteId);
		return "DELETED";
	}
}
