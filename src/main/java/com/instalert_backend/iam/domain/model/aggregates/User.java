package com.instalert_backend.iam.domain.model.aggregates;

import com.instalert_backend.iam.domain.model.entities.Role;
import com.instalert_backend.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class User extends AbstractDomainAggregateRoot<User> {

    private Long id;
    private String email;
    private String password;
    private List<Role> roles;

    public User() {
        this.roles = new ArrayList<>();
    }

    public User(String email, String password) {
        this();
        this.email = email;
        this.password = password;
    }

    public User(String email, String password, List<Role> roles) {
        this(email, password);
        this.roles = roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }
}