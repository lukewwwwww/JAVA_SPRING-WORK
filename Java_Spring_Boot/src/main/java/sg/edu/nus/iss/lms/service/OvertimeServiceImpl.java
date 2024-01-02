package sg.edu.nus.iss.lms.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.edu.nus.iss.lms.model.Employee;
import sg.edu.nus.iss.lms.model.OvertimeClaim;
import sg.edu.nus.iss.lms.model.OvertimeClaim.ClaimStatus;
import sg.edu.nus.iss.lms.repository.OvertimeRepository;

@Service
@Transactional(readOnly = true)
public class OvertimeServiceImpl implements OvertimeService {
	@Autowired
	OvertimeRepository overtimeRepo;
	
	@Autowired
	LeaveEntitlementService leaveEntService;
	
	// -- Employee --
	@Override
	public OvertimeClaim createOvertime(OvertimeClaim overtime) {
		return overtimeRepo.saveAndFlush(overtime);
	}
	
	@Override
	public OvertimeClaim updateOvertime(OvertimeClaim overtime) {
		if (overtime.getStatus() == ClaimStatus.APPROVED) {
			leaveEntService.updateLeaveEntitlementBalanceByDays(overtime.getEmployee(), "Compensation", overtime.getClaimableCompensation());
		}
		return overtimeRepo.saveAndFlush(overtime);
	}
	
	@Override
	public OvertimeClaim findEmployeeOvertimeId(Employee employee, Integer leaveId) {
		return overtimeRepo.findEmployeeOvertimeById(employee.getId(), leaveId);
	}

	@Override
	public List<OvertimeClaim> findAllEmployeeOvertimes(Employee employee) {
		return overtimeRepo.findAllOvertimesByEmployeeId(employee.getId());
	}
	
	@Override
	public List<OvertimeClaim> findActiveEmployeeOvertimes(Employee employee) {
		return overtimeRepo.findActiveOvertimesByEmployeeId(employee.getId());
	}
	
	@Override
	public List<OvertimeClaim> findCurrYearEmployeeOvertimes(Employee employee) {
		return overtimeRepo.findCurrYearOvertimesByEmployeeId(employee.getId());
	}
		
	@Override
	public Page<OvertimeClaim> getPaginatedOvertimes(int page, int pageSize, List<OvertimeClaim> listOvertimes) {
        Pageable pageRequest = PageRequest.of(page, pageSize);

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), listOvertimes.size());

        List<OvertimeClaim> pageContent = listOvertimes.subList(start, end);
        return new PageImpl<>(pageContent, pageRequest, listOvertimes.size());
    }
	
	// -- Manager --
	@Override
	public List<OvertimeClaim> findAllSubordinatePendingOvertimes(Employee manager) {
		return overtimeRepo.findAllSubordinatePendingOvertimes(manager.getId());
	}
	
	@Override
    public List<OvertimeClaim> findSubordinateOvertimeHistory(Employee manager, Integer subordinateId) {
		System.out.println(manager.getId());
		System.out.println(subordinateId);
		return overtimeRepo.findSubordinateOvertimeHistory(manager.getId(), subordinateId);
	}
	
	@Override
    public OvertimeClaim findSubordinateOvertimeById(Employee manager, Integer subordinateId, Integer overtimeId) {
		return overtimeRepo.findSubordinateOvertimeById(manager.getId(), subordinateId, overtimeId);
	}
	
}
