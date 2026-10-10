package com.instalert_backend.iam.domain.model.entities;

import com.instalert_backend.iam.domain.model.valueobjects.Roles;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Role {

    private Long id;
    private Roles name;

    public Role(Roles name) {
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStringName() {
        return name.name();
    }
}