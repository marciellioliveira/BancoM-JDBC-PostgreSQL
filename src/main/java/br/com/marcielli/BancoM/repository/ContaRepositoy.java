package br.com.marcielli.BancoM.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.marcielli.BancoM.entity.Conta;
import br.com.marcielli.BancoM.entity.ContaPoupanca;

@Repository
public interface ContaRepositoy extends JpaRepository<Conta, Long>{

}
