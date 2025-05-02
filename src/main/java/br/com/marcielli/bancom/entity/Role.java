package br.com.marcielli.bancom.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.context.annotation.Profile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Role {

	private Long id;
	private String name;
	public enum Values {
		
		ADMIN(1L),
		BASIC(2L);
		
		private final Long roleId;
		
	    private Values(Long roleId) {
	        this.roleId = roleId;
	    }

	    public long getRoleId() {
	        return roleId;
	    }
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
