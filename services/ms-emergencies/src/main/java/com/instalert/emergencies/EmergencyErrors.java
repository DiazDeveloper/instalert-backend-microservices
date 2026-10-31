package com.instalert.emergencies;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.dao.OptimisticLockingFailureException;
import java.util.Map;
@RestControllerAdvice
public class EmergencyErrors {
 @ExceptionHandler(OptimisticLockingFailureException.class)
 ResponseEntity<?> concurrentUpdate() { return ResponseEntity.status(409).body(Map.of("status",409,"error","Concurrent update; retry request")); }
}
