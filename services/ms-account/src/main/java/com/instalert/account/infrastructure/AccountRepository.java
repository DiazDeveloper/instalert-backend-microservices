package com.instalert.account.infrastructure;
import com.instalert.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByIdentityEmail(String email);
    List<Account> findByIdentityStatus(String status);
    boolean existsByIdentityEmail(String email);
}
