package com.zeeniya.expense_tracker.repository;

import com.zeeniya.expense_tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    // updates just this column
    @Transactional
    @Modifying
    @Query("update User u set u.alertsCheckedAt = :checkedAt where u.id = :userId")
    void updateAlertsCheckedAt(@Param("userId") Long userId, @Param("checkedAt") LocalDateTime checkedAt);

}
