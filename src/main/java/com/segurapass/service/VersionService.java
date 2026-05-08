package com.segurapass.service;

import xyz.segurapass.api.versions.VersionInfo;
import com.segurapass.exception.SdkException;

public interface VersionService {

    VersionInfo getVersionInfo() throws SdkException;
}
