package xyz.segurapass.sdk.service;

import xyz.segurapass.sdk.exception.SegurapassSdkException;
import xyz.segurapass.sdk.models.ClientLatestVersion;
import xyz.segurapass.sdk.models.ClientVersion;
import xyz.segurapass.sdk.models.VersionModel;

import java.io.IOException;

public interface VersionService {

    VersionModel getVersionInfo() throws SegurapassSdkException;
    ClientLatestVersion getClientLatestVersion(String baseUrl, String suffix) throws SegurapassSdkException;
    ClientVersion getClientVersion(String baseUrl, String suffix) throws SegurapassSdkException;
    byte[] getBytes(String url) throws SegurapassSdkException, IOException, InterruptedException;
}
