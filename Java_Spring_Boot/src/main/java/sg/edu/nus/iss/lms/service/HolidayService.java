package sg.edu.nus.iss.lms.service;

import java.time.LocalDate;
import java.util.List;

import sg.edu.nus.iss.lms.model.Holiday;

public interface HolidayService {
	public List<Holiday> findAllActiveHolidays();
	public List<LocalDate> findAllActiveHolidayDates();
}
