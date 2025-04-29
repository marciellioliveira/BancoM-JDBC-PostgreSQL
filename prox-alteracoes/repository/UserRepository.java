package br.com.marcielli.bancom.repository;

import java.util.Optional;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import br.com.marcielli.bancom.entity.User;


//@Repository
public interface UserRepository { // extends JpaRepository<User, Integer>

	 // Optional<User> findByUsername(String username);
}
