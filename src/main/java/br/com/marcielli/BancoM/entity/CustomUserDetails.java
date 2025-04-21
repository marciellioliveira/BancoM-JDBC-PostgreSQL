package br.com.marcielli.BancoM.entity;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;



public class CustomUserDetails implements UserDetails {
	
	private String username;
    private String password;
    private Long clienteId; // Esse é o campo extra
    private Collection<? extends GrantedAuthority> authorities;
	    
		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return authorities;
		}
		@Override
		public String getPassword() {
			 return password;
		}
		@Override
		public String getUsername() {
			 return username; 
		}
		
		public CustomUserDetails(String username, String password, Long clienteId, Collection<? extends GrantedAuthority> authorities) {
	        this.username = username;
	        this.password = password;
	        this.clienteId = clienteId;
	        this.authorities = authorities;
	        
	        
	    }

	    // Métodos do UserDetails

	    @Override
	    public boolean isAccountNonExpired() {
	        return true;
	    }

	    @Override
	    public boolean isAccountNonLocked() {
	        return true;
	    }

	    @Override
	    public boolean isCredentialsNonExpired() {
	        return true;
	    }

	    @Override
	    public boolean isEnabled() {
	        return true;
	    }

	    public Long getClienteId() {
	        return clienteId;  // Retorna o clienteId
	    }
	    
	    public void setUsername(String username) {
	        this.username = username;
	    }

	    public void setPassword(String password) {
	        this.password = password;
	    }

	    public void setClienteId(Long clienteId) {
	        this.clienteId = clienteId;
	    }

	    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
	        this.authorities = authorities;
	    }

	    
}
