package xyz.segurapass.sdk.service;

import xyz.segurapass.sdk.exception.SegurapassSdkException;

import java.util.UUID;

public interface DeletionService {

    void emailDeletion(String email)
            throws SegurapassSdkException;

    void authorizedDeletion(String email, char[] masterPassword, UUID deviceId)
            throws SegurapassSdkException;

}
