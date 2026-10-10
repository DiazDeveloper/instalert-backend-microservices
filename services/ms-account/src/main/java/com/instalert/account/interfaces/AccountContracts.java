package com.instalert.account.interfaces;

import com.instalert.account.domain.Account;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public final class AccountContracts {
    private AccountContracts() {}
    public record Registration(@NotBlank @Size(max=120) String name,
        @NotBlank @Email @Size(max=254) String email, @NotBlank @Size(min=8,max=72) String password,
        @Size(max=30) String phone, String birthDate, @Size(max=40) String gender) {}
    public record Credentials(@NotBlank @Email @Size(max=254) String email, @NotBlank @Size(max=72) String password) {}
    public record Update(@Size(max=120) String name, @NotBlank @Email @Size(max=254) String email,
        @Size(max=30) String phone, String birthDate, @Size(max=40) String gender) {}
    public record PasswordChange(@NotBlank @Size(max=72) String currentPassword, @NotBlank @Size(min=8,max=72) String newPassword) {}
    public record RoleChange(@NotBlank String role) {}
    public record ProfileView(long id, String accountId, String name, String email, String role,
        List<String> roles, String currentLocation, String avatar, String phone, LocalDate birthDate, String gender) {
        public static ProfileView from(Account a) {
            return new ProfileView(a.profile.referenceId(), a.accountId, a.profile.displayName, a.identity.email,
                a.isAdmin()?"Admin":"Ciudadano verificado", a.identity.roles.stream().map(r->r.roleName).sorted().toList(),
                a.profile.currentLocation, a.profile.avatar, a.profile.phone, a.profile.birthDate, a.profile.gender);
        }
    }
    // Directory deliberately omits email, phone, date of birth, location and authority roles.
    public record DirectoryEntry(long id, String accountId, String name, String avatar) {}
    public record Session(String accountId, String email, String token, ProfileView profile) {}
    public record PrincipalView(String accountId, long referenceId, List<String> roles) {}
}
