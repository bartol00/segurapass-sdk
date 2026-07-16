package xyz.segurapass.sdk.service.impl;

import com.segurapass.api.ApiClient;
import com.segurapass.api.ApiResponse;
import xyz.segurapass.sdk.exception.SegurapassSdkException;
import xyz.segurapass.sdk.service.UptimeService;

public class UptimeServiceImpl implements UptimeService {

    private final String baseEndpoint;

    public UptimeServiceImpl() {
        this.baseEndpoint = "/api/uptime";
    }

    @Override
    public boolean getUptime(String serverUrl) throws SegurapassSdkException {
        ApiClient apiClient = new ApiClient(serverUrl);
        ApiResponse<Void> uptimeResponse = apiClient.sendGetRequest(
                baseEndpoint,
                null,
                null,
                Void.class
        );
        return uptimeResponse.statusCode() == 200;
    }

}
