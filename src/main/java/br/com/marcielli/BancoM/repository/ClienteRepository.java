package br.com.marcielli.BancoM.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.marcielli.BancoM.entity.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
	
	 Optional<Cliente> findByCpf(Long cpf);
	 
	 
	 List<Cliente> findByNomeContainingIgnoreCase(String nome);


}
