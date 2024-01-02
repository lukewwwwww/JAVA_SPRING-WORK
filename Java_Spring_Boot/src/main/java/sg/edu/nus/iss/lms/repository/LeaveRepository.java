package sg.edu.nus.iss.lms.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import sg.edu.nus.iss.lms.model.Leave;

public interface LeaveRepository extends JpaRepository<Leave, Integer> {
	// -- Employee --
	// Find Leave ID
	@Query("SELECT l FROM Employee e JOIN e.leaves l WHERE e.id=:employeeId AND l.id=:leaveId")
	public Leave findEmployeeLeaveById(@Param("employeeId") Integer employeeId, @Param("leaveId") Integer leaveId);

	// Find All Employee Leaves
	@Query("SELECT l FROM Employee e JOIN e.leaves l WHERE e.id=:employeeId ORDER BY l.startDate")
	public List<Leave> findAllLeaveByEmployeeId(@Param("employeeId") Integer employeeId);

	// Find Employee Non-Cancelled Non-Deleted Leaves in Current Year
	@Query("SELECT l FROM Employee e JOIN e.leaves l WHERE e.id=:employeeId AND YEAR(l.startDate)=YEAR(NOW()) AND l.status NOT IN ('CANCELLED', 'DELETED') ORDER BY l.startDate")
	public List<Leave> findCurrYearLeaveByEmployeeId(@Param("employeeId") Integer employeeId);

	// Find Employee Non-Cancelled Non-Deleted Upcoming Leaves
	@Query("SELECT l FROM Employee e JOIN e.leaves l WHERE e.id=:employeeId AND l.startDate >= NOW() AND l.status NOT IN ('CANCELLED', 'DELETED') ORDER BY l.startDate")
	public List<Leave> findUpcomingLeaveByEmployeeId(@Param("employeeId") Integer employeeId);

	// -- Manager --
	// Find all Subordinate's Leaves Pending Approve/Reject
	@Query("SELECT l from Employee e JOIN e.leaves l WHERE e.managerId=:managerId AND l.status IN ('APPLIED', 'UPDATED') ORDER BY l.startDate")
	public List<Leave> findAllSubordinatePendingLeaves(@Param("managerId") Integer managerId);

	// For Manager Approve View
	// Does not take into account DaySection
	@Query("SELECT l from Employee e Join e.leaves l WHERE e.managerId=:managerId AND ((l.startDate BETWEEN :startDate AND :endDate) OR (l.endDate BETWEEN :endDate AND :startDate)) OR (:startDate BETWEEN l.startDate AND l.endDate) ORDER BY l.startDate")
	public List<Leave> findOverlappingSubordinateLeaves(@Param("managerId") Integer managerId, 
			@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	// Find one Subordinate's Leave History (Applied/Updated/Approved)
	@Query("SELECT l from Employee e JOIN e.leaves l WHERE e.managerId=:managerId AND e.id=:employeeId AND l.status IN ('APPLIED', 'UPDATED', 'APPROVED') ORDER BY l.startDate")
	public List<Leave> findSubordinateLeaveHistory(@Param("managerId") Integer managerId,
			@Param("employeeId") Integer employeeId);

	// Find one Subordinate's Leave Details
	@Query("SELECT l from Employee e JOIN e.leaves l WHERE e.managerId=:managerId AND e.id=:employeeId AND l.id=:leaveId")
	public Leave findSubordinateLeaveById(@Param("managerId") Integer managerId,
			@Param("employeeId") Integer employeeId, @Param("leaveId") Integer leaveId);

}
