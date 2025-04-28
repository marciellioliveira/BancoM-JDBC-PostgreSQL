package br.com.marcielli.bancom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.marcielli.bancom.entity.Conta;

@Repository
public interface ContaRepository extends JpaRepository<Conta, Long>{
	

}
