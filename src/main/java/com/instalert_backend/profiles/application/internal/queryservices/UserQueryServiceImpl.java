package com.instalert_backend.profiles.application.internal.queryservices;

import com.instalert_backend.profiles.application.queryservices.UserQueryService;
import com.instalert_backend.profiles.domain.model.aggregates.User;
import com.instalert_backend.profiles.domain.model.queries.GetAllUsersQuery;
import com.instalert_backend.profiles.domain.model.queries.GetUserByIdQuery;
import com.instalert_backend.profiles.domain.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("profilesUserQueryService")
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> handle(GetAllUsersQuery query) {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.id());
    }
}