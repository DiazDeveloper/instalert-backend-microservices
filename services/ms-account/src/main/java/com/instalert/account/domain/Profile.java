package com.instalert.account.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity @Table(name="account_profiles")
public class Profile {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long profileId;
    @Column(unique=true) public Long legacyProfileUserId;
    @Column(nullable=false, length=120) public String displayName;
    @Column(nullable=false, length=30) public String phone="";
    public LocalDate birthDate;
    @Column(nullable=false, length=40) public String gender="";
    @Column(nullable=false, length=2048) public String avatar="";
    @Column(nullable=false, length=255) public String currentLocation="";
    @Version public long version;
    @Column(nullable=false, updatable=false) public java.time.LocalDateTime createdAt;
    @Column(nullable=false) public java.time.LocalDateTime updatedAt;
    @PrePersist void created() { createdAt=java.time.LocalDateTime.now(java.time.ZoneOffset.UTC); updatedAt=createdAt; }
    @PreUpdate void updated() { updatedAt=java.time.LocalDateTime.now(java.time.ZoneOffset.UTC); }
    public long referenceId() { return legacyProfileUserId == null ? profileId : legacyProfileUserId; }
}
