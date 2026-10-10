package com.instalert.account.interfaces;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice
public class AccountErrors {
    @ExceptionHandler({DataIntegrityViolationException.class,ObjectOptimisticLockingFailureException.class})
    ResponseEntity<Map<String,String>> conflict(Exception e) {
        return ResponseEntity.status(409).body(Map.of("message","Account conflict; reload and retry"));
    }
}
