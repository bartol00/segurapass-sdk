package xyz.segurapass.sdk.service.impl;

import xyz.segurapass.api.versions.VersionInfo;
import com.segurapass.api.ApiClient;
import xyz.segurapass.sdk.exception.SegurapassSdkException;
import xyz.segurapass.sdk.helpers.DownloadClient;
import xyz.segurapass.sdk.models.ClientLatestVersion;
import xyz.segurapass.sdk.models.ClientVersion;
import xyz.segurapass.sdk.models.VersionModel;
import xyz.segurapass.sdk.service.VersionService;

import java.io.IOException;

public class VersionServiceImpl implements VersionService {

    private final ApiClient apiClient;
    private final String baseEndpoint;

    public VersionServiceImpl(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.baseEndpoint = "/api/versions";
    }

    @Override
    public VersionModel getVersionInfo() {
        VersionInfo versionInfo = apiClient.sendGetRequest(
                baseEndpoint,
                null,
                null,
                VersionInfo.class
        ).body();

        return new VersionModel(
                versionInfo.getAppVersion(),
                versionInfo.getProtocolVersion()
        );
    }

    @Override
    public ClientLatestVersion getClientLatestVersion(String baseUrl, String suffix) throws SegurapassSdkException {
        ApiClient client = new ApiClient(baseUrl);
        return client.sendGetRequest(
                suffix,
                null,
                null,
                ClientLatestVersion.class
        ).body();
    }

    @Override
    public ClientVersion getClientVersion(String baseUrl, String suffix) throws SegurapassSdkException {
        ApiClient client = new ApiClient(baseUrl);
        return client.sendGetRequest(
                suffix,
                null,
                null,
                ClientVersion.class
        ).body();
    }

    @Override
    public byte[] getBytes(String url)
            throws SegurapassSdkException, IOException, InterruptedException {
        DownloadClient downloadClient = new DownloadClient();
        return downloadClient.downloadBytes(url);
    }

}
