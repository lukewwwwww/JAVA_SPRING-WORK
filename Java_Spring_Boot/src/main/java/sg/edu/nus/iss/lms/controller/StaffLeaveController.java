package sg.edu.nus.iss.lms.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
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
import sg.edu.nus.iss.lms.model.LeaveEntitlement;
import sg.edu.nus.iss.lms.model.LeaveType;
import sg.edu.nus.iss.lms.service.LeaveEntitlementService;
import sg.edu.nus.iss.lms.service.LeaveService;
import sg.edu.nus.iss.lms.service.LeaveTypeService;
import sg.edu.nus.iss.lms.service.OvertimeService;
import sg.edu.nus.iss.lms.validator.LeaveValidator;

@Controller
@RequestMapping(value = "/staff")
public class StaffLeaveController {

	@Autowired
	LeaveService leaveService;

	@Autowired
	LeaveTypeService leaveTypeService;

	@Autowired
	LeaveEntitlementService leaveEntitlementService;

	@Autowired
	OvertimeService overtimeService;
	
	@Autowired
	LeaveValidator leaveValidator;
	
	@InitBinder("leave")
	private void initLeaveBinder(WebDataBinder binder) {
		binder.addValidators(leaveValidator);
	}

	@GetMapping(value = {"", "/", "/overview"})
	public String staffHome(Model model, HttpSession sessionObj) {
		model.addAttribute("leaveService", leaveService);
		model.addAttribute("leaveEntitlement", leaveEntitlementService
				.findAllLeaveEntitlementByEmployee((Employee) sessionObj.getAttribute("employee")));
		model.addAttribute("leaveUpcoming",
				leaveService.findEmployeeLeavesUpcoming((Employee) sessionObj.getAttribute("employee")));
		model.addAttribute("overtimeClaims",
				overtimeService.findActiveEmployeeOvertimes((Employee) sessionObj.getAttribute("employee")));
		return "leave-overview";
	}

	// CREATE
	@GetMapping(value = "/leave/apply")
	public String staffLeaveApplicationForm(Model model, HttpSession sessionObj) {
		model.addAttribute("leave", new Leave());
		model.addAttribute("leaveTypes", leaveTypeService.getTypeNames());
		model.addAttribute("leaveEntitlement", leaveEntitlementService
				.findAllLeaveEntitlementByEmployee((Employee) sessionObj.getAttribute("employee")));
		return "leave-apply";
	}

	@PostMapping(value = "/leave/apply")
	public String staffApplyLeave(@Valid @ModelAttribute("leave") Leave leaveForm, BindingResult bindingResult,
			Model model, HttpSession sessionObj) {

		if (bindingResult.hasErrors()) {
			model.addAttribute("leaveTypes", leaveTypeService.getTypeNames());
			model.addAttribute("leaveEntitlement", leaveEntitlementService
					.findAllLeaveEntitlementByEmployee((Employee) sessionObj.getAttribute("employee")));
			return "leave-apply";
		}
		
		// Validate Sufficient Leave Balance
		LeaveEntitlement currLeaveEntitlement =
				leaveEntitlementService.findLeaveEntitlementByEmployeeAndType((Employee) sessionObj.getAttribute("employee"), leaveForm.getLeaveTypeString());
		double leaveBalance = currLeaveEntitlement.getLeaveBalance();
		double leaveDuration = leaveService.calculateDeductibleDaysInLeave(leaveForm);
		
		if (leaveBalance < leaveDuration) {
			model.addAttribute("leaveTypes", leaveTypeService.getTypeNames());
			model.addAttribute("leaveEntitlement", leaveEntitlementService
					.findAllLeaveEntitlementByEmployee((Employee) sessionObj.getAttribute("employee")));
			model.addAttribute("errorMsg", "You do not have sufficient leave balance.");
			return "leave-apply";
		}
		
		// Create Leave 
		LeaveType currLeaveType = leaveTypeService.findByType(leaveForm.getLeaveTypeString());
		leaveForm.setLeaveType(currLeaveType);
		leaveForm.setEmployee((Employee) sessionObj.getAttribute("employee"));
		leaveForm.setStatus(LeaveStatus.APPLIED);
		leaveService.createLeave(leaveForm);

		return "redirect:/staff/overview";
	}

	// RETRIEVE LIST
	// SHOW CURR YEAR, NOT 'LeaveStatus.DELETED' OR 'LeaveStatus.CANCELLED'
	@GetMapping(value = "/leave/history")
	public String staffLeaveHistory(Model model, HttpSession sessionObj, HttpServletRequest request,
			@RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
		int currPage = page.orElse(1);
		int pageSize = size.orElse(10);

		List<Leave> leaveHistory = leaveService
				.findEmployeeLeavesCurrYear((Employee) sessionObj.getAttribute("employee"));

		// Clamp PageNumber between 1 to MaxPage
		int maxPage = (int) Math.ceil(leaveHistory.size() / (double) pageSize);
		int getPageNum = Math.max(1, Math.min(maxPage, currPage));
		getPageNum = getPageNum - 1; // Convert 1-Index to 0-Index

		Page<Leave> leaveHistoryPage = leaveService.getPaginatedLeaves(getPageNum, pageSize, leaveHistory);
		List<Leave> leaveHistoryPaged = leaveHistoryPage.getContent();

		model.addAttribute("leaveService", leaveService);
		model.addAttribute("currUrl", request.getRequestURI().toString());
		model.addAttribute("currPage", currPage);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("totalPages", leaveHistoryPage.getTotalPages());
		model.addAttribute("totalItems", leaveHistoryPage.getTotalElements());
		model.addAttribute("leaveHistory", leaveHistoryPaged);
		model.addAttribute("showAll", false);
		return "leave-history";
	}

	// SHOW ALL
	@GetMapping(value = "/leave/history/all")
	public String staffLeaveHistoryAll(Model model, HttpSession sessionObj, HttpServletRequest request,
			@RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
		int currPage = page.orElse(1);
		int pageSize = size.orElse(10);

		List<Leave> leaveHistory = leaveService
				.findAllEmployeeLeaves((Employee) sessionObj.getAttribute("employee"));

		// Clamp PageNumber between 1 to MaxPage
		int maxPage = (int) Math.ceil(leaveHistory.size() / (double) pageSize);
		int getPageNum = Math.max(1, Math.min(maxPage, currPage));
		getPageNum = getPageNum - 1; // Convert 1-Index to 0-Index

		Page<Leave> leaveHistoryPage = leaveService.getPaginatedLeaves(getPageNum, pageSize, leaveHistory);
		List<Leave> leaveHistoryPaged = leaveHistoryPage.getContent();

		model.addAttribute("leaveService", leaveService);
		model.addAttribute("currUrl", request.getRequestURI().toString());
		model.addAttribute("currPage", currPage);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("totalPages", leaveHistoryPage.getTotalPages());
		model.addAttribute("totalItems", leaveHistoryPage.getTotalElements());
		model.addAttribute("leaveHistory", leaveHistoryPaged);
		model.addAttribute("showAll", false);
		return "leave-history";
	}

	// RETRIEVE ONE
	@GetMapping(value = "/leave/details/{id}")
	public String staffLeaveDetail(@PathVariable(name = "id") Integer leaveId, Model model, HttpSession sessionObj) {
		Leave leave = leaveService.findEmployeeLeaveId((Employee) sessionObj.getAttribute("employee"), leaveId);
		model.addAttribute("leave", leave);
		return "leave-details";
	}

	// UPDATE
	@GetMapping(value = "/leave/edit/{id}")
	public String staffLeaveEditForm(@PathVariable(name = "id") Integer leaveId, Model model, HttpSession sessionObj) {
		Leave leave = leaveService.findEmployeeLeaveId((Employee) sessionObj.getAttribute("employee"), leaveId);
		model.addAttribute("leave", leave);
		model.addAttribute("leaveTypes", leaveTypeService.getTypeNames());
		model.addAttribute("leaveEntitlement", leaveEntitlementService
				.findAllLeaveEntitlementByEmployee((Employee) sessionObj.getAttribute("employee")));
		return "leave-edit";
	}

	@PostMapping(value = "/leave/edit/{id}")
	public String staffEditLeave(@PathVariable(name = "id") Integer leaveId,
			@Valid @ModelAttribute("leave") Leave leaveForm, BindingResult bindingResult, Model model,
			HttpSession sessionObj) {
		
		if (bindingResult.hasErrors()) {
			Leave leave = leaveService.findEmployeeLeaveId((Employee) sessionObj.getAttribute("employee"), leaveId);
			model.addAttribute("leave", leave);
			model.addAttribute("leaveTypes", leaveTypeService.getTypeNames());
			model.addAttribute("leaveEntitlement", leaveEntitlementService
					.findAllLeaveEntitlementByEmployee((Employee) sessionObj.getAttribute("employee")));
			return "leave-edit";
		}
		
		// Validate Sufficient Leave Balance
		LeaveEntitlement currLeaveEntitlement =
				leaveEntitlementService.findLeaveEntitlementByEmployeeAndType((Employee) sessionObj.getAttribute("employee"), leaveForm.getLeaveTypeString());
		double leaveBalance = currLeaveEntitlement.getLeaveBalance();
		double leaveDuration = leaveService.calculateDeductibleDaysInLeave(leaveForm);
		
		if (leaveBalance < leaveDuration) {
			Leave leave = leaveService.findEmployeeLeaveId((Employee) sessionObj.getAttribute("employee"), leaveId);
			model.addAttribute("leave", leave);
			model.addAttribute("leaveTypes", leaveTypeService.getTypeNames());
			model.addAttribute("leaveEntitlement", leaveEntitlementService
					.findAllLeaveEntitlementByEmployee((Employee) sessionObj.getAttribute("employee")));
			model.addAttribute("errorMsg", "You do not have sufficient leave balance.");
			return "leave-edit";
		}
		
		leaveForm.setId(leaveId);
		LeaveType currLeaveType = leaveTypeService.findByType(leaveForm.getLeaveTypeString());
		leaveForm.setLeaveType(currLeaveType);
		leaveForm.setEmployee((Employee) sessionObj.getAttribute("employee"));
		leaveForm.setStatus(LeaveStatus.UPDATED);
		leaveService.updateLeave(leaveForm);

		return "redirect:/staff/overview";
	}

	// CANCEL
	@PostMapping(value = "/leave/cancel/{id}")
	public String staffCancelLeave(@PathVariable(name = "id") Integer leaveId, Model model, HttpSession sessionObj) {
		Leave leave = leaveService.findEmployeeLeaveId((Employee) sessionObj.getAttribute("employee"), leaveId);
		leave.setStatus(LeaveStatus.CANCELLED);
		leaveService.updateLeave(leave);
		return "redirect:/staff/overview";
	}

	// DELETE
	@PostMapping(value = "/leave/delete/{id}")
	public String staffDeleteLeave(@PathVariable(name = "id") Integer leaveId, Model model, HttpSession sessionObj) {
		Leave leave = leaveService.findEmployeeLeaveId((Employee) sessionObj.getAttribute("employee"), leaveId);
		leave.setStatus(LeaveStatus.DELETED);
		leaveService.updateLeave(leave);
		return "redirect:/staff/overview";
	}
}
