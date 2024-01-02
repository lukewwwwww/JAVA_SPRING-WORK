package sg.edu.nus.iss.lms;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sg.edu.nus.iss.lms.model.Account;
import sg.edu.nus.iss.lms.model.Employee;
import sg.edu.nus.iss.lms.model.Holiday;
import sg.edu.nus.iss.lms.model.Leave;
import sg.edu.nus.iss.lms.model.Leave.DaySection;
import sg.edu.nus.iss.lms.model.Leave.LeaveStatus;
import sg.edu.nus.iss.lms.model.LeaveEntitlement;
import sg.edu.nus.iss.lms.model.LeaveType;
import sg.edu.nus.iss.lms.model.OvertimeClaim;
import sg.edu.nus.iss.lms.model.OvertimeClaim.ClaimStatus;
import sg.edu.nus.iss.lms.model.Role;
import sg.edu.nus.iss.lms.repository.AccountRepository;
import sg.edu.nus.iss.lms.repository.EmployeeRepository;
import sg.edu.nus.iss.lms.repository.HolidayRepository;
import sg.edu.nus.iss.lms.repository.LeaveEntitlementRepository;
import sg.edu.nus.iss.lms.repository.LeaveRepository;
import sg.edu.nus.iss.lms.repository.LeaveTypeRepository;
import sg.edu.nus.iss.lms.repository.OvertimeRepository;
import sg.edu.nus.iss.lms.repository.RoleRepository;

@SpringBootApplication
public class LeaveManagementSystemApplication {
	
	@Autowired
	HolidayRepository holidayRepo;
	
	public static void main(String[] args) throws IOException {
		SpringApplication.run(LeaveManagementSystemApplication.class, args);
	}
	
	// Reference: https://www.baeldung.com/java-read-json-from-url
	// Reference: https://stackoverflow.com/questions/71788850/how-to-parse-a-json-array-of-objects-using-jackson
	public static Holiday[] getHolidaysFromAPI() throws IOException {
		URL url = new URL("https://holidayapi.com/v1/holidays?pretty&country=SG&year=2022&key=2872decf-d74d-4f8f-997b-822b41f2908a");
		ObjectMapper mapper = new ObjectMapper();
	    ObjectNode node = mapper.readValue(url, ObjectNode.class);
	    return mapper.treeToValue(node.get("holidays"), Holiday[].class);
	}
	
    @Bean
    CommandLineRunner loadData(AccountRepository accRepo,
    						   EmployeeRepository empRepo,
    						   RoleRepository roleRepo,
    						   LeaveRepository leaveRepo,
    						   LeaveTypeRepository leaveTypeRepo,
    						   LeaveEntitlementRepository leaveEntitlementRepo,
    						   OvertimeRepository overtimeRepo) {
		return args -> {

			// Initialize LeaveTypes
			LeaveType annual = leaveTypeRepo.save(new LeaveType("Annual"));
			LeaveType medical = leaveTypeRepo.save(new LeaveType("Medical"));
			LeaveType compensation = leaveTypeRepo.save(new LeaveType("Compensation"));
			
			// Initialize Roles
			Role admin = roleRepo.save(new Role("admin", "Admin", "System Administrator"));
			Role staff = roleRepo.save(new Role("staff", "Staff", "Staff Member"));
			Role manager = roleRepo.save(new Role("manager", "Manager", "Manager"));
			
			List<Role> allRoles = new ArrayList<Role>() {{ add(admin); add(manager); add(staff); }};
			List<Role> managerRoles = new ArrayList<Role>() {{ add(manager); add(staff); }};
			List<Role> staffRoles = new ArrayList<Role>() {{ add(staff); }};
			
			// Create Employee Account with Entitlement & Leaves
			Employee brandonBoss = empRepo.save(new Employee("Lee Junhui Brandon","Boss", null));
			Employee brandonManager = empRepo.save(new Employee("BrandonManager","Manager", 1));
			Employee brandonStaff = empRepo.save(new Employee("BrandonStaff","AdminStaff", 2));
			Employee brandonStaff2 = empRepo.save(new Employee("BrandonStaff2","AdminStaff", 2));
			Employee brandonStaff3 = empRepo.save(new Employee("BrandonStaff3","AdminStaff", 2));
			Employee alexStaff = empRepo.save(new Employee("AlexStaff","ProfessionalStaff", null));
			
			accRepo.save(new Account("brandonStaff", "password1", brandonStaff, staffRoles));
			accRepo.save(new Account("brandonManager", "password1", brandonManager, managerRoles));
			leaveEntitlementRepo.save(new LeaveEntitlement(brandonStaff, annual, 18));
			leaveEntitlementRepo.save(new LeaveEntitlement(brandonStaff, medical, 60));
			leaveEntitlementRepo.save(new LeaveEntitlement(brandonStaff, compensation, 0));
			leaveRepo.save(new Leave(brandonStaff, annual, LocalDate.of(2023, 12, 25), DaySection.AM, LocalDate.of(2023, 12, 26), DaySection.PM, "Local", "Holiday", "person1", "contact1", LeaveStatus.APPROVED));
			leaveRepo.save(new Leave(brandonStaff, medical, LocalDate.of(2023, 12, 27), DaySection.AM, LocalDate.of(2023, 12, 27), DaySection.PM, "Overseas", "Sick", "person2", "contact2", LeaveStatus.APPLIED));
			leaveRepo.save(new Leave(brandonStaff, compensation, LocalDate.of(2023, 12, 28), DaySection.AM, LocalDate.of(2023, 12, 28), DaySection.AM, "Local", "Break", "person3", "contact3", LeaveStatus.REJECTED));
			leaveRepo.save(new Leave(brandonStaff, annual, LocalDate.of(2023, 12, 29), DaySection.AM, LocalDate.of(2023, 12, 29), DaySection.PM, "Overseas", "Holiday", "person4", "contact4", LeaveStatus.APPROVED));
			leaveRepo.save(new Leave(brandonStaff, annual, LocalDate.of(2023, 12, 30), DaySection.AM, LocalDate.of(2023, 12, 30), DaySection.AM, "Local", "Holiday", "updated1", "updated2", LeaveStatus.UPDATED));
			leaveRepo.save(new Leave(brandonStaff, annual, LocalDate.of(2023, 12, 30), DaySection.PM, LocalDate.of(2023, 12, 30), DaySection.PM, "Overseas", "Holiday", "cancelled1", "cancelled2", LeaveStatus.CANCELLED));
			leaveRepo.save(new Leave(brandonStaff, annual, LocalDate.of(2023, 12, 31), DaySection.AM, LocalDate.of(2023, 12, 31), DaySection.PM, "Local", "Holiday", "delete1", "delete2", LeaveStatus.DELETED));
			leaveRepo.save(new Leave(brandonStaff, annual, LocalDate.of(2024, 1, 30), DaySection.PM, LocalDate.of(2024, 1, 30), DaySection.PM, "Overseas", "Holiday", "2024-1", "2024-2", LeaveStatus.APPLIED));
			leaveRepo.save(new Leave(brandonStaff, annual, LocalDate.of(2024, 2, 10), DaySection.PM, LocalDate.of(2024, 2, 10), DaySection.PM, "Local", "Holiday", "2024-1", "2024-2", LeaveStatus.APPLIED));
			leaveRepo.save(new Leave(brandonStaff, medical, LocalDate.of(2023, 12, 27), DaySection.AM, LocalDate.of(2023, 12, 27), DaySection.PM, "Overseas", "Sick", "person2", "contact2", LeaveStatus.APPLIED));
			leaveRepo.save(new Leave(brandonStaff, medical, LocalDate.of(2023, 12, 27), DaySection.AM, LocalDate.of(2023, 12, 27), DaySection.PM, "Local", "Sick", "person2", "contact2", LeaveStatus.APPLIED));
			leaveRepo.save(new Leave(brandonStaff, medical, LocalDate.of(2023, 12, 27), DaySection.AM, LocalDate.of(2023, 12, 27), DaySection.PM, "Overseas", "Sick", "person2", "contact2", LeaveStatus.APPLIED));
			leaveRepo.save(new Leave(brandonStaff, medical, LocalDate.of(2023, 12, 27), DaySection.AM, LocalDate.of(2023, 12, 27), DaySection.PM, "Local", "Sick", "person2", "contact2", LeaveStatus.APPLIED));
			leaveRepo.save(new Leave(brandonStaff, medical, LocalDate.of(2023, 12, 27), DaySection.AM, LocalDate.of(2023, 12, 27), DaySection.PM, "Overseas", "Sick", "person2", "contact2", LeaveStatus.APPLIED));
			leaveRepo.save(new Leave(brandonStaff, medical, LocalDate.of(2023, 12, 27), DaySection.AM, LocalDate.of(2023, 12, 27), DaySection.PM, "Local", "Sick", "person2", "contact2", LeaveStatus.APPLIED));
			leaveRepo.save(new Leave(brandonStaff, medical, LocalDate.of(2023, 12, 27), DaySection.AM, LocalDate.of(2023, 12, 27), DaySection.PM, "Overseas", "Sick", "person2", "contact2", LeaveStatus.APPLIED));
			leaveRepo.save(new Leave(brandonStaff2, annual, LocalDate.of(2023, 12, 28), DaySection.AM, LocalDate.of(2023, 12, 28), DaySection.PM, "Local", "Sick", "person2", "contact2", LeaveStatus.APPLIED));
			leaveRepo.save(new Leave(brandonStaff3, compensation, LocalDate.of(2023, 12, 29), DaySection.AM, LocalDate.of(2023, 12, 29), DaySection.PM, "Overseas", "Sick", "person2", "contact2", LeaveStatus.UPDATED));
			leaveRepo.save(new Leave(brandonBoss, medical, LocalDate.of(2023, 12, 27), DaySection.AM, LocalDate.of(2023, 12, 27), DaySection.PM, "Local", "Sick", "boss1", "boss1", LeaveStatus.APPLIED));
			leaveRepo.save(new Leave(brandonBoss, medical, LocalDate.of(2023, 12, 27), DaySection.AM, LocalDate.of(2023, 12, 27), DaySection.PM, "Overseas", "Sick", "boss2", "boss2", LeaveStatus.UPDATED));
			leaveRepo.save(new Leave(brandonManager, annual, LocalDate.of(2023, 12, 27), DaySection.AM, LocalDate.of(2023, 12, 27), DaySection.PM, "Local", "Sick", "manager1", "manager1", LeaveStatus.APPLIED));
			leaveRepo.save(new Leave(brandonManager, medical, LocalDate.of(2023, 12, 28), DaySection.AM, LocalDate.of(2023, 12, 29), DaySection.AM, "Overseas", "Sick", "manager2", "manager2", LeaveStatus.UPDATED));
			leaveRepo.save(new Leave(brandonManager, compensation, LocalDate.of(2023, 12, 27), DaySection.AM, LocalDate.of(2023, 12, 27), DaySection.AM, "Local", "Sick", "manager3", "manager3", LeaveStatus.APPROVED));
			overtimeRepo.save(new OvertimeClaim(brandonStaff, LocalDateTime.of(LocalDate.of(2023, 2, 1), LocalTime.of(18, 0)), LocalDateTime.of(LocalDate.of(2023, 2, 1), LocalTime.of(22, 0)), "4hrs OT", ClaimStatus.APPLIED));
			overtimeRepo.save(new OvertimeClaim(brandonStaff, LocalDateTime.of(LocalDate.of(2023, 2, 2), LocalTime.of(18, 30)), LocalDateTime.of(LocalDate.of(2023, 2, 3), LocalTime.of(0, 0)), "5.5hrs OT", ClaimStatus.REJECTED));
			overtimeRepo.save(new OvertimeClaim(brandonStaff, LocalDateTime.of(LocalDate.of(2023, 2, 3), LocalTime.of(9, 0)), LocalDateTime.of(LocalDate.of(2023, 2, 3), LocalTime.of(18, 0)), "9hours OT", ClaimStatus.APPROVED));
			
			// Add Holiday Mock Data from 2022-2024
			/*
		    for (Holiday holiday : getHolidaysFromAPI()) {
		    	holidayRepo.saveAndFlush(holiday);
		    	
		    	Holiday holiday2023 = new Holiday(holiday.getName(), holiday.getDate().plusYears(1), holiday.getObserved().plusYears(1), holiday.isActive());
		    	holidayRepo.saveAndFlush(holiday2023);
		    	
		    	Holiday holiday2024 = new Holiday(holiday.getName(), holiday.getDate().plusYears(2), holiday.getObserved().plusYears(2), holiday.isActive());
		    	holidayRepo.saveAndFlush(holiday2024);
		    }*/
		};
	}
}
