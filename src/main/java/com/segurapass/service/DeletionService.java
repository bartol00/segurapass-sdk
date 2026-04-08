package com.segurapass.service;

import java.util.UUID;

public interface DeletionService {

    void emailDeletion(String email);
    void authorizedDeletion(String email, String masterPassword, UUID deviceId);
}
