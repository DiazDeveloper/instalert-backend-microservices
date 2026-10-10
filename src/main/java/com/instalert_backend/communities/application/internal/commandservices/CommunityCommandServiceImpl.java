package com.instalert_backend.communities.application.internal.commandservices;

import com.instalert_backend.communities.application.commandservices.CommunityCommandService;
import com.instalert_backend.communities.domain.model.aggregates.Community;
import com.instalert_backend.communities.domain.model.commands.CreateCommunityCommand;
import com.instalert_backend.communities.domain.model.commands.DeleteCommunityCommand;
import com.instalert_backend.communities.domain.repositories.CommunityRepository;
import com.instalert_backend.shared.application.result.ApplicationError;
import com.instalert_backend.shared.application.result.Result;
import org.springframework.stereotype.Service;

@Service
public class CommunityCommandServiceImpl implements CommunityCommandService {
    private final CommunityRepository communityRepository;

    public CommunityCommandServiceImpl(CommunityRepository communityRepository) {
        this.communityRepository = communityRepository;
    }

    @Override
    public Result<Community, ApplicationError> handle(CreateCommunityCommand command) {
        try {
            // Aquí puedes agregar validaciones (ej. que no exista una junta vecinal con el mismo nombre)
            var community = new Community(command);
            var savedCommunity = communityRepository.save(community);
            return Result.success(savedCommunity);
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Community creation", e.getMessage()));
        }
    }

    @Override
    public void handle(DeleteCommunityCommand command) {
        var community = communityRepository.findById(command.communityId());

        if (community.isEmpty()) {
            throw new IllegalArgumentException("La comunidad no existe");
        }

        communityRepository.delete(community.get());
    }
}