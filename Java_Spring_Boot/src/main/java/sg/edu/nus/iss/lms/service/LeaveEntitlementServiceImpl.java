package sg.edu.nus.iss.lms.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.edu.nus.iss.lms.model.Employee;
import sg.edu.nus.iss.lms.model.LeaveEntitlement;
import sg.edu.nus.iss.lms.repository.LeaveEntitlementRepository;

@Service
@Transactional(readOnly = true)
public class LeaveEntitlementServiceImpl implements LeaveEntitlementService {
	@Autowired
	LeaveEntitlementRepository leaveEntitlementRepo;
	
	@Override
	public List<LeaveEntitlement> findAllLeaveEntitlementByEmployee(Employee employee) {
		return leaveEntitlementRepo.findAllLeaveEntitlementByEmployeeId(employee.getId());
	};
	
	@Override
	public LeaveEntitlement findLeaveEntitlementByEmployeeAndType(Employee employee, String leaveTypeString) {
		return leaveEntitlementRepo.findLeaveEntitlementByEmployeeIdAndType(employee.getId(), leaveTypeString);
	};

	@Override
	@Transactional(readOnly = false)
	public LeaveEntitlement updateLeaveEntitlementBalanceByDays(Employee employee, String leaveTypeString, double days) {
		LeaveEntitlement leaveEnt = findLeaveEntitlementByEmployeeAndType(employee, leaveTypeString);
		leaveEnt.setLeaveBalance(leaveEnt.getLeaveBalance() + days);
		return leaveEntitlementRepo.saveAndFlush(leaveEnt);
	}
}
