package com.instalert.emergencies;
import org.springframework.data.jpa.repository.*;
public interface EmergencyRepository extends JpaRepository<EmergencyAlert,String>,JpaSpecificationExecutor<EmergencyAlert> {}
