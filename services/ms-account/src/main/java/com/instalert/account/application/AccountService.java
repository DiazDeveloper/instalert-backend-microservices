package com.instalert.account.application;

import com.instalert.account.domain.*;
import com.instalert.account.infrastructure.*;
import com.instalert.account.interfaces.AccountContracts.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service @Transactional
public class AccountService {
    private final AccountRepository accounts;
    private final RoleRepository roles;
    private final PasswordEncoder passwords;
    public AccountService(AccountRepository accounts, RoleRepository roles, PasswordEncoder passwords) {
        this.accounts=accounts; this.roles=roles; this.passwords=passwords;
    }
    public Account register(Registration request) {
        validatePassword(request.password());
        var email=normalize(request.email());
        if(accounts.existsByIdentityEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already registered");
        var identity=new Identity(); identity.email=email; identity.passwordHash=passwords.encode(request.password());
        identity.roles.add(roles.findByRoleName("ROLE_USER").orElseThrow());
        var profile=new Profile(); profile.displayName=request.name().trim();
        profile.phone=text(request.phone()); profile.gender=text(request.gender()); profile.birthDate=date(request.birthDate());
        // Cascading both inserts and the explicit association in ONE local transaction.
        return accounts.saveAndFlush(new Account(identity,profile));
    }
    @Transactional(readOnly=true)
    public Account signIn(Credentials request) {
        var a=accounts.findByIdentityEmail(normalize(request.email())).orElse(null);
        if(a==null || !a.isActive() || !passwords.matches(request.password(), a.identity.passwordHash))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials");
        return a;
    }
    @Transactional(readOnly=true)
    public Account active(String id) {
        var a=accounts.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found"));
        if(!a.isActive()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Account not found");
        return a;
    }
    @Transactional(readOnly=true)
    public List<Account> list() { return accounts.findByIdentityStatus("ACTIVE"); }
    public Account update(String id, Update request) {
        var a=active(id); var email=normalize(request.email());
        var duplicate=accounts.findByIdentityEmail(email);
        if(duplicate.isPresent() && !duplicate.get().accountId.equals(id)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already registered");
        a.identity.email=email;
        if(request.name()!=null) {
            if(request.name().isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Name required");
            a.profile.displayName=request.name().trim();
        }
        a.profile.phone=text(request.phone()); a.profile.gender=text(request.gender()); a.profile.birthDate=date(request.birthDate());
        return accounts.saveAndFlush(a);
    }
    public void changePassword(String id, PasswordChange request) {
        validatePassword(request.newPassword());
        var a=active(id);
        if(!passwords.matches(request.currentPassword(),a.identity.passwordHash)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid current password");
        a.identity.passwordHash=passwords.encode(request.newPassword()); a.identity.revokeSessions();
        accounts.saveAndFlush(a);
    }
    public Account changeRole(String id, RoleChange request) {
        var role=switch(request.role()) {
            case "Admin", "ROLE_ADMIN" -> "ROLE_ADMIN";
            case "Ciudadano verificado", "ROLE_USER" -> "ROLE_USER";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown role");
        };
        var a=active(id); a.identity.roles.clear(); a.identity.roles.add(roles.findByRoleName(role).orElseThrow());
        a.identity.revokeSessions(); return accounts.saveAndFlush(a);
    }
    public void disable(String id) { var a=active(id); a.identity.disable(); accounts.saveAndFlush(a); }
    private static String normalize(String value) { return value.trim().toLowerCase(Locale.ROOT); }
    private static void validatePassword(String value) {
        if(value.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Password exceeds BCrypt byte limit");
    }
    private static String text(String value) { return value==null?"":value.trim(); }
    private static LocalDate date(String value) {
        if(value==null || value.isBlank()) return null;
        try { var date=LocalDate.parse(value); if(date.isAfter(LocalDate.now())) throw new IllegalArgumentException(); return date; }
        catch(Exception e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Birth date must be a past ISO date"); }
    }
}
