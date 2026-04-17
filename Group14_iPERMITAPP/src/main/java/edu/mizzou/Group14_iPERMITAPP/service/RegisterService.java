package edu.mizzou.Group14_iPERMITAPP.service;

// Domain models for RE (Resident Engineer) and EO (Environmental Officer)
import edu.mizzou.Group14_iPERMITAPP.model.EO;
import edu.mizzou.Group14_iPERMITAPP.model.RE;
import edu.mizzou.Group14_iPERMITAPP.model.RESite;

// Repository layer for database access
import edu.mizzou.Group14_iPERMITAPP.repository.EORepository;
import edu.mizzou.Group14_iPERMITAPP.repository.RERepository;

// Spring service annotation and dependency injection
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// Java utilities
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

// Marks this class as a service responsible for authentication and user registration logic
@Service
public class RegisterService {

	// Repository for RE users
	@Autowired
	private RERepository reRepository;

	// Repository for EO users
	@Autowired
	private EORepository eoRepository;

	// Handles login for both RE and EO users
	public boolean login(String email, String password) {

		// Attempt to find RE user by email
		RE re = reRepository.findByEmail(email);

		// Attempt to find EO user by email
		EO eo = eoRepository.findByEmail(email);

		// If user exists as RE, validate password
		if (re != null) {
			return re.getPassword().equals(password);

			// If user exists as EO, validate password
		} else if (eo != null) {
			return eo.getPassword().equals(password);
		}

		// Login fails if no matching user found
		return false;
	}

	// Regex pattern for validating email format
	private static final Pattern EMAIL_PATTERN =
			Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

	// Handles registration of new RE users
	public String register(String name,
	                       String orgName,
	                       String address,
	                       String email,
	                       String password,
	                       String siteAddress,
	                       String siteContactPerson) {

		// Validate that no required fields are empty
		if (isEmpty(name) || isEmpty(orgName) || isEmpty(address) ||
				isEmpty(email) || isEmpty(password) ||
				isEmpty(siteAddress) || isEmpty(siteContactPerson)) {
			return "EMPTY_FIELDS";
		}

		// Validate email format
		if (!EMAIL_PATTERN.matcher(email).matches()) {
			return "INVALID_EMAIL";
		}

		// Ensure email is not already registered
		if (reRepository.findByEmail(email) != null) {
			return "EMAIL_EXISTS";
		}

		// Create new RE user object
		RE user = new RE();
		user.setContactPersonName(name);
		user.setOrganizationName(orgName);
		user.setOrganizationAddress(address);
		user.setEmail(email);
		user.setPassword(password);

		// Set account creation date
		user.setCreatedDate(new Date());

		// Create and associate initial site with user
		RESite site = new RESite();
		site.setSiteAddress(siteAddress);
		site.setSiteContactPerson(siteContactPerson);
		site.setRe(user);

		// Attach site to user's site list
		List<RESite> siteList = new ArrayList<>();
		siteList.add(site);
		user.setSites(siteList);

		// Save user to database
		reRepository.save(user);

		// Indicate successful registration
		return "SUCCESS";
	}

	// Helper method to check for null or empty strings
	private boolean isEmpty(String s) {
		return s == null || s.trim().isEmpty();
	}
}