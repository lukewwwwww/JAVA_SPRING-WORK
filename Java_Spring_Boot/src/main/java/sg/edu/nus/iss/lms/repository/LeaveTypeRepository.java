package sg.edu.nus.iss.lms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import sg.edu.nus.iss.lms.model.LeaveType;

public interface LeaveTypeRepository extends JpaRepository<LeaveType, Integer> {
	@Query("Select lt.type from LeaveType lt")
	public List<String> findLeaveTypeNames();
	
	public LeaveType findByType(String type);

}
