package br.com.marcielli.bancom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.marcielli.bancom.entity.Role;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {


	Role findByName(String name);
	
}
