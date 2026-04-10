package edu.mizzou.Group14_iPERMITAPP.service;

import edu.mizzou.Group14_iPERMITAPP.model.RE;
import edu.mizzou.Group14_iPERMITAPP.repository.RERepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RegisterService {

	@Autowired
	private RERepository reRepository;

	public boolean login(String email, String password) {
		RE user = reRepository.findByEmail(email);

		if (user == null)
			return false;

		return user.getPassword().equals(password);
	}

	public boolean register(String name, String orgName, String address, String email, String password) {

		if (reRepository.findByEmail(email) != null) {
			return false;
		}

		RE user = new RE();
		user.setContactPersonName(name);
		user.setOrganizationName(orgName);
		user.setOrganizationAddress(address);
		user.setEmail(email);
		user.setPassword(password);
		user.setCreatedDate(new Date());

		reRepository.save(user);

		return true;
	}
}