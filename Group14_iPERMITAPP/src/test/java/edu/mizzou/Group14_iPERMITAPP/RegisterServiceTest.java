package edu.mizzou.Group14_iPERMITAPP;

import edu.mizzou.Group14_iPERMITAPP.model.RE;
import edu.mizzou.Group14_iPERMITAPP.repository.*;
import edu.mizzou.Group14_iPERMITAPP.service.RegisterService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegisterServiceTest {

    @Mock
    private RERepository reRepository;
    @Mock
    private EORepository eoRepository;

    @InjectMocks
    private RegisterService registerService;

    @Test
    public void login_validRECredentials_returnsTrue() {
        RE re = new RE();
        re.setEmail("test@example.com");
        re.setPassword("secret");
        when(reRepository.findByEmail("test@example.com")).thenReturn(re);

        assertTrue(registerService.login("test@example.com", "secret"));
    }

    @Test
    public void login_wrongPassword_returnsFalse() {
        RE re = new RE();
        re.setEmail("test@example.com");
        re.setPassword("secret");
        when(reRepository.findByEmail("test@example.com")).thenReturn(re);

        assertFalse(registerService.login("test@example.com", "wrongpassword"));
    }

    @Test
    public void register_duplicateEmail_returnsEmailExists() {
        when(reRepository.findByEmail("existing@example.com")).thenReturn(new RE());

        String result = registerService.register("Name", "Org", "Addr",
                "existing@example.com", "pass", "Site Addr", "Contact");

        assertEquals("EMAIL_EXISTS", result);
    }

    @Test
    public void register_invalidEmail_returnsInvalidEmail() {
        String result = registerService.register("Name", "Org", "Addr",
                "notanemail", "pass", "Site Addr", "Contact");

        assertEquals("INVALID_EMAIL", result);
    }

    @Test
    public void register_emptyField_returnsEmptyFields() {
        String result = registerService.register("", "Org", "Addr",
                "valid@example.com", "pass", "Site Addr", "Contact");

        assertEquals("EMPTY_FIELDS", result);
    }

    @Test
    public void register_validFields_returnsSuccess() {
        when(reRepository.findByEmail("new@example.com")).thenReturn(null);

        String result = registerService.register("Name", "Org", "Addr",
                "new@example.com", "pass", "Site Addr", "Contact");

        assertEquals("SUCCESS", result);
        verify(reRepository, times(1)).save(any(RE.class));
    }
}