package br.com.marcielli.BancoM.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.marcielli.BancoM.entity.Seguro;

@Repository
public interface SeguroRepository extends JpaRepository<Seguro, Long>{

}
