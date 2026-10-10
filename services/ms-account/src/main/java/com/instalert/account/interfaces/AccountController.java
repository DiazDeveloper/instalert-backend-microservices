package com.instalert.account.interfaces;

import com.instalert.account.application.AccountService;
import com.instalert.account.infrastructure.AccountTokens;
import com.instalert.account.interfaces.AccountContracts.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@RestController @RequestMapping("/api/v1")
public class AccountController {
    private final AccountService service; private final AccountTokens tokens;
    public AccountController(AccountService service,AccountTokens tokens) { this.service=service; this.tokens=tokens; }
    @PostMapping("/authentication/sign-up") @ResponseStatus(HttpStatus.CREATED)
    public Session register(@Valid @RequestBody Registration r) { return session(service.register(r)); }
    @PostMapping("/authentication/sign-in")
    public Session login(@Valid @RequestBody Credentials r) { return session(service.signIn(r)); }
    @GetMapping("/account/me")
    public ProfileView me(Authentication auth) { return ProfileView.from(service.active(auth.getName())); }
    @GetMapping("/account/session")
    public PrincipalView principal(Authentication auth) {
        var a=service.active(auth.getName());
        return new PrincipalView(a.accountId,a.profile.referenceId(),a.identity.roles.stream().map(r->r.roleName).sorted().toList());
    }
    @GetMapping("/account/directory")
    public List<DirectoryEntry> directory() { return service.list().stream().map(a->new DirectoryEntry(a.profile.referenceId(),a.accountId,a.profile.displayName,a.profile.avatar)).toList(); }
    @GetMapping("/account/count")
    public Map<String,Integer> count() { return Map.of("count",service.list().size()); }
    @GetMapping("/users") public List<ProfileView> users() { return service.list().stream().map(ProfileView::from).toList(); }
    @GetMapping("/users/{id}")
    public ProfileView get(@PathVariable String id,Authentication auth) { ownerOrAdmin(auth,id); return ProfileView.from(service.active(id)); }
    @PutMapping("/users/{id}")
    public ProfileView update(@PathVariable String id,@Valid @RequestBody Update r,Authentication auth) { ownerOrAdmin(auth,id); return ProfileView.from(service.update(id,r)); }
    @PostMapping("/authentication/{id}/change-password") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void password(@PathVariable String id,@Valid @RequestBody PasswordChange r,Authentication auth) {
        // Password changes require the owner even for administrators.
        if(!auth.getName().equals(id)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        service.changePassword(id,r);
    }
    @PutMapping("/users/{id}/role")
    public ProfileView role(@PathVariable String id,@Valid @RequestBody RoleChange r,Authentication auth) { admin(auth); return ProfileView.from(service.changeRole(id,r)); }
    @DeleteMapping("/users/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable String id,Authentication auth) { admin(auth); service.disable(id); }
    private Session session(com.instalert.account.domain.Account a) { return new Session(a.accountId,a.identity.email,tokens.issue(a),ProfileView.from(a)); }
    private static void ownerOrAdmin(Authentication auth,String id) { if(!auth.getName().equals(id)) admin(auth); }
    private static void admin(Authentication auth) {
        if(auth.getAuthorities().stream().noneMatch(a->a.getAuthority().equals("ROLE_ADMIN"))) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
}
