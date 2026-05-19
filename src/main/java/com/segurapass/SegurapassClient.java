package com.segurapass;

import com.segurapass.service.*;
import com.segurapass.api.ApiClient;
import com.segurapass.service.impl.*;
import lombok.Setter;

import java.util.function.Supplier;

public class SegurapassClient {
    private final AuthorizationService authorizationService;
    private final CredentialsService credentialsService;
    private final DeletionService deletionService;
    private final VersionService versionService;
    private final KeyService keyService;
    private final PasswordChangeService passwordChangeService;
    @Setter
    private String jwt;

    public SegurapassClient(ApiClient apiClient) {
        this.authorizationService = new AuthorizationServiceImpl(apiClient);
        this.credentialsService = new CredentialsServiceImpl(apiClient, jwtSupplier());
        this.deletionService = new DeletionServiceImpl(apiClient, jwtSupplier());
        this.versionService = new VersionServiceImpl(apiClient);
        this.keyService = new KeyServiceImpl(apiClient);
        this.passwordChangeService = new PasswordChangeServiceImpl(apiClient, jwtSupplier());
    }

    public Supplier<String> jwtSupplier() {
        return () -> jwt;
    }

    public AuthorizationService auth() {
        return authorizationService;
    }

    public CredentialsService credentials() {
        return credentialsService;
    }

    public DeletionService deletion() {
        return deletionService;
    }

    public VersionService version() {
        return versionService;
    }

    public KeyService keys()  {
        return keyService;
    }

    public PasswordChangeService passwordChange() {
        return passwordChangeService;
    }
}
