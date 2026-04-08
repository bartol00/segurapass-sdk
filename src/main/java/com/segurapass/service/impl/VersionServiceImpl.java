package com.segurapass.service.impl;

import com.segurapass.ApiClient;
import com.segurapass.model.versions.VersionInfo;
import com.segurapass.service.VersionService;

public class VersionServiceImpl implements VersionService {

    private final ApiClient apiClient;
    private final String baseEndpoint;

    public VersionServiceImpl(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.baseEndpoint = "/api/versions";
    }

    @Override
    public VersionInfo getVersionInfo() {
        String endpoint = baseEndpoint + "/latest";

        return apiClient.sendGetRequest(
                endpoint,
                null,
                null,
                null,
                VersionInfo.class
        );
    }
}
