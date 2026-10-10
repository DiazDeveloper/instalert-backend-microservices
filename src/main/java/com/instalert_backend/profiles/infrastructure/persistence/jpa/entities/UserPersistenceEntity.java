package com.instalert_backend.profiles.infrastructure.persistence.jpa.entities;

import com.instalert_backend.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(name = "ProfilesUserPersistenceEntity")
@Table(name = "users")
public class UserPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String role;
    private String currentLocation;
    private String avatar;
    private String phone;
    private String birthDate;
    private String gender;

    public UserPersistenceEntity() {}
}