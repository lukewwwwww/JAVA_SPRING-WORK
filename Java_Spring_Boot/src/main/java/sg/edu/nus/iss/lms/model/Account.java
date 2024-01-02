package sg.edu.nus.iss.lms.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
	
	@Id
	@Size(min=3, max=20, message="Username must be 3-20 characters")
	private String username;
	
	@Size(min=6, message="Password must be at least 6 characters")	
	private String password;
	
	@OneToOne
	private Employee employee;
	
	@ManyToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	private List<Role> roles;
}