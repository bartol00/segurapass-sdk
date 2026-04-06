package com.segurapass;

import com.segurapass.service.AuthorizationService;
import com.segurapass.service.impl.AuthorizationServiceImpl;
import lombok.Setter;

import java.util.function.Supplier;

public class SegurapassClient {
    private final ApiClient apiClient;
    private final AuthorizationService authorizationService;
    @Setter
    private String jwt;

    public SegurapassClient(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.authorizationService = new AuthorizationServiceImpl(apiClient);
    }

    // always returns the latest JWT
    public Supplier<String> jwtSupplier() {
        return () -> jwt;
    }

    public AuthorizationService auth() {
        return authorizationService;
    }
}
