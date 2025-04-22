package br.com.marcielli.BancoM.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.com.marcielli.BancoM.entity.Cliente;
import br.com.marcielli.BancoM.entity.User;


@Repository
public interface UserRepository extends JpaRepository<User, Integer>{
	
	  Optional<User> findByUsername(String username);
		 
	  List<User> findByFirstNameContainingIgnoreCase(String firstName);
	
}
