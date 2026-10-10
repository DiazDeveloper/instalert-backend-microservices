package com.instalert_backend.iam.application.queryservices;

import com.instalert_backend.iam.domain.model.entities.Role;
import com.instalert_backend.iam.domain.model.queries.GetAllRolesQuery;

import java.util.List;

public interface RoleQueryService {
    List<Role> handle(GetAllRolesQuery query);
}