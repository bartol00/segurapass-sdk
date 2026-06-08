package xyz.segurapass.sdk.service.impl;

import xyz.segurapass.api.versions.VersionInfo;
import com.segurapass.api.ApiClient;
import xyz.segurapass.sdk.service.VersionService;

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
                VersionInfo.class
        ).body();
    }
}
