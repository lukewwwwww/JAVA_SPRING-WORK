package sg.edu.nus.iss.lms.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class LeaveEntitlement {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	private LeaveType leaveType;
	
	private double entitlement;
	
	private double leaveBalance;
	
	@ManyToOne
	private Employee employee;
	
	// TestData Constructor
	public LeaveEntitlement(Employee employee, LeaveType leaveType, double entitlement) {
		this.employee = employee;
		this.leaveType = leaveType;
		this.entitlement = entitlement;
		this.leaveBalance = entitlement;
	}
}
