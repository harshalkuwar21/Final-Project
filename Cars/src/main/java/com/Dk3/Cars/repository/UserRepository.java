package com.Dk3.Cars.repository;

import com.Dk3.Cars.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByEnabled(boolean enabled);

    // count staff or other roles (not ROLE_USER)
    long countByRoleNot(String role);

    // find users who are not regular users (staff/admin)
    List<User> findByRoleNot(String role);

    // New methods for staff management
    List<User> findByRole(String role);
    List<User> findByActive(boolean active);
    List<User> findByDepartment(String department);

    @Query("SELECT u FROM User u WHERE u.role IN ('ROLE_SALES_EXECUTIVE', 'ROLE_MANAGER', 'ROLE_ACCOUNTANT') AND u.active = true")
    List<User> findActiveStaff();

    @Query("SELECT u FROM User u WHERE u.role = 'ROLE_SALES_EXECUTIVE' AND u.active = true")
    List<User> findActiveSalesExecutives();
}
