package xyz.segurapass.sdk.service;

import xyz.segurapass.sdk.exception.SegurapassSdkException;

import java.security.PublicKey;

public interface KeyService {

    PublicKey getPublicKey()
            throws SegurapassSdkException;

    boolean isValid(String token, PublicKey publicKey);

}
