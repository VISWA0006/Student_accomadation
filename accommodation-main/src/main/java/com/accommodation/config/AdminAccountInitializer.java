package com.accommodation.config;

import com.accommodation.entity.UserAccount;
import com.accommodation.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
public class AdminAccountInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final UserAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public AdminAccountInitializer(UserAccountRepository accountRepository,
                                   PasswordEncoder passwordEncoder,
                                   @Value("${app.security.admin.username:}") String username,
                                   @Value("${app.security.admin.password:}") String password) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException(
                    "Set ADMIN_USERNAME and ADMIN_PASSWORD before starting the application.");
        }
        if (password.length() < 12) {
            throw new IllegalStateException("ADMIN_PASSWORD must contain at least 12 characters.");
        }
        if (accountRepository.findByUsernameIgnoreCase(username).isEmpty()) {
            accountRepository.save(new UserAccount(
                    username, passwordEncoder.encode(password), "ADMIN", null));
            log.info("Created the configured administrator account.");
        }
    }
}
