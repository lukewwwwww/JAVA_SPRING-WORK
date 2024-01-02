package sg.edu.nus.iss.lms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sg.edu.nus.iss.lms.model.LeaveEntitlement;

public interface LeaveEntitlementRepository extends JpaRepository<LeaveEntitlement, Integer> {

	@Query("SELECT le FROM Employee e JOIN e.leaveEntitlements le WHERE e.id=:employeeId")
	public List<LeaveEntitlement> findAllLeaveEntitlementByEmployeeId(@Param("employeeId")Integer employeeId);
	
	@Query("SELECT le FROM Employee e JOIN e.leaveEntitlements le JOIN le.leaveType lt WHERE e.id=:employeeId AND lt.type=:leaveType")
	public LeaveEntitlement findLeaveEntitlementByEmployeeIdAndType(@Param("employeeId")Integer employeeId, @Param("leaveType")String leaveTypeString);
}
