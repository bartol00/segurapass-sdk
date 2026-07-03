package xyz.segurapass.sdk.service;

import xyz.segurapass.sdk.exception.SegurapassSdkException;

public interface UptimeService {
    boolean getUptime() throws SegurapassSdkException;
}
