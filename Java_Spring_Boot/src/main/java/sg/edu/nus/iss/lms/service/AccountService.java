package sg.edu.nus.iss.lms.service;

import sg.edu.nus.iss.lms.model.Account;

public interface AccountService {
	public Account authenticate(String username, String password);
}
