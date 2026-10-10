package com.instalert.account.domain;
import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name="account_associations")
public class Account {
    @Id @Column(length=36) @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR) public String accountId;
    @OneToOne(optional=false, cascade={CascadeType.PERSIST,CascadeType.MERGE}) @JoinColumn(name="identity_id", unique=true) public Identity identity;
    @OneToOne(optional=false, cascade={CascadeType.PERSIST,CascadeType.MERGE}) @JoinColumn(name="profile_id", unique=true) public Profile profile;
    protected Account() {}
    public Account(Identity identity, Profile profile) {
        this.accountId=UUID.randomUUID().toString(); this.identity=identity; this.profile=profile;
    }
    public boolean isActive() { return "ACTIVE".equals(identity.status); }
    public boolean isAdmin() { return identity.roles.stream().anyMatch(r -> "ROLE_ADMIN".equals(r.roleName)); }
}
