package com.segurapass.exception;

import lombok.Getter;

@Getter
public class SdkException extends RuntimeException {
    private final int statusCode;
    private final String httpMethod;
    private final String endpoint;

    public SdkException(int statusCode, String httpMethod, String message, String endpoint) {
        super(message);
        this.statusCode = statusCode;
        this.httpMethod = httpMethod;
        this.endpoint = endpoint;
    }

    public SdkException(int statusCode, String httpMethod, String message, String endpoint, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.httpMethod = httpMethod;
        this.endpoint = endpoint;
    }
}
