package com.segurapass;

import com.segurapass.service.AuthorizationService;
import com.segurapass.service.CredentialsService;
import com.segurapass.service.impl.AuthorizationServiceImpl;
import com.segurapass.service.impl.CredentialsServiceImpl;
import lombok.Setter;

import java.util.function.Supplier;

public class SegurapassClient {
    private final ApiClient apiClient;
    private final AuthorizationService authorizationService;
    private final CredentialsService credentialsService;
    @Setter
    private String jwt;

    public SegurapassClient(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.authorizationService = new AuthorizationServiceImpl(apiClient);
        this.credentialsService = new CredentialsServiceImpl(apiClient, jwtSupplier());
    }

    // always returns the latest JWT
    public Supplier<String> jwtSupplier() {
        return () -> jwt;
    }

    public AuthorizationService auth() {
        return authorizationService;
    }

    public CredentialsService credentials() {
        return credentialsService;
    }
}
