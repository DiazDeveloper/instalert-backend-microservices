package com.instalert.emergencies;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CreationRequestRepository extends JpaRepository<CreationRequest,String> {
 Optional<CreationRequest> findByOwnerAccountIdAndRequestKey(String owner,String key);
}
