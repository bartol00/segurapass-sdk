package com.segurapass.service;

import com.segurapass.exception.SdkException;

import java.util.UUID;

public interface DeletionService {
    void emailDeletion(String email) throws SdkException;
    void authorizedDeletion(String email, char[] masterPassword, UUID deviceId) throws SdkException;
}
