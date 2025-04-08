package br.com.marcielli.BancoM.service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Endereco;
import br.com.marcielli.BancoM.exception.ClienteCpfInvalidoException;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.exception.ClienteNomeInvalidoException;
import br.com.marcielli.BancoM.repository.ClienteRepository;

@Service
public class ClienteService {
	
	@Autowired
	private ClienteRepository clienteRepository;	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cliente save(Cliente cliente) {
		
		//Validar Nome		
		validarNome(cliente.getNome());
		
		if(cliente.getCpf() != null) {
			String novoCpf = ""+cliente.getCpf();
			validarCpf(novoCpf);
		}		
		
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
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Cliente update(Cliente cliente, Long id) {
		
		//Validar Nome		
		validarNome(cliente.getNome());
		
		if(cliente.getCpf() != null) {
			String novoCpf = ""+cliente.getCpf();
			validarCpf(novoCpf);
		}		
		
		Optional<Cliente> clienteH2 = clienteRepository.findById(id);
		
		Cliente clienteAtualizado = null;
		
		if(clienteH2.isPresent()) {
			System.err.println("Teste? "+cliente.getEndereco());
			clienteAtualizado = clienteH2.get();
			clienteAtualizado.setNome(cliente.getNome());
			clienteAtualizado.setCpf(cliente.getCpf());		
			
			clienteAtualizado.getEndereco().setCidade(cliente.getEndereco().getCidade());
			clienteAtualizado.getEndereco().setBairro(cliente.getEndereco().getBairro());
			clienteAtualizado.getEndereco().setCep(cliente.getEndereco().getCep());
			clienteAtualizado.getEndereco().setComplemento(cliente.getEndereco().getComplemento());
			clienteAtualizado.getEndereco().setEstado(cliente.getEndereco().getEstado());
			clienteAtualizado.getEndereco().setNumero(cliente.getEndereco().getNumero());
			clienteAtualizado.getEndereco().setRua(cliente.getEndereco().getRua());
			
			
			return clienteRepository.save(clienteAtualizado);	
		} else { 
			throw new ClienteNaoEncontradoException("O cliente não pode ser atualizado porque não existe no banco.");
		}	
		
	}
		
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<Cliente> getAll(){	
		
		List<Cliente> clientes = clienteRepository.findAll();
		
		if(clientes.size() <= 0) {
			throw new ClienteNaoEncontradoException("Não existem clientes cadastrados no banco.");
		}
		
		return clientes;
		
		//return clienteRepository.findAll();
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Optional<Cliente> getClienteById(Long id){	
		
		Optional<Cliente> clienteH2 = clienteRepository.findById(id);
		
		if(!clienteH2.isPresent()) {
			throw new ClienteNaoEncontradoException("Cliente não encontrado.");
		}
		
		return clienteH2;
		//return clienteRepository.findById(cliente.getId());
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean delete(Long clienteId) {
		
		Optional<Cliente> clienteH2 = clienteRepository.findById(clienteId);
		
		if(clienteH2.isPresent()) {
			clienteRepository.deleteById(clienteId);	
			return true;
		} else {
			throw new ClienteNaoEncontradoException("O cliente não pode ser deletado porque não existe no banco.");
		}
	}
	
	
	//Outros métodos
	public boolean validarNome(String nome) {
		
		for(int i=0; i<nome.length(); i++) {			
			char letra = nome.charAt(i);
			Boolean flag = Character.isDigit(letra);
			
			if(flag) {
				throw new ClienteNomeInvalidoException("O nome '"+nome+"' digitado possui número. O nome do cliente deve conter apenas letras.");
			}
		}			
		
		if(nome.contains(",") || nome.contains(".") || nome.contains("!") || nome.contains("\\") || nome.contains("\"") || nome.contains("/") || nome.contains("#") || nome.contains("$") || nome.contains("%") || nome.contains("&") || nome.contains("*") || nome.contains(":") || nome.contains(";") || nome.contains("+") || nome.contains("<") || nome.contains(">") || nome.contains("=") || nome.contains("?") || nome.contains("@") || nome.contains("[") || nome.contains("]") || nome.contains("_") || nome.contains("{") || nome.contains("}") || nome.contains("|")) {
			throw new ClienteNomeInvalidoException("O nome '"+nome+"' digitado possui caracteres especiais. O nome do cliente deve conter apenas letras.");
		}	
		
		String tam = "";
		
		if(nome.length() < 2 || nome.length()> 100) {
			
			if(nome.length() < 2) {
				tam = "MAIOR";
			}
			
			if(nome.length() > 2) {
				tam = "MENOR";
			}
			throw new ClienteNomeInvalidoException("O nome '"+nome+"' digitado é inválido. O nome do cliente deve conter entre 2 e 100 caracteres.");
		}	
		
		return true;
		
	}	
	
	public boolean validarCpf(String cpf)  {
		
		if(!cpf.contains(".") && cpf.contains("-")) {
			if(cpf.length() < 11 || cpf.length() > 11){
				throw new ClienteCpfInvalidoException("O cpf '"+cpf+"' digitado é inválido porque contem "+cpf.length()+" caracteres. O cpf do cliente deve conter 11 caracteres sem traços e pontos. ");
			} 
		}
		
		if(cpf.length() < 11 || cpf.length() > 11){
			throw new ClienteCpfInvalidoException("O cpf '"+cpf+"' digitado é inválido porque contem "+cpf.length()+" caracteres. O cpf do cliente deve conter 11 caracteres sem traços e pontos. ");
		} 

		//Validar ultimos numeros do cpf
		int valor = 0;
		int j = 10;


		for(int i=0; i<cpf.length(); i++) { 
		
			while(j>1) { 
				
				char letra = cpf.charAt(i);
				int caracter = letra - '0';
				
				valor += caracter * j; 			
				j--;
				break;
				
			}		
		}
				
		int resultado = (valor * 10) % 11;
			
		if(resultado == 10) {
			resultado = 0;
		} else {
			
			char penultimoDig = cpf.charAt(9);			
			int penultimoDigito = penultimoDig - '0';
			
			
			if(resultado == penultimoDigito) {			
			
				int b = 11;
			
				int valor2 = 0;
				
				for(int a=0; a<10; a++) { 
					while(b>1) { 
						char letra2 = cpf.charAt(a); 
						int caracter2 = letra2 - '0';
						
						valor2 += caracter2 * b; 
						
						b--;
						
						break;
					}
				}
			
			
			int resultado2 = (valor2 * 10) % 11;
			
			char ultimoDig = cpf.charAt(10);
			int ultimoDigito = ultimoDig - '0';		
			
			if(!(resultado2 == ultimoDigito)) {
				throw new ClienteCpfInvalidoException("O cpf '"+cpf+"' digitado é inválido. Por favor, digite um cpf válido.");
			}	
			
			} 		
		}
		
		
		//Validar cpf com numeros iguais
		int numOcorrencias = 1;		
		HashMap<Character, Integer> findDuplicated = new HashMap<Character, Integer>();
		String novoCpf = "";
		
		//Usar o hashmap para contar os caracteres duplicados da string cpf
		
		char[] meuCpf = cpf.toCharArray();
		
		for(int i=0; i<meuCpf.length; i++) {
			if(!findDuplicated.containsKey(meuCpf[i])) {
				findDuplicated.put(meuCpf[i], 1);
				novoCpf += meuCpf[i];
				
			} else {
				findDuplicated.put(meuCpf[i], 1);
				numOcorrencias++; 
			}
		}
		
		if(numOcorrencias >= 11) {
			throw new ClienteCpfInvalidoException("O cpf '"+cpf+"' digitado é inválido. Por favor, digite um cpf válido.");
		}
		return true;
		
	}
	
	public boolean validarEndereco(Endereco endereco) {
		
		
		
		return true;
	}
	
	
	
	
	
	
	
	
}
