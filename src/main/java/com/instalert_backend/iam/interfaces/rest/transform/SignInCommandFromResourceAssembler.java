package com.instalert_backend.iam.interfaces.rest.transform;

import com.instalert_backend.iam.domain.model.commands.SignInCommand;
import com.instalert_backend.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {
    public static SignInCommand toCommandFromResource(SignInResource resource) {
        return new SignInCommand(resource.email(), resource.password());
    }
}