package sg.edu.nus.iss.lms.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import sg.edu.nus.iss.lms.model.Employee;
import sg.edu.nus.iss.lms.model.Leave;
import sg.edu.nus.iss.lms.model.Leave.LeaveStatus;
import sg.edu.nus.iss.lms.service.EmployeeService;
import sg.edu.nus.iss.lms.service.LeaveService;
import sg.edu.nus.iss.lms.service.OvertimeService;

@Controller
@RequestMapping(value = "/manager")
public class ManagerLeaveController {
	@Autowired
	EmployeeService employeeService;
	
	@Autowired
	LeaveService leaveService;
	
	@Autowired
	OvertimeService overtimeService;
	
	@GetMapping(value = {"", "/", "/overview"})
	public String managerHome(Model model, HttpSession sessionObj) {
		model.addAttribute("leaveService", leaveService);
		model.addAttribute("leavePending",
				leaveService.findAllSubordinatePendingLeaves((Employee) sessionObj.getAttribute("employee")));
		model.addAttribute("overtimePending",
				overtimeService.findAllSubordinatePendingOvertimes((Employee) sessionObj.getAttribute("employee")));
		return "manager-overview";
	}
	
	@GetMapping(value = "/leave/pending")
	public String pendingLeaves(Model model, HttpSession sessionObj) {
		List<Leave> leavePending = leaveService.findAllSubordinatePendingLeaves((Employee) sessionObj.getAttribute("employee"));
		Map<Integer, List<Leave>> leavePendingByEmployee = new HashMap<>();
		
		for (Leave leave : leavePending) {
			Integer employeeId = leave.getEmployee().getId();
			
			if(!leavePendingByEmployee.containsKey(employeeId)) {
				leavePendingByEmployee.put(employeeId, new ArrayList<>());
			}
			leavePendingByEmployee.get(employeeId).add(leave);
		}
		
		model.addAttribute("leaveService", leaveService);
		model.addAttribute("leavePending", leavePendingByEmployee);
		return "manager-leave-pending";
	}
	
	@GetMapping(value = "/staff")
	public String subordinateList(Model model, HttpSession sessionObj) {
		model.addAttribute("subordinateList",
				employeeService.findAllSubordinates((Employee) sessionObj.getAttribute("employee")));
		return "manager-subordinate-list";
	}
	
	@GetMapping(value = "/staff/{sid}/leave")
	public String subordinateLeaveHistory(@PathVariable(name = "sid") Integer subordinateId, Model model, HttpSession sessionObj, HttpServletRequest request,
			@RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
		int currPage = page.orElse(1);
		int pageSize = size.orElse(10);
		
		List<Leave> leaveHistory = leaveService.findSubordinateLeaveHistory((Employee) sessionObj.getAttribute("employee"), subordinateId);
		
		// Clamp PageNumber between 1 to MaxPage
		int maxPage = (int) Math.ceil(leaveHistory.size() / (double) pageSize);
		int getPageNum = Math.max(1, Math.min(maxPage, currPage));
		getPageNum = getPageNum - 1; // Convert 1-Index to 0-Index

		Page<Leave> leaveHistoryPage = leaveService.getPaginatedLeaves(getPageNum, pageSize, leaveHistory);
		List<Leave> leaveHistoryPaged = leaveHistoryPage.getContent();

		model.addAttribute("leaveService", leaveService);
		model.addAttribute("subordinateName", employeeService.findEmployeeById(subordinateId).getName());
		model.addAttribute("currUrl", request.getRequestURI().toString());
		model.addAttribute("currPage", currPage);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("totalPages", leaveHistoryPage.getTotalPages());
		model.addAttribute("totalItems", leaveHistoryPage.getTotalElements());
		model.addAttribute("leaveHistory", leaveHistoryPaged);
		return "subordinate-leave-history";
	}

	@GetMapping(value = "/staff/{sid}/leave/{id}")
	public String subordinateLeaveDetails(@PathVariable(name = "sid") Integer subordinateId,
										  @PathVariable(name = "id") Integer leaveId,
										  Model model, HttpSession sessionObj) {
		Leave leave = leaveService.findSubordinateLeaveById((Employee) sessionObj.getAttribute("employee"), subordinateId, leaveId);
		List<Leave> overlappingLeaves = leaveService.findOverlappingSubordinateLeaves((Employee) sessionObj.getAttribute("employee"), leave);
		
		model.addAttribute("leave", leave);
		model.addAttribute("overlappingLeaves", overlappingLeaves);
		return "subordinate-leave-details";
	}
	
	@PostMapping(value = "/staff/{sid}/leave/{id}/approve")
	public String approveLeave(@PathVariable(name = "sid") Integer subordinateId,
							   @PathVariable(name = "id") Integer leaveId,
							   @Valid @ModelAttribute("leave") Leave leaveForm, BindingResult bindingResult,
							   Model model, HttpSession sessionObj) {
		Leave leave = leaveService.findSubordinateLeaveById((Employee) sessionObj.getAttribute("employee"), subordinateId, leaveId);
		leave.setManagerComment(leaveForm.getManagerComment());
		leave.setStatus(LeaveStatus.APPROVED);
		leaveService.updateLeave(leave);
		return "redirect:/manager/overview";
	}
	
	@PostMapping(value = "/staff/{sid}/leave/{id}/reject")
	public String rejectLeave(@PathVariable(name = "sid") Integer subordinateId,
							  @PathVariable(name = "id") Integer leaveId,
							  @Valid @ModelAttribute("leave") Leave leaveForm, BindingResult bindingResult,
							  Model model, HttpSession sessionObj) {
		Leave leave = leaveService.findSubordinateLeaveById((Employee) sessionObj.getAttribute("employee"), subordinateId, leaveId);
		leave.setManagerComment(leaveForm.getManagerComment());
		leave.setStatus(LeaveStatus.REJECTED);
		leaveService.updateLeave(leave);
		return "redirect:/manager/overview";
	}
}
