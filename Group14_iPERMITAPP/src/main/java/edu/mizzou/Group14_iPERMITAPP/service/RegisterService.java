package edu.mizzou.Group14_iPERMITAPP.service;

import edu.mizzou.Group14_iPERMITAPP.model.EO;
import edu.mizzou.Group14_iPERMITAPP.model.RE;
import edu.mizzou.Group14_iPERMITAPP.model.RESite;
import edu.mizzou.Group14_iPERMITAPP.repository.EORepository;
import edu.mizzou.Group14_iPERMITAPP.repository.RERepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class RegisterService {

	@Autowired
	private RERepository reRepository;

	@Autowired
	private EORepository eoRepository;

	public boolean login(String email, String password) {
	    RE re = reRepository.findByEmail(email);
	    EO eo = eoRepository.findByEmail(email);
	    
	    if (re != null) {
	        return re.getPassword().equals(password);
	    } else if (eo != null) {
	        return eo.getPassword().equals(password);
	    }
	    
	    return false;
	}

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	public String register(String name, String orgName, String address, String email, String password, String siteAddress, String siteContactPerson) {

	    if (isEmpty(name) || isEmpty(orgName) || isEmpty(address) || isEmpty(email) || 
	            isEmpty(password) || isEmpty(siteAddress) || isEmpty(siteContactPerson)) {
	            return "EMPTY_FIELDS";
	    }

		if (!EMAIL_PATTERN.matcher(email).matches()) {
			return "INVALID_EMAIL";
		}

		if (reRepository.findByEmail(email) != null) {
			return "EMAIL_EXISTS";
		}

		RE user = new RE();
		user.setContactPersonName(name);
		user.setOrganizationName(orgName);
		user.setOrganizationAddress(address);
		user.setEmail(email);
		user.setPassword(password);
		user.setCreatedDate(new Date());
		
	    RESite site = new RESite();
	    site.setSiteAddress(siteAddress);
	    site.setSiteContactPerson(siteContactPerson);
	    site.setRe(user);
		
	    List<RESite> siteList = new ArrayList<>();
	    siteList.add(site);
	    user.setSites(siteList);

		reRepository.save(user);

		return "SUCCESS";
	}

	private boolean isEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}
}