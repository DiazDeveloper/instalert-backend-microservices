package com.instalert.emergencies;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import java.util.List;
public interface OutboxRepository extends JpaRepository<OutboxEvent,String> {
 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("select e from OutboxEvent e where e.publicationStatus='PENDING' order by e.occurredAt,e.aggregateVersion")
 List<OutboxEvent> pending(Pageable page);
}
