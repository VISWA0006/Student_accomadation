package com.accommodation.service;

import com.accommodation.entity.Student;
import com.accommodation.entity.UserAccount;
import com.accommodation.repository.StudentRepository;
import com.accommodation.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"h2", "test"})
@Transactional
class AccountProvisioningIntegrationTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    void provisionedStudentMustChangeTemporaryPassword() {
        Student student = studentRepository.save(student("ACCOUNT-001", "FEMALE"));
        UserAccountService.StudentCredentials credentials = userAccountService
                .provisionStudentAccount(student.getId());
        UserAccount account = userAccountRepository.findByUsernameIgnoreCase(credentials.username())
                .orElseThrow();

        assertTrue(account.isPasswordChangeRequired());
        assertTrue(passwordEncoder.matches(credentials.temporaryPassword(), account.getPasswordHash()));

        userAccountService.changePassword(credentials.username(),
                credentials.temporaryPassword(), "A-new-secure-password-123");
        UserDetails updated = userAccountService.loadUserByUsername(credentials.username());

        assertFalse(account.isPasswordChangeRequired());
        assertFalse(updated.getAuthorities().stream()
                .anyMatch(authority -> "PASSWORD_CHANGE_REQUIRED".equals(authority.getAuthority())));
    }

    private Student student(String registerNumber, String gender) {
        return new Student("Test " + registerNumber, registerNumber,
                registerNumber.toLowerCase() + "@example.test", "5551234567",
                "Computer Science", 1, gender);
    }
}
