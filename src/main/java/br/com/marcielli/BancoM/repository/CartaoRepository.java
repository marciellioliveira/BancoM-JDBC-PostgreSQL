package br.com.marcielli.BancoM.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.marcielli.BancoM.entity.Cartao;

@Repository
public interface CartaoRepository  extends JpaRepository<Cartao, Long> {

}
