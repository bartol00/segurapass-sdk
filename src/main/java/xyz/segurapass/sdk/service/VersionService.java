package xyz.segurapass.sdk.service;

import xyz.segurapass.sdk.exception.SegurapassSdkException;
import xyz.segurapass.api.versions.VersionInfo;

public interface VersionService {

    VersionInfo getVersionInfo() throws SegurapassSdkException;

}
