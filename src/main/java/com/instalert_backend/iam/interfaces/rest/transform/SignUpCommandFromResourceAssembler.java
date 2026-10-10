package com.instalert_backend.iam.interfaces.rest.transform;

import com.instalert_backend.iam.domain.model.commands.SignUpCommand;
import com.instalert_backend.iam.interfaces.rest.resources.SignUpResource;

public class SignUpCommandFromResourceAssembler {
    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        return new SignUpCommand(resource.email(), resource.password(), resource.roles());
    }
}