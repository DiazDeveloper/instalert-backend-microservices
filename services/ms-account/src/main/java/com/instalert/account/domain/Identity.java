package com.instalert.account.domain;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity @Table(name="account_identities")
public class Identity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long identityId;
    @Column(unique=true) public Long legacyIamUserId;
    @Column(nullable=false, unique=true, length=254) public String email;
    @Column(nullable=false, length=255) public String passwordHash;
    @Column(name="account_status", nullable=false, length=20) public String status = "ACTIVE";
    public long tokenVersion;
    @Column(nullable=false, updatable=false) public java.time.LocalDateTime createdAt;
    @Column(nullable=false) public java.time.LocalDateTime updatedAt;
    @PrePersist void created() { createdAt=java.time.LocalDateTime.now(java.time.ZoneOffset.UTC); updatedAt=createdAt; }
    @PreUpdate void updated() { updatedAt=java.time.LocalDateTime.now(java.time.ZoneOffset.UTC); }
    @Version public long version;
    @ManyToMany(fetch=FetchType.EAGER)
    @JoinTable(name="identity_roles", joinColumns=@JoinColumn(name="identity_id"), inverseJoinColumns=@JoinColumn(name="role_id"))
    public Set<Role> roles = new HashSet<>();
    public void revokeSessions() { tokenVersion++; }
    public void disable() { status="DISABLED"; revokeSessions(); }
}
