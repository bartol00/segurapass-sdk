package com.segurapass.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.segurapass.ApiClient;
import com.segurapass.model.credentials.CredentialsReq;
import com.segurapass.model.credentials.CredentialsResp;
import com.segurapass.model.credentials.PagedResponse;
import com.segurapass.service.CredentialsService;

import java.util.Map;
import java.util.function.Supplier;

public class CredentialsServiceImpl implements CredentialsService {

    private final ApiClient apiClient;
    private final Supplier<String> jwtSupplier;
    private final String baseEndpoint;

    public CredentialsServiceImpl(ApiClient apiClient, Supplier<String> jwtSupplier) {
        this.apiClient = apiClient;
        this.jwtSupplier = jwtSupplier;
        this.baseEndpoint = "/api/credentials";
    }

    @Override
    public PagedResponse<CredentialsResp> getCredentials(int page, int size) {
        String endpoint = baseEndpoint + "/get";

        Map<String,String> params = Map.of(
                "page", String.valueOf(page),
                "size", String.valueOf(size)
        );

        return apiClient.sendGetRequest(
                endpoint,
                params,
                jwtSupplier.get(),
                null,
                new TypeReference<>() {}
        );
    }

    @Override
    public CredentialsResp addCredential(String website, String ivWebsite, String username, String ivUsername, String password, String ivPassword) {
        String endpoint = baseEndpoint + "/create";

        CredentialsReq req = new CredentialsReq(
                website,
                username,
                password,
                ivWebsite,
                ivUsername,
                ivPassword
        );

        return apiClient.sendPostRequest(
                req,
                endpoint,
                null,
                jwtSupplier.get(),
                null,
                CredentialsResp.class
        );
    }

    @Override
    public CredentialsResp updateCredential(String website, String ivWebsite, String username, String ivUsername, String password, String ivPassword, String credentialId) {
        String endpoint = baseEndpoint + "/update/" + credentialId;

        CredentialsReq req = new CredentialsReq(
                website,
                username,
                password,
                ivWebsite,
                ivUsername,
                ivPassword
        );

        return apiClient.sendPutRequest(
                req,
                endpoint,
                null,
                jwtSupplier.get(),
                null,
                CredentialsResp.class
        );
    }

    @Override
    public void deleteCredential(String credentialId) {
        String endpoint = baseEndpoint + "/delete/" + credentialId;

        apiClient.sendDeleteRequest(
                endpoint,
                null,
                jwtSupplier.get(),
                null,
                null
        );
    }
}
