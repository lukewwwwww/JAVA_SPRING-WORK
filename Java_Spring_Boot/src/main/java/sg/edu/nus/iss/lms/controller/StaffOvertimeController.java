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
import sg.edu.nus.iss.lms.model.OvertimeClaim;
import sg.edu.nus.iss.lms.model.OvertimeClaim.ClaimStatus;
import sg.edu.nus.iss.lms.service.OvertimeService;
import sg.edu.nus.iss.lms.validator.OvertimeValidator;

@Controller
@RequestMapping(value="/staff/overtime")
public class StaffOvertimeController {
	
	@Autowired
	OvertimeService overtimeService;

	@Autowired
	OvertimeValidator overtimeValidator;
	
	@InitBinder("overtime")
	private void initLeaveBinder(WebDataBinder binder) {
		binder.addValidators(overtimeValidator);
	}
	
	// CREATE
	@GetMapping(value = "/apply")
	public String staffOvertimeApplicationForm(Model model, HttpSession sessionObj) {
		model.addAttribute("overtime", new OvertimeClaim());
		return "overtime-apply";
	}

	@PostMapping(value = "/apply")
	public String staffApplyOvertime(@Valid @ModelAttribute("overtime") OvertimeClaim overtimeForm, BindingResult bindingResult,
			Model model, HttpSession sessionObj) {

		if (bindingResult.hasErrors()) {
			return "overtime-apply";
		}
		
		overtimeForm.setEmployee((Employee) sessionObj.getAttribute("employee"));
		overtimeForm.setStatus(ClaimStatus.APPLIED);
		overtimeService.createOvertime(overtimeForm);

		return "redirect:/staff/overview";
	}

	// RETRIEVE CURR YEAR
	@GetMapping(value = "/history")
	public String staffOvertimeHistory(Model model, HttpSession sessionObj, HttpServletRequest request,
			@RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
		int currPage = page.orElse(1);
		int pageSize = size.orElse(10);

		List<OvertimeClaim> overtimeHistory = overtimeService
				.findCurrYearEmployeeOvertimes((Employee) sessionObj.getAttribute("employee"));

		// Clamp PageNumber between 1 to MaxPage
		int maxPage = (int) Math.ceil(overtimeHistory.size() / (double) pageSize);
		int getPageNum = Math.max(1, Math.min(maxPage, currPage));
		getPageNum = getPageNum - 1; // Convert 1-Index to 0-Index

		Page<OvertimeClaim> overtimeHistoryPage = overtimeService.getPaginatedOvertimes(getPageNum, pageSize, overtimeHistory);
		List<OvertimeClaim> overtimeHistoryPaged = overtimeHistoryPage.getContent();

		model.addAttribute("currUrl", request.getRequestURI().toString());
		model.addAttribute("currPage", currPage);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("totalPages", overtimeHistoryPage.getTotalPages());
		model.addAttribute("totalItems", overtimeHistoryPage.getTotalElements());
		model.addAttribute("overtimeHistory", overtimeHistoryPaged);
		model.addAttribute("showAll", false);
		return "overtime-history";
	}

	// RETRIEVE ALL
	@GetMapping(value = "/history/all")
	public String staffOvertimeHistoryAll(Model model, HttpSession sessionObj, HttpServletRequest request,
			@RequestParam("page") Optional<Integer> page, @RequestParam("size") Optional<Integer> size) {
		int currPage = page.orElse(1);
		int pageSize = size.orElse(10);

		List<OvertimeClaim> overtimeHistory = overtimeService
				.findAllEmployeeOvertimes((Employee) sessionObj.getAttribute("employee"));

		// Clamp PageNumber between 1 to MaxPage
		int maxPage = (int) Math.ceil(overtimeHistory.size() / (double) pageSize);
		int getPageNum = Math.max(1, Math.min(maxPage, currPage));
		getPageNum = getPageNum - 1; // Convert 1-Index to 0-Index

		Page<OvertimeClaim> overtimeHistoryPage = overtimeService.getPaginatedOvertimes(getPageNum, pageSize, overtimeHistory);
		List<OvertimeClaim> overtimeHistoryPaged = overtimeHistoryPage.getContent();

		model.addAttribute("currUrl", request.getRequestURI().toString());
		model.addAttribute("currPage", currPage);
		model.addAttribute("pageSize", pageSize);
		model.addAttribute("totalPages", overtimeHistoryPage.getTotalPages());
		model.addAttribute("totalItems", overtimeHistoryPage.getTotalElements());
		model.addAttribute("overtimeHistory", overtimeHistoryPaged);
		model.addAttribute("showAll", false);
		return "overtime-history";
	}

	// RETRIEVE ONE
	@GetMapping(value = "/details/{id}")
	public String staffOvertimeDetail(@PathVariable(name = "id") Integer overtimeId, Model model, HttpSession sessionObj) {
		OvertimeClaim overtime = overtimeService.findEmployeeOvertimeId((Employee) sessionObj.getAttribute("employee"), overtimeId);
		model.addAttribute("overtime", overtime);
		return "overtime-details";
	}

	// UPDATE
	@GetMapping(value = "/edit/{id}")
	public String staffOvertimeEditForm(@PathVariable(name = "id") Integer overtimeId, Model model, HttpSession sessionObj) {
		OvertimeClaim overtime = overtimeService.findEmployeeOvertimeId((Employee) sessionObj.getAttribute("employee"), overtimeId);
		model.addAttribute("overtime", overtime);
		return "overtime-edit";
	}

	@PostMapping(value = "/edit/{id}")
	public String staffEditOvertime(@PathVariable(name = "id") Integer overtimeId,
			@Valid @ModelAttribute("overtime") OvertimeClaim overtimeForm, BindingResult bindingResult, Model model,
			HttpSession sessionObj) {
		
		if (bindingResult.hasErrors()) {
			return "overtime-edit";
		}
		
		overtimeForm.setId(overtimeId);
		overtimeForm.setEmployee((Employee) sessionObj.getAttribute("employee"));
		overtimeForm.setStatus(ClaimStatus.UPDATED);
		overtimeService.updateOvertime(overtimeForm);
		return "redirect:/staff/overview";
	}

	// DELETE
	@PostMapping(value = "/delete/{id}")
	public String staffDeleteOvertime(@PathVariable(name = "id") Integer overtimeId, Model model, HttpSession sessionObj) {
		OvertimeClaim overtime = overtimeService.findEmployeeOvertimeId((Employee) sessionObj.getAttribute("employee"), overtimeId);
		overtime.setStatus(ClaimStatus.DELETED);
		overtimeService.updateOvertime(overtime);
		return "redirect:/staff/overview";
	}

}