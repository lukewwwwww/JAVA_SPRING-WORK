package sg.edu.nus.iss.lms.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sg.edu.nus.iss.lms.model.LeaveType;
import sg.edu.nus.iss.lms.repository.LeaveTypeRepository;

@Service
@Transactional(readOnly = true)
public class LeaveTypeServiceImpl implements LeaveTypeService {
	@Autowired
	LeaveTypeRepository leaveTypeRepo;

	public List<String> getTypeNames() {
		return leaveTypeRepo.findLeaveTypeNames();
	}

	public LeaveType findByType(String type) {
		return leaveTypeRepo.findByType(type);
	}

	@Override
	public List<LeaveType> findAll() {
		return leaveTypeRepo.findAll();
	}

	@Override
	public Optional<LeaveType> findById(Integer id) {
		return Optional.ofNullable(leaveTypeRepo.getById(id));
	}

	@Override
	public LeaveType update(Integer id, LeaveType leaveType) {
		Optional<LeaveType> optionalLeaveType = leaveTypeRepo.findById(id);
		if (optionalLeaveType.isPresent()) {
			LeaveType existingLeaveType = optionalLeaveType.get();

			return leaveTypeRepo.save(existingLeaveType);
		} else {
			return null;
		}
	}
}
