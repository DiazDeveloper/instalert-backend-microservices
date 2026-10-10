package com.instalert.account.domain;
import jakarta.persistence.*;

@Entity @Table(name="roles")
public class Role {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Short roleId;
    @Column(nullable=false, unique=true, length=50) public String roleName;
}
