package br.com.marcielli.BancoM.service;

import java.math.BigDecimal;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.com.marcielli.BancoM.dto.security.SeguroCreateDTO;
import br.com.marcielli.BancoM.dto.security.SeguroUpdateDTO;
import br.com.marcielli.BancoM.entity.Cartao;
import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.Seguro;
import br.com.marcielli.BancoM.entity.User;
import br.com.marcielli.BancoM.entity.ValidacaoUsuarioAtivo;
import br.com.marcielli.BancoM.enuns.CategoriaConta;
import br.com.marcielli.BancoM.enuns.TipoSeguro;
import br.com.marcielli.BancoM.exception.ClienteNaoEncontradoException;
import br.com.marcielli.BancoM.repository.SeguroRepository;
import br.com.marcielli.BancoM.repository.UserRepository;

@Service
public class UserSeguroService {
	
	private final SeguroRepository seguroRepository;
	private final UserRepository userRepository;
	
	public UserSeguroService(SeguroRepository seguroRepository, UserRepository userRepository) {
		this.seguroRepository = seguroRepository;
		this.userRepository = userRepository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Seguro save(SeguroCreateDTO dto, JwtAuthenticationToken token) {
		//Receber o usuário que está logado e criar a conta desse usuário.
		Integer userId = null;
		 BigDecimal valorMensal = BigDecimal.ZERO;
	     BigDecimal valorApolice = BigDecimal.ZERO;
	     Seguro seguro = new Seguro();
	     
		try {
			userId = Integer.parseInt(token.getName());
			
			User user = userRepository.findById(userId)
				    .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
			
			ValidacaoUsuarioAtivo.verificarUsuarioAtivo(user);	
			
			Cliente cliente = user.getCliente();
			if (cliente == null) {
			    throw new RuntimeException("Cliente não associado ao cartão");
			}
			
			
			Cartao cartaoDaConta = cliente.getContas().stream()
				    .flatMap(conta -> conta.getCartoes().stream())
				    .filter(c -> c.getId().equals(dto.idCartao()))
				    .findFirst()
				    .orElseThrow(() -> new RuntimeException("Cartão não pertence a este cliente"));
			
			
			seguro.setTipo(dto.tipoSeguro());
			seguro.setAtivo(true);
			
				
			if(dto.tipoSeguro() == TipoSeguro.SEGURO_VIAGEM && cartaoDaConta.getCategoriaConta() == CategoriaConta.PREMIUM) {
				 valorMensal = BigDecimal.ZERO;
			} else {
				 valorMensal = new BigDecimal("50.00");
			}
			
			if(dto.tipoSeguro() == TipoSeguro.SEGURO_FRAUDE) {
				valorApolice = new BigDecimal("5000.00");
			}
			
			seguro.setCartao(cartaoDaConta);
				
			
			
			
		} catch (NumberFormatException e) {			
			System.out.println("ID inválido no token: " + token.getName());
		}
        seguroRepository.save(seguro);
		
		return seguro;
	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Seguro getSegurosById(Long id) {
		return seguroRepository.findById(id).orElse(null);
	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Seguro update(Long id, SeguroUpdateDTO dto) {
		
		Seguro seguroExiste = seguroRepository.findById(id).orElse(null);
		
		if (seguroExiste == null) {
			 return null;
		}
		
		seguroExiste.setTipo(dto.tipo());
		seguroRepository.save(seguroExiste);
		return seguroExiste;
	
	}
	
	
	@Transactional
	public boolean delete(Long id) {
		
		Seguro seguroExiste = seguroRepository.findById(id).orElse(null);
		
		boolean isAdmin = seguroExiste.getCartao().getConta().getCliente().getUser().getRoles().stream()
			    .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
	
		if(isAdmin) {
			throw new ClienteNaoEncontradoException("Não é possível deletar dados do administrador do sistema.");
		}
		
		seguroExiste.setAtivo(false);
		
	    return true;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

