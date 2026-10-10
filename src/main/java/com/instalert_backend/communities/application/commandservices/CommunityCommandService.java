package com.instalert_backend.communities.application.commandservices;

import com.instalert_backend.communities.domain.model.aggregates.Community;
import com.instalert_backend.communities.domain.model.commands.CreateCommunityCommand;
import com.instalert_backend.communities.domain.model.commands.DeleteCommunityCommand;
import com.instalert_backend.shared.application.result.ApplicationError;
import com.instalert_backend.shared.application.result.Result;

public interface CommunityCommandService {
    Result<Community, ApplicationError> handle(CreateCommunityCommand command);
    void handle(DeleteCommunityCommand command);
}