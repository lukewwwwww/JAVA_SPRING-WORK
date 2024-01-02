package sg.edu.nus.iss.lms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import sg.edu.nus.iss.lms.model.Account;
import sg.edu.nus.iss.lms.model.Employee;
import sg.edu.nus.iss.lms.service.AccountService;
import sg.edu.nus.iss.lms.service.EmployeeService;

@Controller
public class HomeController {
	
	@Autowired
	private AccountService accService;

	@Autowired
	private EmployeeService empService;
	
	@GetMapping(value = { "/", "/login", "/home" })
	public String login(Model model) {
		model.addAttribute("account", new Account());
		return "login";
	}
	
	@PostMapping(value = "/authenticate")
	public String handleLogin(@Valid @ModelAttribute("account") Account accForm, BindingResult bindingResult, Model model, HttpSession sessionObj) {
		if (bindingResult.hasErrors()) {
			return "login";
		}
		
		Account acc = accService.authenticate(accForm.getUsername(), accForm.getPassword());
		if (acc == null) {
			model.addAttribute("errorMsg", "Invalid Username or Password. Please try again.");
			return "login";
		}
		
		Employee emp = empService.findEmployeeByAccount(acc);
		sessionObj.setAttribute("employee", emp);
		
		return "redirect:/staff/overview";
	}
	
	@GetMapping(value = "/logout")
	public String logout(Model model) {
		return "redirect:/login";
	}
	
	@GetMapping(value = "/calendar")
	public String calendar(Model model) {
		return "calendar";
	}
}
