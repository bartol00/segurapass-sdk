package xyz.segurapass.sdk.service;

import xyz.segurapass.sdk.exception.SegurapassSdkException;
import xyz.segurapass.sdk.models.VersionModel;

public interface VersionService {

    VersionModel getVersionInfo() throws SegurapassSdkException;

}
