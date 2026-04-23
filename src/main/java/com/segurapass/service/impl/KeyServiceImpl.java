package com.segurapass.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.segurapass.api.ApiClient;
import com.segurapass.api.ApiResponse;
import com.segurapass.exception.SdkException;
import com.segurapass.model.key.PublicKeyResp;
import com.segurapass.service.KeyService;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class KeyServiceImpl implements KeyService {

    private final ApiClient apiClient;
    private final String baseEndpoint;

    public KeyServiceImpl(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.baseEndpoint = "/.well-known";
    }

    @Override
    public PublicKey getPublicKey() {
        String endpoint = baseEndpoint + "/public-key";

        ApiResponse<PublicKeyResp> publicKeyResp = apiClient.sendGetRequest(
                endpoint,
                null,
                null,
                null,
                PublicKeyResp.class
        );

        return parsePublicKey(publicKeyResp.getBody().getPublicKey());
    }

    @Override
    public boolean isValid(String token, PublicKey publicKey) {
        try {
            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;

            Algorithm algorithm = Algorithm.RSA256(rsaPublicKey, null);

            JWTVerifier verifier = JWT.require(algorithm)
                    .build();

            verifier.verify(token);
            System.out.println("TOKEN VERIFIED");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private PublicKey parsePublicKey(String publicKeyString) throws SdkException {
        try {
            String cleanPem = publicKeyString
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(cleanPem);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
            System.out.println("PUBLIC KEY: " + Base64.getEncoder().encodeToString(publicKey.getEncoded()));
            return publicKey;
        } catch (Exception e) {
            throw new SdkException(500, "GET", "Failed to parse public key", "/.well-known/public-key");
        }
    }

}
