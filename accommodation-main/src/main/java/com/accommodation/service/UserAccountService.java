package com.accommodation.service;

import com.accommodation.entity.Student;
import com.accommodation.entity.UserAccount;
import com.accommodation.exception.DuplicateResourceException;
import com.accommodation.exception.ResourceNotFoundException;
import com.accommodation.repository.StudentRepository;
import com.accommodation.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class UserAccountService implements UserDetailsService {

    private final UserAccountRepository accountRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public UserAccountService(UserAccountRepository accountRepository,
                              StudentRepository studentRepository,
                              PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount account = accountRepository.findByUsernameIgnoreCase(username)
                .filter(UserAccount::isEnabled)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));

        return User.withUsername(account.getUsername())
                .password(account.getPasswordHash())
                .authorities(account.isPasswordChangeRequired()
                        ? java.util.List.of(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + account.getRole()),
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("PASSWORD_CHANGE_REQUIRED"))
                        : java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + account.getRole())))
                .build();
    }

    @Transactional(readOnly = true)
    public Long getStudentId(String username) {
        UserAccount account = accountRepository.findByUsernameIgnoreCase(username)
                .filter(UserAccount::isEnabled)
                .filter(user -> "STUDENT".equals(user.getRole()))
                .orElseThrow(() -> new ResourceNotFoundException("Student account not found"));
        return account.getStudent().getId();
    }

    @Transactional(readOnly = true)
    public boolean isStudentAccount(String username) {
        return accountRepository.findByUsernameIgnoreCase(username)
                .map(account -> "STUDENT".equals(account.getRole()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean hasStudentAccount(Long studentId) {
        return accountRepository.findByStudentId(studentId).isPresent();
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        UserAccount account = accountRepository.findByUsernameIgnoreCase(username)
                .filter(UserAccount::isEnabled)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found"));
        if (!passwordEncoder.matches(currentPassword, account.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (newPassword == null || newPassword.length() < 12) {
            throw new IllegalArgumentException("New password must contain at least 12 characters.");
        }
        account.changePassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public StudentCredentials provisionStudentAccount(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
        if (accountRepository.findByStudentId(studentId).isPresent()) {
            throw new DuplicateResourceException("This student already has a login account.");
        }

        String username = student.getRegisterNumber().trim();
        if (accountRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new DuplicateResourceException("An account already uses this register number.");
        }

        byte[] secret = new byte[18];
        secureRandom.nextBytes(secret);
        String temporaryPassword = Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
        accountRepository.save(new UserAccount(
                username, passwordEncoder.encode(temporaryPassword), "STUDENT", student));
        return new StudentCredentials(username, temporaryPassword);
    }

    public record StudentCredentials(String username, String temporaryPassword) {
    }
}
