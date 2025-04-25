package br.com.marcielli.BancoM.service;

import java.math.BigDecimal;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.marcielli.BancoM.dto.security.CartaoUpdateDTO;
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
import br.com.marcielli.BancoM.exception.ContaNaoEncontradaException;
import br.com.marcielli.BancoM.exception.PermissaoNegadaException;
import br.com.marcielli.BancoM.repository.ClienteRepository;
import br.com.marcielli.BancoM.repository.SeguroRepository;
import br.com.marcielli.BancoM.repository.UserRepository;

@Service
public class UserSeguroService {
	
	private final SeguroRepository seguroRepository;
	private final UserRepository userRepository;
	private final ClienteRepository clienteRepository;
	
	public UserSeguroService(SeguroRepository seguroRepository, UserRepository userRepository, ClienteRepository clienteRepository) {
		this.seguroRepository = seguroRepository;
		this.userRepository = userRepository;
		this.clienteRepository = clienteRepository;
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Seguro save(SeguroCreateDTO dto, JwtAuthenticationToken token) {
	   
	    User currentUser = ValidacaoUsuarioAtivo.validarUsuarioAdmin(userRepository, token);
	    ValidacaoUsuarioAtivo.verificarUsuarioAtivo(currentUser);

	    Cliente clienteAlvo;
	    
	    if (ValidacaoUsuarioAtivo.isAdmin(currentUser)) {
	      
	        if (dto.idUsuario() == null) {
	            throw new IllegalArgumentException("Admin deve informar o ID do cliente.");
	        }
	        
	        clienteAlvo = clienteRepository.findById(dto.idUsuario())
	                .orElseThrow(() -> new ContaNaoEncontradaException("Cliente não encontrado"));
	    } else {
	        // BASIC só pode criar seguros para si mesmo
	        Long currentUserId = Long.valueOf(currentUser.getId());
	        
	        if (dto.idUsuario() == null || !dto.idUsuario().equals(currentUserId)) {
	            throw new PermissaoNegadaException("Você só pode criar seguros para seu próprio usuário.");
	        }
	        
	        clienteAlvo = currentUser.getCliente();
	    }

	    Cartao cartao = clienteAlvo.getContas().stream()
	            .flatMap(conta -> conta.getCartoes().stream())
	            .filter(c -> c.getId().equals(dto.idCartao()))
	            .findFirst()
	            .orElseThrow(() -> new PermissaoNegadaException("Cartão não pertence ao cliente informado."));

	    if (!cartao.isStatus()) {
	        throw new IllegalStateException("Não é possível criar seguro para um cartão desativado.");
	    }

	    Seguro seguro = new Seguro();
	    seguro.setTipo(dto.tipoSeguro());
	    seguro.setAtivo(true);
	    seguro.setCartao(cartao);

	    if (dto.tipoSeguro() == TipoSeguro.SEGURO_VIAGEM && cartao.getCategoriaConta() == CategoriaConta.PREMIUM) {
	        seguro.setValorMensal(BigDecimal.ZERO);
	    } else {
	        seguro.setValorMensal(new BigDecimal("50.00"));
	    }

	    if (dto.tipoSeguro() == TipoSeguro.SEGURO_FRAUDE) {
	        seguro.setValorApolice(new BigDecimal("5000.00"));
	    }

	    return seguroRepository.save(seguro);
	}

//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public Seguro save(SeguroCreateDTO dto, JwtAuthenticationToken token) {
//		//Receber o usuário que está logado e criar a conta desse usuário.
//		Integer userId = null;
//		 BigDecimal valorMensal = BigDecimal.ZERO;
//	     BigDecimal valorApolice = BigDecimal.ZERO;
//	     Seguro seguro = new Seguro();
//	     
//		try {
//			userId = Integer.parseInt(token.getName());
//			
//			User user = userRepository.findById(userId)
//				    .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
//			
//			ValidacaoUsuarioAtivo.verificarUsuarioAtivo(user);	
//			
//			Cliente cliente = user.getCliente();
//			if (cliente == null) {
//			    throw new RuntimeException("Cliente não associado ao cartão");
//			}
//			
//			
//			Cartao cartaoDaConta = cliente.getContas().stream()
//				    .flatMap(conta -> conta.getCartoes().stream())
//				    .filter(c -> c.getId().equals(dto.idCartao()))
//				    .findFirst()
//				    .orElseThrow(() -> new RuntimeException("Cartão não pertence a este cliente"));
//			
//			
//			seguro.setTipo(dto.tipoSeguro());
//			seguro.setAtivo(true);
//			
//				
//			if(dto.tipoSeguro() == TipoSeguro.SEGURO_VIAGEM && cartaoDaConta.getCategoriaConta() == CategoriaConta.PREMIUM) {
//				 valorMensal = BigDecimal.ZERO;
//			} else {
//				 valorMensal = new BigDecimal("50.00");
//			}
//			
//			if(dto.tipoSeguro() == TipoSeguro.SEGURO_FRAUDE) {
//				valorApolice = new BigDecimal("5000.00");
//			}
//			
//			seguro.setCartao(cartaoDaConta);
//				
//			
//			
//			
//		} catch (NumberFormatException e) {			
//			System.out.println("ID inválido no token: " + token.getName());
//		}
//        seguroRepository.save(seguro);
//		
//		return seguro;
//	}
	
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Seguro getSegurosById(Long id) {
		return seguroRepository.findById(id).orElse(null);
	}
	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Seguro update(Long seguroId, SeguroUpdateDTO dto, JwtAuthenticationToken token) {
	   
	    User currentUser = ValidacaoUsuarioAtivo.validarUsuarioAdmin(userRepository, token);
	    ValidacaoUsuarioAtivo.verificarUsuarioAtivo(currentUser);

	    Seguro seguro = seguroRepository.findById(seguroId)
	            .orElseThrow(() -> new ContaNaoEncontradaException("Seguro não encontrado"));

	    if (!seguro.getCartao().isStatus()) {
	        throw new IllegalStateException("Não é possível atualizar um seguro de um cartão desativado.");
	    }

	    if (!seguro.getAtivo()) {
	        throw new IllegalStateException("Não é possível atualizar um seguro inativo.");
	    }

	    if (!seguro.getCartao().getConta().getCliente().getId().equals(dto.idUsuario())) {
	        throw new PermissaoNegadaException("Este seguro não pertence ao usuário informado");
	    }

	    if (ValidacaoUsuarioAtivo.isAdmin(currentUser)) {
	       
	        if (dto.idUsuario() == null) {
	            throw new IllegalArgumentException("Admin deve informar o ID do usuário");
	        }
	        
	        clienteRepository.findById(dto.idUsuario())
	                .orElseThrow(() -> new ContaNaoEncontradaException("Usuário não encontrado"));
	    } else {
	
	        Long currentUserId = currentUser.getId().longValue();
	        
	        if (!currentUserId.equals(dto.idUsuario())) {
	            throw new PermissaoNegadaException("Você só pode atualizar seus próprios seguros");
	        }

	        Long idDonoSeguro = seguro.getCartao().getConta().getCliente().getUser().getId().longValue();
	        if (!currentUserId.equals(idDonoSeguro)) {
	            throw new PermissaoNegadaException("Este seguro não pertence ao seu usuário");
	        }
	    }

	    seguro.setTipo(dto.tipo());
	    
	   

	    if (dto.tipo() == TipoSeguro.SEGURO_VIAGEM && 
	        seguro.getCartao().getCategoriaConta() == CategoriaConta.PREMIUM) {
	        seguro.setValorMensal(BigDecimal.ZERO);
	    } else if (seguro.getValorMensal().compareTo(BigDecimal.ZERO) == 0) {
	        seguro.setValorMensal(new BigDecimal("50.00"));
	    }

	    if (dto.tipo() == TipoSeguro.SEGURO_FRAUDE) {
	        seguro.setValorApolice(new BigDecimal("5000.00"));
	    }
	    System.err.println("\n----------------------\n"+seguro);
	    return seguroRepository.save(seguro);
	}
	
	
//	@Transactional(propagation = Propagation.REQUIRES_NEW)
//	public Seguro update(Long id, SeguroUpdateDTO dto) {
//		
//		Seguro seguroExiste = seguroRepository.findById(id).orElse(null);
//		
//		if (seguroExiste == null) {
//			 return null;
//		}
//		
//		seguroExiste.setTipo(dto.tipo());
//		seguroRepository.save(seguroExiste);
//		return seguroExiste;
//	
//	}
	
	
	@Transactional
	public boolean delete(Long id, CartaoUpdateDTO dto, JwtAuthenticationToken token) {
	    User currentUser = ValidacaoUsuarioAtivo.validarUsuarioAdmin(userRepository, token);
	    ValidacaoUsuarioAtivo.verificarUsuarioAtivo(currentUser);

	    Seguro seguro = seguroRepository.findById(id)
	            .orElseThrow(() -> new ContaNaoEncontradaException("Seguro não encontrado"));

	    if (ValidacaoUsuarioAtivo.isAdmin(currentUser)) {
	        if (seguro.getCartao().getConta().getCliente().getUser().getRoles().stream()
	                .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()))) {
	            throw new PermissaoNegadaException("Não é possível desativar seguros de administradores.");
	        }

	        if (!seguro.getCartao().getConta().getCliente().getId().equals(dto.idUsuario())) {
	            throw new PermissaoNegadaException("Este seguro não pertence ao usuário informado.");
	        }
	    } else {
	    	Long currentUserId = currentUser.getId().longValue();

	        if (!currentUserId.equals(dto.idUsuario())) {
	            throw new PermissaoNegadaException("Você só pode desativar seus próprios seguros.");
	        }

	        Long idDonoCartao = seguro.getCartao().getConta().getCliente().getUser().getId().longValue();
	        if (!currentUserId.equals(idDonoCartao)) {
	            throw new PermissaoNegadaException("Este seguro não pertence ao seu usuário.");
	        }
	    }

	    seguro.setAtivo(false);
	    seguroRepository.save(seguro);
	    return true;
	}
	
//	@Transactional
//	public boolean delete(Long id) {
//		
//		Seguro seguroExiste = seguroRepository.findById(id).orElse(null);
//		
//		boolean isAdmin = seguroExiste.getCartao().getConta().getCliente().getUser().getRoles().stream()
//			    .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
//	
//		if(isAdmin) {
//			throw new ClienteNaoEncontradoException("Não é possível deletar dados do administrador do sistema.");
//		}
//		
//		seguroExiste.setAtivo(false);
//		
//	    return true;
//	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}

