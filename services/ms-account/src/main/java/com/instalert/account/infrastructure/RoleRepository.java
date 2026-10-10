package com.instalert.account.infrastructure;
import com.instalert.account.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface RoleRepository extends JpaRepository<Role, Short> { Optional<Role> findByRoleName(String name); }
