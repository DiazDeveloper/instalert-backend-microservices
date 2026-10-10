package com.instalert_backend.iam.application.queryservices;

import com.instalert_backend.iam.domain.model.aggregates.User;
import com.instalert_backend.iam.domain.model.queries.GetUserByIdQuery;
import com.instalert_backend.iam.domain.model.queries.GetUserByEmailQuery;

import java.util.Optional;

public interface UserQueryService {
    Optional<User> handle(GetUserByIdQuery query);
    Optional<User> handle(GetUserByEmailQuery query);
}