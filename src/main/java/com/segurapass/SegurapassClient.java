package com.segurapass;

import com.segurapass.service.AuthorizationService;
import com.segurapass.service.CredentialsService;
import com.segurapass.service.DeletionService;
import com.segurapass.service.VersionService;
import com.segurapass.service.api.ApiClient;
import com.segurapass.service.impl.AuthorizationServiceImpl;
import com.segurapass.service.impl.CredentialsServiceImpl;
import com.segurapass.service.impl.DeletionServiceImpl;
import com.segurapass.service.impl.VersionServiceImpl;
import lombok.Setter;

import java.util.function.Supplier;

public class SegurapassClient {
    private final ApiClient apiClient;
    private final AuthorizationService authorizationService;
    private final CredentialsService credentialsService;
    private final DeletionService deletionService;
    private final VersionService versionService;
    @Setter
    private String jwt;

    public SegurapassClient(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.authorizationService = new AuthorizationServiceImpl(apiClient);
        this.credentialsService = new CredentialsServiceImpl(apiClient, jwtSupplier());
        this.deletionService = new DeletionServiceImpl(apiClient, jwtSupplier());
        this.versionService = new VersionServiceImpl(apiClient);
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
}
