package br.com.marcielli.bancom.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.marcielli.bancom.entity.Seguro;

@Repository
public interface SeguroRepository extends JpaRepository<Seguro, Long>{

	List<Seguro> findByAtivoTrue();
}
