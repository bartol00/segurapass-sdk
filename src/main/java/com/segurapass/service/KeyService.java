package com.segurapass.service;

import com.segurapass.exception.SdkException;

import java.security.PublicKey;

public interface KeyService {

    PublicKey getPublicKey() throws SdkException;
    boolean isValid(String token, PublicKey publicKey);

}
