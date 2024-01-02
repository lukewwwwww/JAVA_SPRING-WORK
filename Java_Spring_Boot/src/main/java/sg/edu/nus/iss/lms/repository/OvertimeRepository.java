package sg.edu.nus.iss.lms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sg.edu.nus.iss.lms.model.OvertimeClaim;

public interface OvertimeRepository extends JpaRepository<OvertimeClaim, Integer> {
	// -- Staff --
	// Find OvertimeClaim ID
	@Query("SELECT o FROM Employee e JOIN e.overtimes o WHERE e.id=:employeeId AND o.id=:overtimeId")
	public OvertimeClaim findEmployeeOvertimeById(@Param("employeeId")Integer employeeId, @Param("overtimeId")Integer overtimeId);
		
	// Find All Employee OvertimeClaims
	@Query("SELECT o FROM Employee e JOIN e.overtimes o WHERE e.id=:employeeId ORDER BY o.startTime")
	public List<OvertimeClaim> findAllOvertimesByEmployeeId(@Param("employeeId")Integer employeeId);
	
	// Find All Employee OvertimeClaims Non-Deleted
	@Query("SELECT o FROM Employee e JOIN e.overtimes o WHERE e.id=:employeeId AND o.status NOT IN ('DELETED') ORDER BY o.startTime")
	public List<OvertimeClaim> findActiveOvertimesByEmployeeId(@Param("employeeId")Integer employeeId);

	// Find All Employee OvertimeClaims Non-Deleted in Current Year
	@Query("SELECT o FROM Employee e JOIN e.overtimes o WHERE e.id=:employeeId AND YEAR(o.startTime)=YEAR(NOW()) AND o.status NOT IN ('DELETED') ORDER BY o.startTime")
	public List<OvertimeClaim> findCurrYearOvertimesByEmployeeId(@Param("employeeId")Integer employeeId);

	// -- Manager --
	// Find all Subordinate's OvertimeClaims Pending Approve/Reject
	@Query("SELECT o from Employee e JOIN e.overtimes o WHERE e.managerId=:managerId AND o.status IN ('APPLIED', 'UPDATED') ORDER BY o.startTime")
	public List<OvertimeClaim> findAllSubordinatePendingOvertimes(@Param("managerId") Integer managerId);

	// Find one Subordinate's OvertimeClaim History (Applied/Updated/Approved/Rejected)
	@Query("SELECT o from Employee e JOIN e.overtimes o WHERE e.managerId=:managerId AND e.id=:employeeId AND o.status IN ('APPLIED', 'UPDATED', 'APPROVED', 'REJECTED') ORDER BY o.startTime")
	public List<OvertimeClaim> findSubordinateOvertimeHistory(@Param("managerId") Integer managerId, @Param("employeeId") Integer employeeId);
	
	// Find one Subordinate's OvertimeClaim Details
	@Query("SELECT o from Employee e JOIN e.overtimes o WHERE e.managerId=:managerId AND e.id=:employeeId AND o.id=:overtimeId")
	public OvertimeClaim findSubordinateOvertimeById(@Param("managerId") Integer managerId,
			@Param("employeeId") Integer employeeId, @Param("overtimeId") Integer overtimeId);
	
}
