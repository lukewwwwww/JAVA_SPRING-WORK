package sg.edu.nus.iss.lms.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Leave {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	private Employee employee;

	@ManyToOne
	private LeaveType leaveType;
	
	@Transient
	private String leaveTypeString;
	
	@NotNull(message = "Start Date must not be empty.")
	@Future(message = "Start Date must be after today.")
	private LocalDate startDate;
	
	@Enumerated(EnumType.STRING)
	@NotNull(message = "Start Date AM/PM field must not be empty.")
	private DaySection startDaySection;
	
	@NotNull(message = "End Date must not be empty.")
	@Future(message = "End Date must be after today.")
	private LocalDate endDate;
	
	@Enumerated(EnumType.STRING)
	@NotNull(message = "End Date AM/PM field must not be empty.")
	private DaySection endDaySection;
	
	@NotBlank(message = "Destination must not be empty.")
	private String destination;
	
	@NotBlank(message = "Reason must not be empty.")
	private String reason;
	
	private String dissemination;
	
	private String contact;
	
	@Enumerated(EnumType.STRING)
	private LeaveStatus status;
	
	private String managerComment;
	
	public Leave(Employee employee, LeaveType leaveType,
			LocalDate startDate, DaySection startDaySection,
			LocalDate endDate, DaySection endDaySection,
			String destination, String reason, String dissemination, String contact,
			LeaveStatus status) {
		this.employee = employee;
		this.leaveType = leaveType;
		this.startDate = startDate;
		this.startDaySection = startDaySection;
		this.endDate = endDate;
		this.endDaySection = endDaySection;
		this.destination = destination;
		this.reason = reason;
		this.dissemination = dissemination;
		this.contact = contact;
		this.status = status;
	}

	public double getCalendarDuration() {
		double halfDay = 0.0;
		
		if (startDaySection == DaySection.AM && endDaySection == DaySection.PM) {
			halfDay = 1.0;
		} else if (startDaySection == DaySection.PM && endDaySection == DaySection.AM) {
			halfDay = 0.0;
		} else if (startDaySection == endDaySection) {
			halfDay = 0.5;
		}
		
		long fullDays = startDate.until(endDate, ChronoUnit.DAYS);
		return fullDays + halfDay;
	}
	
	public enum DaySection {
		AM, PM
	}
	
	public enum LeaveStatus {
		APPLIED, UPDATED, APPROVED, REJECTED, CANCELLED, DELETED
	}

	@Override
	public String toString() {
		return "Leave [id=" + id + ", employee=" + employee + ", leaveType=" + leaveType + ", leaveTypeString="
				+ leaveTypeString + ", startDate=" + startDate + ", startDaySection=" + startDaySection + ", endDate="
				+ endDate + ", endDaySection=" + endDaySection + ", destination=" + destination + ", reason=" + reason
				+ ", dissemination=" + dissemination + ", contact=" + contact + ", status=" + status
				+ ", managerComment=" + managerComment + "]";
	}
}
