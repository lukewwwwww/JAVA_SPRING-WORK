package sg.edu.nus.iss.lms.service;

import java.util.List;

import org.springframework.data.domain.Page;

import sg.edu.nus.iss.lms.model.Employee;
import sg.edu.nus.iss.lms.model.OvertimeClaim;

public interface OvertimeService {
	// -- Employee --
	public OvertimeClaim createOvertime(OvertimeClaim overtime);
	public OvertimeClaim updateOvertime(OvertimeClaim overtime);
	public OvertimeClaim findEmployeeOvertimeId(Employee employee, Integer overtimeId);
	public List<OvertimeClaim> findAllEmployeeOvertimes(Employee employee);
	public List<OvertimeClaim> findActiveEmployeeOvertimes(Employee employee);
	public List<OvertimeClaim> findCurrYearEmployeeOvertimes(Employee employee);
	public Page<OvertimeClaim> getPaginatedOvertimes(int page, int pageSize, List<OvertimeClaim> overtimeClaims);
	
	// -- Manager
	public List<OvertimeClaim> findAllSubordinatePendingOvertimes(Employee manager);
	public List<OvertimeClaim> findSubordinateOvertimeHistory(Employee manager, Integer subordinateId);
	public OvertimeClaim findSubordinateOvertimeById(Employee manger, Integer subordinateId, Integer overtimeId);
}
