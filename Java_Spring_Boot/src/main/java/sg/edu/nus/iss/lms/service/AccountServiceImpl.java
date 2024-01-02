package sg.edu.nus.iss.lms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.edu.nus.iss.lms.model.Account;
import sg.edu.nus.iss.lms.repository.AccountRepository;


@Service
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {
	@Autowired
	AccountRepository accRepo;
	
	@Override
	public Account authenticate(String username, String password) {
		return accRepo.findAccountByUsernamePassword(username, password);
	}
}
