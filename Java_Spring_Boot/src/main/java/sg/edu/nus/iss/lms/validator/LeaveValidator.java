package sg.edu.nus.iss.lms.validator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import sg.edu.nus.iss.lms.model.Leave;
import sg.edu.nus.iss.lms.model.Leave.DaySection;
import sg.edu.nus.iss.lms.service.HolidayService;
import sg.edu.nus.iss.lms.service.LeaveService;

@Component
public class LeaveValidator implements Validator {
	@Autowired
	private LeaveService leaveService;
	
	@Autowired
	private HolidayService holidayService;
	
	@Override
	public boolean supports(Class<?> clazz) {
		return Leave.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object obj, Errors errors) {
		Leave leave = (Leave) obj;
		LocalDate leaveStartDate = leave.getStartDate();
		LocalDate leaveEndDate = leave.getEndDate();		
		String leaveTypeString = leave.getLeaveTypeString();
		double leaveDeductibleDays = leaveService.calculateDeductibleDaysInLeave(leave);
		
		// Require Contact if Destination is Overseas
		if (leave.getDestination().equalsIgnoreCase("Overseas")) {
			ValidationUtils.rejectIfEmptyOrWhitespace(errors, "contact",
					"error.contact", "Contact details are required for overseas leave.");
		}
		
		// Require End Date after Start Date
		if (leaveEndDate.isBefore(leaveStartDate)) {
			errors.rejectValue("endDate", "error.endDate", "End Date must be after Start Date.");
		} else if (leaveEndDate.equals(leaveStartDate)) {
			if (leave.getStartDaySection() == DaySection.PM && leave.getEndDaySection() == DaySection.AM) {
				errors.rejectValue("endDate", "error.endDate", "End Date must be after Start Date.");
			}
		}
		
		// Require Start Date && End Date on Working Day
		if(!isWorkingDay(leaveStartDate)) {
			errors.rejectValue("startDate","error.startDate","Start Date must be on a working day.");
		}
		
		if(!isWorkingDay(leaveEndDate)) {
			errors.rejectValue("endDate","error.endDate","End Date must be on a working day.");
		}
		
		// Require Duration to be Full Day for All Entitlement except Compensation
		if (!leaveTypeString.equals("Compensation")) {
			if (leaveDeductibleDays < 1) {
				// Case: Same Day, Only one DaySection (AM-AM, PM-PM)
				errors.rejectValue("startDate","error.startDate","Only full-day leaves can be taken for " + leaveTypeString + " leave.");
			} else {
				// Case: Leave spans >1 day, but not full day (AM-PM) leave.
				if (leave.getStartDaySection() == DaySection.PM) {
					errors.rejectValue("startDate","error.startDate","Only full-day leaves can be taken for " + leaveTypeString + " leave.");
				}
				if (leave.getEndDaySection() == DaySection.AM) {
					errors.rejectValue("endDate","error.endDate","Only full-day leaves can be taken for " + leaveTypeString + " leave.");
				}
			}
		}
		
		// Note: Validation for Sufficient Leave Balance is done in Controller (Apply, Edit)
	}
		
	public boolean isWorkingDay(LocalDate date) {
		return !isWeekend(date) && !isHoliday(date);
	}
	
	public boolean isWeekend(LocalDate date) {
		DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
	}
	
	public boolean isHoliday(LocalDate date) {
		List<LocalDate> publicHolidays = holidayService.findAllActiveHolidayDates();
		return publicHolidays.contains(date);
	}
	
}
