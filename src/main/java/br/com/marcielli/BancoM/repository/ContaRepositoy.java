package br.com.marcielli.BancoM.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.marcielli.BancoM.entity.Conta;

@Repository
public interface ContaRepositoy extends JpaRepository<Conta, Long>{
	
	//@Query("SELECT u FROM Conta u WHERE u.chave_pix = ?1")
	 //List<Conta> findByPixAleatorio(String pixAleatorio);

}
