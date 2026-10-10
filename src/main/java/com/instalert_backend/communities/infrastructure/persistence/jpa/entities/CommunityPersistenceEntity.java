package com.instalert_backend.communities.infrastructure.persistence.jpa.entities;

import com.instalert_backend.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "communities")
public class CommunityPersistenceEntity extends AuditableAbstractPersistenceEntity {
    private String name;
    private String description;
    private boolean isPrivate;
    private Long ownerId;

    public CommunityPersistenceEntity() {}


    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
}