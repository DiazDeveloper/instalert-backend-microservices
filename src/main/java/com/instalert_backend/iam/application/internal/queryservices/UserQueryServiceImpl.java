package com.instalert_backend.iam.application.internal.queryservices;

import com.instalert_backend.iam.application.queryservices.UserQueryService;
import com.instalert_backend.iam.domain.model.aggregates.User;
import com.instalert_backend.iam.domain.model.queries.GetUserByIdQuery;
import com.instalert_backend.iam.domain.model.queries.GetUserByEmailQuery;
import com.instalert_backend.iam.domain.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("iamUserQueryService")
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.id());
    }

    @Override
    public Optional<User> handle(GetUserByEmailQuery query) {
        return userRepository.findByEmail(query.email());
    }
}