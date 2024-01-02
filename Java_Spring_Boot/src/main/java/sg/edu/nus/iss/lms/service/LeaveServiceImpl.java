package sg.edu.nus.iss.lms.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.edu.nus.iss.lms.model.Employee;
import sg.edu.nus.iss.lms.model.Leave;
import sg.edu.nus.iss.lms.model.Leave.DaySection;
import sg.edu.nus.iss.lms.model.Leave.LeaveStatus;
import sg.edu.nus.iss.lms.repository.HolidayRepository;
import sg.edu.nus.iss.lms.repository.LeaveRepository;

@Service
@Transactional(readOnly = true)
public class LeaveServiceImpl implements LeaveService {
	@Autowired
	LeaveRepository leaveRepo;
	
	@Autowired
	HolidayRepository holidayRepo;
	
	@Autowired
	LeaveEntitlementService leaveEntService;
	
	// -- Employee --	
	@Override
	public Leave createLeave(Leave leave) {
		leaveEntService.updateLeaveEntitlementBalanceByDays(leave.getEmployee(), leave.getLeaveTypeString(), -calculateDeductibleDaysInLeave(leave));
		return leaveRepo.saveAndFlush(leave);
	}
	
	@Override
	public Leave updateLeave(Leave leave) {
		double originalDeduction = calculateDeductibleDaysInLeave(findEmployeeLeaveId(leave.getEmployee(), leave.getId()));
		double currentDeduction = calculateDeductibleDaysInLeave(leave);
		
		if (leave.getStatus() == LeaveStatus.CANCELLED || leave.getStatus() == LeaveStatus.DELETED || leave.getStatus() == LeaveStatus.REJECTED) {
			currentDeduction = 0;
		}
		
		leaveEntService.updateLeaveEntitlementBalanceByDays(leave.getEmployee(), leave.getLeaveType().getType(), originalDeduction - currentDeduction);
		return leaveRepo.saveAndFlush(leave);
	}
	
	@Override
	public List<Leave> findAllEmployeeLeaves(Employee employee) {
		return leaveRepo.findAllLeaveByEmployeeId(employee.getId());
	}

	@Override
	public List<Leave> findEmployeeLeavesCurrYear(Employee employee) {
		return leaveRepo.findCurrYearLeaveByEmployeeId(employee.getId());
	}
	
	@Override
	public List<Leave> findEmployeeLeavesUpcoming(Employee employee) {
		return leaveRepo.findUpcomingLeaveByEmployeeId(employee.getId());
	}
	
	@Override
	public Leave findEmployeeLeaveId(Employee employee, Integer leaveId) {
		return leaveRepo.findEmployeeLeaveById(employee.getId(), leaveId);
	}
	
    
    // -- Manager --
    public List<Leave> findAllSubordinatePendingLeaves(Employee manager) {
		return leaveRepo.findAllSubordinatePendingLeaves(manager.getId());
	}
	
    public List<Leave> findOverlappingSubordinateLeaves(Employee manager, Leave leave) {
		return leaveRepo.findOverlappingSubordinateLeaves(manager.getId(), leave.getStartDate(), leave.getEndDate());
	}
	
    public List<Leave> findSubordinateLeaveHistory(Employee manager, Integer subordinateId) {
		return leaveRepo.findSubordinateLeaveHistory(manager.getId(), subordinateId);
	}
	
    public Leave findSubordinateLeaveById(Employee manager, Integer subordinateId, Integer leaveId) {
		return leaveRepo.findSubordinateLeaveById(manager.getId(), subordinateId, leaveId);
	}
    
    // -- Utility --
    @Override
    public Page<Leave> getPaginatedLeaves(int page, int pageSize, List<Leave> listLeaves) {
        Pageable pageRequest = PageRequest.of(page, pageSize);

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), listLeaves.size());

        List<Leave> pageContent = listLeaves.subList(start, end);
        return new PageImpl<>(pageContent, pageRequest, listLeaves.size());
    }
    
	@Override
	public double calculateDeductibleDaysInLeave(Leave leave) {
		// If Leave Period > 14 days, weekends and public holidays are included
		if (leave.getCalendarDuration() > 14) {
			return leave.getCalendarDuration();
		}
		
		// Else, weekends and public holidays are excluded
		LocalDate startDate = leave.getStartDate();
		LocalDate endDate = leave.getEndDate();
		DaySection startDaySection = leave.getStartDaySection();
		DaySection endDaySection = leave.getEndDaySection();
		Set<DayOfWeek> weekend = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
		List<LocalDate> holidayDates = holidayRepo.findAllActiveHolidayDates();
		
		long fullDays = startDate.datesUntil(endDate)
		        .filter(d -> !weekend.contains(d.getDayOfWeek()) && !holidayDates.contains(d))
		        .count();
		
		// Assumes Leave startDate and endDate are working days (taken care of by LeaveValidator)
		double halfDay = 0.0;
		
		if (startDaySection == DaySection.AM && endDaySection == DaySection.PM) {
			halfDay = 1.0;
		} else if (startDaySection == DaySection.PM && endDaySection == DaySection.AM) {
			halfDay = 0.0;
		} else if (startDaySection == endDaySection) {
			halfDay = 0.5;
		}
		
		return fullDays + halfDay;
	}
}
