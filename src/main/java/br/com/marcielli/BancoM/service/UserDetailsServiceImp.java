//package br.com.marcielli.BancoM.service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import br.com.marcielli.BancoM.entity.User;
//import br.com.marcielli.BancoM.enuns.Role;
//import br.com.marcielli.BancoM.repository.UserRepository;

//@Service
//public class UserDetailsServiceImp implements UserDetailsService {
//
//	private final UserRepository repository;
//
//	
//    public UserDetailsServiceImp(UserRepository repository) {
//        this.repository = repository;
//      
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        // Encontre o usuário no banco de dados
//        User user = repository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));
//        
//     //   String role =  user.getRole().name(); 
//        System.err.println();
//        System.err.println();
//        System.err.println("A senha ta criptografada aqui no userdetails? "+user.getPassword());
//        System.err.println();
//        System.err.println();
//      
//        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
//        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
//        
//        // Se for um admin, clienteId será null
//        Long clienteId = null;
//        if (user.getRole() != Role.ADMIN && user.getCliente() != null) {
//            clienteId = user.getCliente().getId(); // Só recupera clienteId se o usuário não for admin
//        }
//        
//        clienteId = 1L;
//      //  Long clienteId = (user.getCliente() != null) ? user.getCliente().getId() : null;
//        
//        System.err.println("Cliente ID: "+clienteId);
//        System.err.println("uuser.getUsername() : "+user.getUsername());
//        System.err.println("user.getPassword(): "+user.getPassword());
//        return new CustomUserDetails(user.getUsername(), user.getPassword(), clienteId, authorities);
//
//    }
//
//
//}