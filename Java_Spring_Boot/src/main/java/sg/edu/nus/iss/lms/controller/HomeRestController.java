package sg.edu.nus.iss.lms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import sg.edu.nus.iss.lms.model.Account;
import sg.edu.nus.iss.lms.model.Employee;
import sg.edu.nus.iss.lms.service.AccountService;
import sg.edu.nus.iss.lms.service.EmployeeService;

@RestController
@RequestMapping(value="/rest")
public class HomeRestController {
	 @Autowired
	 private AccountService accService;

	 @Autowired
	 private EmployeeService empService;
	 
	 
	 @GetMapping(value = { "/", "/login", "/home" })
	 public ResponseEntity<String> login() {
		 return new ResponseEntity<>("This is Login", HttpStatus.OK);
		 }
	 
	 @PostMapping("/authenticate")
	 public ResponseEntity<String> handleLogin(@Valid @RequestBody Account accForm, BindingResult bindingResult, HttpSession sessionObj) {
        if (bindingResult.hasErrors()) {
            return new ResponseEntity<>("Invalid input data", HttpStatus.BAD_REQUEST);
        }

        Account acc = accService.authenticate(accForm.getUsername(), accForm.getPassword());
        if (acc == null) {
            return new ResponseEntity<>("Invalid Username or Password. Please try again.", HttpStatus.UNAUTHORIZED);
        }

        Employee emp = empService.findEmployeeByAccount(acc);
        sessionObj.setAttribute("employee", emp);

        return new ResponseEntity<>("Login successful", HttpStatus.OK);
	 	}
	 
	@GetMapping("/logout")
	public ResponseEntity<String> logout(HttpSession sessionObj) {
		sessionObj.invalidate();
		return new ResponseEntity<>("Logout successful", HttpStatus.OK);
		}
	
	@GetMapping("/calendar")
	public ResponseEntity<String> getCalendar() {
		return new ResponseEntity<>("Calendar data", HttpStatus.OK);
		}
}
