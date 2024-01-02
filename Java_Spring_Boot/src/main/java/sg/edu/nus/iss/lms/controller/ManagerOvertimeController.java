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
import sg.edu.nus.iss.lms.model.OvertimeClaim;
import sg.edu.nus.iss.lms.model.OvertimeClaim.ClaimStatus;
import sg.edu.nus.iss.lms.service.EmployeeService;
import sg.edu.nus.iss.lms.service.OvertimeService;

@Controller
@RequestMapping(value = "/manager")
public class ManagerOvertimeController {

	@Autowired
	EmployeeService employeeService;
	
	@Autowired
	OvertimeService overtimeService;
	
	@GetMapping(value = "/overtime/pending")
	public String pendingOvertimes(Model model, HttpSession sessionObj) {
		List<OvertimeClaim> overtimePending = overtimeService.findAllSubordinatePendingOvertimes((Employee) sessionObj.getAttribute("employee"));
		Map<Integer, List<OvertimeClaim>> overtimePendingByEmployee = new HashMap<>();
		
		for (OvertimeClaim overtime : overtimePending) {
			Integer employeeId = overtime.getEmployee().getId();
			
			if(!overtimePendingByEmployee.containsKey(employeeId)) {
				overtimePendingByEmployee.put(employeeId, new ArrayList<>());
			}
			overtimePendingByEmployee.get(employeeId).add(overtime);
		}
		
		model.addAttribute("overtimePending", overtimePendingByEmployee);
		return "manager-overtime-pending";
	}
	
	@GetMapping(value = "/staff/{sid}/overtime")
	public String subordinateOvertimeHistory(@PathVariable(name = "sid") Integer subordinateId, Model model, HttpSession sessionObj, HttpServletRequest request,
			@RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
		int currPage = page.orElse(1);
		int pageSize = size.orElse(10);
		
		List<OvertimeClaim> overtimeHistory = overtimeService.findSubordinateOvertimeHistory((Employee) sessionObj.getAttribute("employee"), subordinateId);
		
		// Clamp PageNumber between 1 to MaxPage
		int maxPage = (int) Math.ceil(overtimeHistory.size() / (double) pageSize);
		int getPageNum = Math.max(1, Math.min(maxPage, currPage));
		getPageNum = getPageNum - 1; // Convert 1-Index to 0-Index

		Page<OvertimeClaim> overtimeHistoryPage = overtimeService.getPaginatedOvertimes(getPageNum, pageSize, overtimeHistory);
		List<OvertimeClaim> overtimeHistoryPaged = overtimeHistoryPage.getContent();

		model.addAttribute("subordinateName", employeeService.findEmployeeById(subordinateId).getName());
		model.addAttribute("currUrl", request.getRequestURI().toString());
		model.addAttribute("currPage", currPage);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("totalPages", overtimeHistoryPage.getTotalPages());
		model.addAttribute("totalItems", overtimeHistoryPage.getTotalElements());
		model.addAttribute("overtimeHistory", overtimeHistoryPaged);
		return "subordinate-overtime-history";
	}

	@GetMapping(value = "/staff/{sid}/overtime/{id}")
	public String subordinateOvertimeDetails(@PathVariable(name = "sid") Integer subordinateId,
										  @PathVariable(name = "id") Integer overtimeId,
										  Model model, HttpSession sessionObj) {
		OvertimeClaim overtime = overtimeService.findSubordinateOvertimeById((Employee) sessionObj.getAttribute("employee"), subordinateId, overtimeId);
		model.addAttribute("overtime", overtime);
		return "subordinate-overtime-details";
	}
	
	@PostMapping(value = "/staff/{sid}/overtime/{id}/approve")
	public String approveOvertimeClaim(@PathVariable(name = "sid") Integer subordinateId,
							   @PathVariable(name = "id") Integer overtimeId,
							   @Valid @ModelAttribute("overtime") OvertimeClaim overtimeForm, BindingResult bindingResult,
							   Model model, HttpSession sessionObj) {
		OvertimeClaim overtime = overtimeService.findSubordinateOvertimeById((Employee) sessionObj.getAttribute("employee"), subordinateId, overtimeId);
		overtime.setManagerComment(overtimeForm.getManagerComment());
		overtime.setStatus(ClaimStatus.APPROVED);
		overtimeService.updateOvertime(overtime);
		return "redirect:/manager/overview";
	}
	
	@PostMapping(value = "/staff/{sid}/overtime/{id}/reject")
	public String rejectOvertimeClaim(@PathVariable(name = "sid") Integer subordinateId,
							  @PathVariable(name = "id") Integer overtimeId,
							  @Valid @ModelAttribute("overtime") OvertimeClaim overtimeForm, BindingResult bindingResult,
							  Model model, HttpSession sessionObj) {
		OvertimeClaim overtime = overtimeService.findSubordinateOvertimeById((Employee) sessionObj.getAttribute("employee"), subordinateId, overtimeId);
		overtime.setManagerComment(overtimeForm.getManagerComment());
		overtime.setStatus(ClaimStatus.REJECTED);
		overtimeService.updateOvertime(overtime);
		return "redirect:/manager/overview";
	}
}
