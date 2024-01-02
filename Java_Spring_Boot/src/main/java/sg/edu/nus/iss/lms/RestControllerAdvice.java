package sg.edu.nus.iss.lms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.nus.iss.lms.validator.OvertimeValidator;

@ControllerAdvice(annotations = RestController.class)
public class RestControllerAdvice {
	@Autowired
	OvertimeValidator overtimeValidator;

    @InitBinder("leave")
    private void initLeaveBinder(WebDataBinder binder) {
        binder.addValidators(overtimeValidator);
    }
}
