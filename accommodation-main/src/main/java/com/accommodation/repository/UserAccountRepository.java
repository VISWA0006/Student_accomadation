package com.accommodation.repository;

import com.accommodation.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsernameIgnoreCase(String username);

    Optional<UserAccount> findByStudentId(Long studentId);
}
