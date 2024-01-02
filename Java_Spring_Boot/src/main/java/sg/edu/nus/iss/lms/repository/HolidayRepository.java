package sg.edu.nus.iss.lms.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import sg.edu.nus.iss.lms.model.Holiday;

public interface HolidayRepository extends JpaRepository<Holiday, Integer>{
	@Query("SELECT h FROM Holiday h WHERE h.isActive=TRUE ORDER BY h.date")
	public List<Holiday> findAllActiveHolidays();
	
	@Query("SELECT h.observed FROM Holiday h WHERE h.isActive=TRUE ORDER BY h.date")
	public List<LocalDate> findAllActiveHolidayDates();
}
