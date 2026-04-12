package com.segurapass.service;

import com.segurapass.exception.SdkException;
import com.segurapass.model.versions.VersionInfo;

public interface VersionService {

    VersionInfo getVersionInfo() throws SdkException;
}
