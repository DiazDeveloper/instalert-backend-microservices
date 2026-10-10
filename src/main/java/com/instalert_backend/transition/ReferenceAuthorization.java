package com.instalert_backend.transition;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public final class ReferenceAuthorization {
    private ReferenceAuthorization() {}
    public static void requireOwner(Long referenceId) {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth==null || !(auth.getPrincipal() instanceof AccountSecurityConfiguration.AccountPrincipal p))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if(referenceId==null || p.referenceId()!=referenceId)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    public static void requireOwnerOrAdmin(Long referenceId) {
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(auth!=null && auth.getAuthorities().stream().anyMatch(a->"ROLE_ADMIN".equals(a.getAuthority()))) return;
        requireOwner(referenceId);
    }
}
